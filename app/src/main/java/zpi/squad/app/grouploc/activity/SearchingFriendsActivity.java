package zpi.squad.app.grouploc.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.adapter.SearchingFriendAdapter;
import zpi.squad.app.grouploc.domain.Friend;

public class SearchingFriendsActivity extends AppCompatActivity {
    EditText searchFriendInput;
    ListView searchFriendListView;
    private SearchingFriendAdapter adapter;
    ArrayList<Friend> searchFriendsList;
    ParseQuery<ParseUser> query, queryFriend;
    ParseQuery queryAlreadyFriends, queryAlreadyFriends2;
    ParseUser newFriend = null;
    boolean alreadyFriends = false, alreadySent = false, success = false;
    private SessionManager session = SessionManager.getInstance();
    View empty;
    private int selectedItem = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_friends);

        searchFriendsList = new ArrayList<>();

        adapter = new SearchingFriendAdapter(this, searchFriendsList);
        searchFriendListView = (ListView) findViewById(R.id.searchingFriendsListView);
        searchFriendListView.setAdapter(adapter);
        searchFriendListView.setTextFilterEnabled(true);

        searchFriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Friend item;

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                item = (Friend) searchFriendListView.getItemAtPosition(position);

                try {
                    selectedItem = position;
                    new SendFriendshipRequest().execute(item);
                } catch (Exception e) {
                    e.getLocalizedMessage();
                    e.printStackTrace();
                }
            }
        });

        searchFriendInput = (EditText) findViewById(R.id.search_friends_input);
        searchFriendInput.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {

                        Log.e("FILTER COMPLETE: ", "count= " + count);

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        FillTheList fill = new FillTheList();
        fill.execute();
    }


    @Override
    public void onContentChanged() {
        super.onContentChanged();

        empty = findViewById(R.id.emptyTextInSearching);
        ListView list = (ListView) findViewById(R.id.searchingFriendsListView);
        list.setEmptyView(empty);
    }

    private String addFriendship(final String newFriendEmail) {
        String methodResult = "";
        success = false;
        alreadyFriends = false;
        alreadySent = false;

        if (newFriendEmail.equals(ParseUser.getCurrentUser().getEmail())) {
            methodResult = "You can't be friend with yourself";
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
                        success = true;
                        Log.e("Friendship: ", "saved");
                        methodResult = "Invitation sent to " + newFriend.get("name").toString();

                    } else if (alreadySent)
                        methodResult = "You've already sent invitation to " + newFriend.get("name").toString();
                }
            } catch (ParseException e) {
                e.getLocalizedMessage();
                e.printStackTrace();
            }

        }

        return methodResult;
    }


    private void sendFriendshipNotification(String email) {
        ParseQuery notificationQuery = ParseInstallation.getQuery().whereEqualTo("name", email);
        ParsePush notification = new ParsePush();
        notification.setQuery(notificationQuery);
        notification.setMessage("Użytkownik " + ParseUser.getCurrentUser().get("name") + " wysłał Ci zaproszenie do znajomych!");
        notification.sendInBackground();
    }

    private class FillTheList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            searchFriendsList.clear();
            searchFriendsList.addAll(session.getAllUsersFromParseWithoutCurrentAndFriends());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter.notifyDataSetChanged();
        }
    }

    private class SendFriendshipRequest extends AsyncTask<Friend, Void, Void> {
        boolean correct;
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Friend... params) {
            if (params[0] != null) {
                message = addFriendship(params[0].getEmail());
                if (message.contains("Invitation sent to")) {
                    sendFriendshipNotification(params[0].getEmail());
                    correct = true;
                }

            } else
                Log.e("Wrong argument ", " in doInBackground: null");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}