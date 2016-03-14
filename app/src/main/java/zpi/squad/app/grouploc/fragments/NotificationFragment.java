package zpi.squad.app.grouploc.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import zpi.squad.app.grouploc.adapters.NotificationAdapter;
import zpi.squad.app.grouploc.domains.Notification;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class NotificationFragment extends Fragment {

    ListView notificationListView;
    public static ArrayList<Notification> notificationsList;
    public static NotificationAdapter adapter;
    ConfirmFriendship confirm = new ConfirmFriendship();
    SendFriendshipAcceptanceNotification sendAcceptNotif = new SendFriendshipAcceptanceNotification();
    private Activity actualActivity;
    private SessionManager session = SessionManager.getInstance();
    CommonMethods commonMethods;

    public NotificationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).setActionBarTitle("Notifications");

        commonMethods = new CommonMethods(getActivity().getApplicationContext());

        notificationsList = SessionManager.getInstance().getNotificationsList();

        adapter = new NotificationAdapter(getActivity().getApplicationContext(), notificationsList);

        notificationListView = (ListView) getActivity().findViewById(R.id.notificationsListView);
        notificationListView.setAdapter(adapter);

        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                if (!((Notification) adapter.getItem(position)).isMarkedAsRead()) {
                    if ((((Notification) adapter.getItem(position)).getType() == 101)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder
                                //no i tutaj też to trzeba jeszcze rozbić kiedyś na typy powiadomień
                                .setTitle("Friendship request")
                                .setMessage("Do you want to accept invitation from " + ((Notification) adapter.getItem(position)).getSenderEmail())
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        confirm.execute(((Notification) adapter.getItem(position)).getMessage());
                                        sendAcceptNotif.execute(((Notification) adapter.getItem(position)).getMessage());

                                        commonMethods.reloadFriendsData(MainActivity.adapter);
                                        notificationsList.get(position).setMarkedAsRead(true);
                                        new MarkAsReadNotification().execute(((Notification) adapter.getItem(position)).getNotificationId());
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            }
        });

        DownloadPhotos downloadPhotos = new DownloadPhotos();
        downloadPhotos.execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        actualActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class MarkAsReadNotification extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Notification");
            query.whereEqualTo("objectId", params[0]);

            try {
                System.out.println(query.count());
                List<ParseObject> res = query.find();
                if (res.size() > 0) {
                    res.get(0).fetch().put("markedAsRead", true);
                    res.get(0).save();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class ConfirmFriendship extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendship");
            query.whereEqualTo("objectId", params[0]);

            try {
                List<ParseObject> res = query.find();
                if (res.size() > 0) {
                    res.get(0).fetch().put("accepted", true);
                    res.get(0).save();
                    Log.e("ZAPROSZENIE ", "PRZYJĘTE!");
                    //albo tutaj
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(actualActivity.getApplicationContext(), "Friend added!", Toast.LENGTH_LONG).show();
            commonMethods.reloadFriendsData(MainActivity.adapter);
            Log.e("FRIEND ADDED", "SUCCESSFULLY!");
        }
    }

    private void reloadNotificationsData() {
        session.refreshNotificationsList();
        List<Notification> objects = session.getNotificationsList();
        adapter.clear();
        adapter.addAll(objects);
        adapter.notifyDataSetChanged();
    }

    class SendFriendshipAcceptanceNotification extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendship");
            query.whereEqualTo("objectId", params[0]);
            Log.e("MOZE TU", " WEJdZIE");
            try {
                List<ParseObject> res = query.find();
                if (res.size() > 0) {
                    String notifReceiver = "";
                    //String frinedshipId= res.get(0).fetch().getObjectId();
                    Log.e("ID OBDZEKTU: ", ((ParseObject) res.get(0).fetch().get("friend1")).getObjectId());
                    if (((ParseObject) res.get(0).fetch().get("friend1")).getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                        notifReceiver = ((ParseObject) res.get(0).fetch().get("friend2")).getString("email");
                    else
                        notifReceiver = ((ParseObject) res.get(0).fetch().getParseUser("friend1")).fetchIfNeeded().getString("email");

                    sendFriendshipAcceptanceNotification(notifReceiver, ParseUser.getCurrentUser().getString("name"), params[0]);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendFriendshipAcceptanceNotification(String email, String friendName, String friendshipId) throws JSONException {
        String notificationId = null;

        try {
            ParseObject notific = new ParseObject("Notification");
            notific.put("senderEmail", ParseUser.getCurrentUser().getEmail());
            notific.put("senderName", session.getUserName());
            notific.put("receiverEmail", email);
            notific.put("kindOfNotification", 102);
            notific.put("markedAsRead", false);
            notific.put("extra", friendshipId);


            notific.save();
            notific.fetch();
            notificationId = notific.getObjectId();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseQuery notificationQuery = ParseInstallation.getQuery().whereEqualTo("name", email);
        ParsePush notification = new ParsePush();
        notification.setQuery(notificationQuery);
        notification.setMessage("Użytkownik " + ParseUser.getCurrentUser().get("name") + " zaakceptował twoje zaproszenie do grona znajomych!");

        JSONObject message = new JSONObject();
        message.put("kind_of_notification", 102);
        message.put("friend_email", email);
        message.put("friendship_id", friendshipId);
        message.put("origin_sender_name", ParseUser.getCurrentUser().get("name"));
        message.put("notification_id", notificationId);

        notification.setData(message);
        //notification.setExpirationTimeInterval(60 * 60 * 24 * 7); //1 week
        notification.sendInBackground();
    }

    class DownloadPhotos extends AsyncTask<String, Void, Void> {

        int pobranezdjecia = 0;

        @Override
        protected Void doInBackground(String... params) {


            for (int i = 0; i < notificationsList.size(); i++) {
                try {
                    notificationsList.get(i).setPhoto(commonMethods.decodeBase64ToBitmap((String) (ParseUser.getQuery().whereEqualTo("email", ((Notification) notificationsList.get(i)).getSenderEmail()).find()).get(0).fetch().get("photo")));
                    pobranezdjecia++;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e("POBRANE ZDJECIA: ", "" + pobranezdjecia);

            adapter.notifyDataSetChanged();

        }
    }
}
