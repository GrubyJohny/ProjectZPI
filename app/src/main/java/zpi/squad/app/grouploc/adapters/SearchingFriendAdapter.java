package zpi.squad.app.grouploc.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.activities.MainActivity;
import zpi.squad.app.grouploc.domains.Friend;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class SearchingFriendAdapter extends ArrayAdapter<Friend> implements Filterable {
    private final ArrayList<Friend> helpList;
    private final CommonMethods commonMethods;
    private SessionManager session = SessionManager.getInstance();
    private ArrayList<Friend> items;
    private ArrayList<Friend> filteredList = new ArrayList<>();
    ParseQuery<ParseUser> queryFriend;
    ParseQuery queryAlreadyFriends, queryAlreadyFriends2;
    ParseUser newFriend = null;
    boolean alreadyFriends = false, alreadySent = false, success = false;
    private ImageView inviteFriendButton;

    public SearchingFriendAdapter(Context context, ArrayList<Friend> items) {
        super(context, R.layout.search_friend_list_row, items);
        this.items = items;
        helpList = SessionManager.getUsersWithoutCurrentFriendsAndWithGrayAlmostFriends();
        commonMethods = new CommonMethods(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Friend friend = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_friend_list_row, parent, false);
        }
        if (position % 2 == 1) {
            convertView.setBackgroundResource(R.color.list_divider);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }
        inviteFriendButton = (ImageView) convertView.findViewById(R.id.inviteFriend);

        inviteFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendFriendshipRequest().execute(friend);
            }
        });
        if (friend.alreadyInvited) { // tutaj jakiś fajny warunek
            inviteFriendButton.setImageDrawable(convertView.getResources().getDrawable(R.drawable.plus_circle_gray));
            inviteFriendButton.setClickable(false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.txt);
        TextView email = (TextView) convertView.findViewById(R.id.userEmail);
        ImageView photo = (ImageView) convertView.findViewById(R.id.img);
        // Populate the data into the template view using the data object
        email.setText(friend.getEmail());
        name.setText(friend.getName());
        photo.setImageBitmap(commonMethods.decodeBase64ToBitmap(friend.getPhoto()));
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults result = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    result.values = helpList;
                    result.count = helpList.size();
                } else {

                    if (filteredList == null)
                        filteredList = new ArrayList<>();
                    else
                        filteredList.clear();

                    for (int a = 0; a < helpList.size(); a++) {
                        if (helpList.get(a).getName().toLowerCase().contains(constraint.toString().trim().toLowerCase()))
                            filteredList.add(helpList.get(a));
                    }

                    result.values = filteredList;
                    result.count = filteredList.size();
                }

                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dataSetChanged((List<Friend>) results.values);
            }
        };
        return filter;
    }

    public void dataSetChanged(List<Friend> values) {
        items.clear();
        items.addAll(values);
        notifyDataSetChanged();
    }

    private class SendFriendshipRequest extends AsyncTask<Friend, Void, Void> {

        String[] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Friend... params) {
            if (params[0] != null) {
                temp = addFriendship(params[0].getEmail());
                if (temp.length > 1 && temp[0].contains("Invitation sent to")) {
                    try {
                        sendFriendshipNotification(params[0].getEmail(), params[0].getName(), temp[1]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e("Wrong argument ", " in doInBackground: null");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(MainActivity.context, temp[0], Toast.LENGTH_LONG).show();
        }
    }

    private void sendFriendshipNotification(String email, String friendName, String friendshipId) throws JSONException {
        Log.e("do sendFriendshiNotif", "tak");
        String notificationId = null;
        try {
            ParseObject notific = new ParseObject("Notification");
            notific.put("senderEmail", ParseUser.getCurrentUser().getEmail());
            notific.put("receiverEmail", newFriend.getEmail());
            notific.put("kindOfNotification", 101);
            notific.put("markedAsRead", false);
            notific.put("extra", friendshipId);


            notific.save();
            notific.fetch();
            notificationId = notific.getObjectId();
            Log.e("Notification object: ", "saved succesfully in Parse");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseQuery notificationQuery = ParseInstallation.getQuery().whereEqualTo("name", email);
        ParsePush notification = new ParsePush();
        notification.setQuery(notificationQuery);
        notification.setMessage("Użytkownik " + ParseUser.getCurrentUser().get("name") + " wysłał Ci zaproszenie do znajomych!");

        JSONObject message = new JSONObject();
        message.put("kind_of_notification", 101);
        message.put("friend_email", email);
        message.put("friendship_id", friendshipId);
        message.put("new_friend_name", friendName);
        message.put("notification_id", notificationId);

        notification.setData(message);
        //notification.setExpirationTimeInterval(60 * 60 * 24 * 7); //1 week
        notification.sendInBackground();

        Log.e("z sendFriendshiNotif", "tak");
    }

    private String[] addFriendship(final String newFriendEmail) {
        String methodResult[] = null;
        success = false;
        alreadyFriends = false;
        alreadySent = false;

        if (newFriendEmail.equals(ParseUser.getCurrentUser().getEmail())) {
            methodResult = new String[]{"You can't be friend with yourself"};
        } else {

            ArrayList<Friend> tempFriendsList = SessionManager.getInstance().getFriendsList();
            List<ParseObject> arrayAlreadyFriends = null;
            List<ParseObject> arrayAlreadyFriends2 = null;

            if (queryAlreadyFriends != null) queryAlreadyFriends.clearCachedResult();
            if (queryAlreadyFriends2 != null) queryAlreadyFriends2.clearCachedResult();
            if (queryFriend != null) queryFriend.clearCachedResult();

            queryFriend = ParseUser.getQuery().whereEqualTo("email", newFriendEmail);

            queryAlreadyFriends = new ParseQuery("Friendship");
            queryAlreadyFriends.whereEqualTo("friend1", ParseUser.getCurrentUser());

            queryAlreadyFriends2 = new ParseQuery("Friendship");
            queryAlreadyFriends2.whereEqualTo("friend2", ParseUser.getCurrentUser());

            for (int i = 0; i < tempFriendsList.size(); i++) {
                if (tempFriendsList.get(i).getEmail().equals(newFriendEmail))
                    alreadyFriends = true;
            }

            List<ParseUser> list = null;
            try {
                list = queryFriend.find();

                if (list.size() > 0) {
                    newFriend = list.get(0).fetch();

                    arrayAlreadyFriends = queryAlreadyFriends.find();

                    if (arrayAlreadyFriends != null) {
                        for (int i = 0; i < arrayAlreadyFriends.size(); i++) {

                            if (newFriendEmail.equals(((ParseUser) (arrayAlreadyFriends.get(i)).get("friend2")).fetchIfNeeded().getEmail())) {
                                if ((arrayAlreadyFriends.get(i)).get("accepted").toString().equals("true"))
                                    alreadyFriends = true;
                                else
                                    alreadySent = true;
                            }
                        }
                    }

                    arrayAlreadyFriends2 = queryAlreadyFriends2.find();

                    if (arrayAlreadyFriends2 != null) {
                        for (int i = 0; i < arrayAlreadyFriends2.size(); i++) {

                            if (newFriendEmail.equals(((ParseUser) (arrayAlreadyFriends2.get(i)).get("friend1")).fetchIfNeeded().getEmail())) {
                                if ((arrayAlreadyFriends2.get(i)).get("accepted").toString().equals("true"))
                                    alreadyFriends = true;
                                else
                                    alreadySent = true;
                            }
                        }
                    }

                    if (!alreadyFriends && !alreadySent) {
                        ParseObject friendship = new ParseObject("Friendship");
                        friendship.put("friend1", ParseUser.getCurrentUser());
                        friendship.put("friend2", newFriend);
                        friendship.put("accepted", false);

                        friendship.save();
                        friendship.fetch();

                        success = true;
                        Log.e("Friendship: ", friendship.getObjectId());
                        methodResult = new String[]{("Invitation sent to " + newFriend.get("name").toString()), friendship.getObjectId()};
                    } else if (alreadySent)
                        methodResult = new String[]{"Invitation not responded yet"};
                }
            } catch (ParseException e) {
                e.getLocalizedMessage();
                e.printStackTrace();
            }
        }
        Log.e("ROZMIAR RESULTU: ", methodResult == null ? "NULL" : methodResult[0]);

        return methodResult;
    }
}