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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.adapter.SearchingFriendAdapter;
import zpi.squad.app.grouploc.domain.Friend;

public class SearchingFriendsActivity extends AppCompatActivity {
    EditText searchFriendInput;
    ListView searchFriendListView;
    private SearchingFriendAdapter adapter;
    ArrayList<Friend> searchFriendsList = new ArrayList<>();
    ParseQuery<ParseUser> query;
    ParseUser temp;

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
                    Toast.makeText(getApplicationContext(), "You added: " + item.getName(), Toast.LENGTH_LONG).show();
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

                query = ParseUser.getQuery();
                query.whereContains("name", s.toString().trim());

                adapter.getFilter().filter(s, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {
                        try {


                            query.findInBackground(new FindCallback<ParseUser>() {
                                @Override
                                public void done(List<ParseUser> list, ParseException e) {

                                    if (e == null) {
                                        searchFriendsList.clear();
                                        for (int i = 0; i < list.size(); i++) {
                                            temp = (ParseUser) list.get(i);
                                            searchFriendsList.add(new Friend(temp.getObjectId(), temp.get("name").toString(), temp.getEmail(), (temp.get("photo").toString()), ((ParseGeoPoint) temp.get("location")).getLatitude(), ((ParseGeoPoint) temp.get("location")).getLongitude()));
                                        }
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        e.getLocalizedMessage();
                                        e.printStackTrace();
                                    }

                                }
                            });


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

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

    private void addFriendship(String friendEmail) {
        ParseQuery queryGetUserByEmail = new ParseQuery(new ParseUser().getClassName());
        queryGetUserByEmail.whereEqualTo("email", friendEmail);
        ParseObject friend = null;
        try {
            friend = queryGetUserByEmail.getFirst();

            if (friend != null) {
                ParseQuery checkIfAlreadyExsists = new ParseQuery("Friendship");
                checkIfAlreadyExsists.whereEqualTo("friend1", ParseUser.getCurrentUser());
                ParseQuery checkIfAlreadyExsists2 = new ParseQuery("Friendship");
                checkIfAlreadyExsists2.whereEqualTo("friend2", ParseUser.getCurrentUser());

                Object[] friendsList = null, friendsList2 = null;
                boolean alreadyFriends = false, alreadySent = false;
                ParseObject temp = null;

                friendsList = checkIfAlreadyExsists.find().toArray().clone();

                if (friendsList.length > 0) {
                    for (int i = 0; i < friendsList.length; i++) {
                        //to jest typu Friendship
                        temp = ((ParseObject) friendsList[i]);

                        if (((ParseUser) temp.get("friend2")).fetchIfNeeded().getEmail().equals(friendEmail)) {
                            if (temp.get("accepted").toString().equals("true")) {
                                alreadyFriends = true;
                            } else {
                                alreadySent = true;
                            }
                        }
                    }

                }


                friendsList2 = checkIfAlreadyExsists2.find().toArray().clone();

                if (friendsList2.length > 0) {
                    for (int i = 0; i < friendsList2.length; i++) {
                        //to jest typu Friendship
                        temp = ((ParseObject) friendsList2[i]);

                        if (((ParseUser) temp.get("friend1")).fetchIfNeeded().getEmail().equals(friendEmail)) {
                            if (temp.get("accepted").toString().equals("true")) {
                                alreadyFriends = true;
                            } else {
                                alreadySent = true;
                            }
                        }

                    }
                }


                if (!alreadyFriends && !alreadySent) {
                    ParseObject friendship = new ParseObject("Friendship");
                    friendship.put("friend1", ParseUser.getCurrentUser());
                    friendship.put("friend2", friend);
                    /*TUTAJ OCZYWISCIE
                    * TRZEBA ZMIENIC
                    * NA FALSE
                    * I ZAMIAST TEGO
                    * WYSYLAC POWIADOMIENIE*/
                    friendship.put("accepted", true);
                    friendship.saveInBackground();
                    Toast.makeText(getApplicationContext(), "Invitation sent", Toast.LENGTH_LONG).show();
                } else if (alreadySent)
                    Toast.makeText(getApplicationContext(), "You've already sent invitation to this user", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "You are already friends", Toast.LENGTH_LONG).show();

            } else
                Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            if (e.getMessage().equals("no results found for query")) {
                Toast.makeText(getApplicationContext(), "No such user", Toast.LENGTH_LONG).show();
            } else {
                Log.e("SendFriendShipRequest", e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            Log.e("SendFriendShipRequest", e.getMessage());
            e.printStackTrace();

        }
    }
}
