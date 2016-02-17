package zpi.squad.app.grouploc.activity;

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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
    ArrayList<Friend> searchFriendsList = new ArrayList<>();
    ParseQuery<ParseUser> query, queryFriend;
    ParseQuery queryAlreadyFriends, queryAlreadyFriends2;
    ParseUser temp, newFriend = null;
    boolean alreadyFriends = false, alreadySent = false;
    ArrayList<Friend> alreadyFriendsList ;
    ArrayList<String> excludedFriendsNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_friends);

        adapter = new SearchingFriendAdapter(this, searchFriendsList);
        searchFriendListView = (ListView) findViewById(R.id.searchingFriendsListView);
        searchFriendListView.setAdapter(adapter);
        searchFriendListView.setTextFilterEnabled(true);

        searchFriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Friend item;

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                item = (Friend) searchFriendListView.getItemAtPosition(position);

                try {
                    addFriendship(item.getEmail());
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

                searchFriendsList.clear();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

                try {

                    if(s.toString().trim().length()>1) {

                        searchFriendsList.clear();
                        adapter.notifyDataSetChanged();


                        query = ParseUser.getQuery();
                        query.whereContains("name_lowercase", s.toString().trim().toLowerCase());
                        query.whereNotEqualTo("name_lowercase", ParseUser.getCurrentUser().get("name_lowercase"));

                        alreadyFriendsList = SessionManager.getInstance().getFriendsList();
                            /*for(int i=0; i<alreadyFriendsList.size(); i++) {
                                query.whereNotEqualTo("name_lowercase", alreadyFriendsList.get(i).getName().toLowerCase());
                            }*/


                        query.clearCachedResult();

                        query.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> list, ParseException e) {
                                searchFriendsList.clear();
                                adapter.notifyDataSetChanged();

                                if (e == null) {


                                    for (int i = 0; i < list.size(); i++) {
                                        boolean alreadyIsFriend = false;
                                        temp = list.get(i);

                                        for (Friend f : alreadyFriendsList)
                                            if (f.getEmail().equals(temp.getEmail()))
                                                alreadyIsFriend = true;

                                        if (!alreadyIsFriend)
                                            searchFriendsList.add(new Friend(temp.getObjectId(), temp.get("name").toString(), temp.getEmail(), (temp.get("photo").toString()), ((ParseGeoPoint) temp.get("location")).getLatitude(), ((ParseGeoPoint) temp.get("location")).getLongitude()));

                                        adapter.notifyDataSetChanged();
                                    }


                                } else {
                                    e.getLocalizedMessage();
                                    e.printStackTrace();
                                }

                            }
                        });

                    }
                    else
                    {
                        searchFriendsList.clear();
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                adapter.getFilter().filter(s, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {

                        Log.e("FILTER COMPLETE: ","count= " + count );

                    }
                });
            }
        });
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        View empty = findViewById(R.id.emptyTextInSearching);
        ListView list = (ListView) findViewById(R.id.searchingFriendsListView);
        list.setEmptyView(empty);
    }

    private void addFriendship(final String newFriendEmail) {
        alreadyFriends = false;
        alreadySent = false;

        if (newFriendEmail.equals(ParseUser.getCurrentUser().getEmail())) {
            Toast.makeText(getApplicationContext(), "You can't be friend with yourself", Toast.LENGTH_LONG).show();
        } else {

            ArrayList<Friend> tempFriendsList = SessionManager.getInstance().getFriendsList();

            for (int i = 0; i < tempFriendsList.size(); i++) {
                if (tempFriendsList.get(i).getEmail().equals(newFriendEmail))
                    alreadyFriends = true;
            }

            if (alreadyFriends) {
                Toast.makeText(getApplicationContext(), "You are already friends!", Toast.LENGTH_SHORT).show();
            } else {

                //tutaj można wystartować kółko

                if (queryAlreadyFriends != null) queryAlreadyFriends.clearCachedResult();
                if (queryAlreadyFriends2 != null) queryAlreadyFriends2.clearCachedResult();
                if (queryFriend != null) queryFriend.clearCachedResult();

                queryFriend = ParseUser.getQuery().whereEqualTo("email", newFriendEmail);

                queryAlreadyFriends = new ParseQuery("Friendship");
                queryAlreadyFriends.whereEqualTo("friend1", ParseUser.getCurrentUser());

                queryAlreadyFriends2 = new ParseQuery("Friendship");
                queryAlreadyFriends2.whereEqualTo("friend2", ParseUser.getCurrentUser());

                queryFriend.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> list, ParseException e) {

                        if (list.size() > 0) {

                            try {
                                newFriend = list.get(0).fetch();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }

                            queryAlreadyFriends.findInBackground(new FindCallback() {
                                @Override
                                public void done(List list, ParseException e) {
                                }

                                @Override
                                public void done(Object o, Throwable throwable) {

                                    ArrayList<ParseObject> arrayAlreadyFriends = (ArrayList<ParseObject>) o;

                                    if (arrayAlreadyFriends != null) {
                                        for (int i = 0; i < arrayAlreadyFriends.size(); i++) {
                                            try {
                                                if (newFriendEmail.equals(((ParseUser) (arrayAlreadyFriends.get(i)).get("friend2")).fetchIfNeeded())) {
                                                    if ((arrayAlreadyFriends.get(i)).get("accepted").toString().equals("true"))
                                                        alreadyFriends = true;
                                                    else
                                                        alreadySent = true;
                                                }


                                            } catch (ParseException e1) {
                                                e1.getLocalizedMessage();
                                                e1.printStackTrace();
                                            }
                                        }

                                    }

                                    queryAlreadyFriends2.findInBackground(new FindCallback() {
                                        @Override
                                        public void done(List list, ParseException e) {
                                        }

                                        @Override
                                        public void done(Object o, Throwable throwable) {

                                            ArrayList<ParseObject> arrayAlreadyFriends2 = (ArrayList<ParseObject>) o;

                                            if (arrayAlreadyFriends2 != null) {
                                                for (int i = 0; i < arrayAlreadyFriends2.size(); i++) {
                                                    try {
                                                        if (newFriendEmail.equals(((ParseUser) (arrayAlreadyFriends2.get(i)).get("friend1")).fetchIfNeeded())) {
                                                            if ((arrayAlreadyFriends2.get(i)).get("accepted").toString().equals("true"))
                                                                alreadyFriends = true;
                                                            else
                                                                alreadySent = true;
                                                        }

                                                    } catch (ParseException e1) {
                                                        e1.getLocalizedMessage();
                                                        e1.printStackTrace();
                                                    }

                                                }
                                            }


                                            if (!alreadyFriends && !alreadySent) {
                                                ParseObject friendship = new ParseObject("Friendship");
                                                friendship.put("friend1", ParseUser.getCurrentUser());
                                                friendship.put("friend2", newFriend);
                                                /* *//**//*TUTAJ OCZYWISCIE
                                                * TRZEBA ZMIENIC
                                                * NA FALSE
                                                * I ZAMIAST TEGO
                                                * WYSYLAC POWIADOMIENIE*//**//**/
                                                friendship.put("accepted", true);
                                                friendship.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {

                                                        //a tu jest optymistyczna droga zakończenia - usunięcie kółka
                                                        //reszta przypadków powinna się kończyć w miarę szybko

                                                        Log.e("Friendship: ", "saved");
                                                        Toast.makeText(getApplicationContext(), "Invitation sent to " + newFriend.get("name").toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                            } else if (alreadySent)
                                                Toast.makeText(getApplicationContext(), "You've already sent invitation to this user", Toast.LENGTH_LONG).show();
                                            else
                                                Toast.makeText(getApplicationContext(), "You are already friends", Toast.LENGTH_LONG).show();


                                        }
                                    });


                                }
                            });


                        }


                    }
                });


            }
        }

    }
}