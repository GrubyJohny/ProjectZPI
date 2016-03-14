package zpi.squad.app.grouploc.activities;

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

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.adapters.SearchingFriendAdapter;
import zpi.squad.app.grouploc.domains.Friend;

public class SearchingFriendsActivity extends AppCompatActivity {
    EditText searchFriendInput;
    ListView searchFriendListView;
    public static SearchingFriendAdapter adapter;
    ArrayList<Friend> searchFriendsList;
    private SessionManager session = SessionManager.getInstance();
    View empty;


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
                        Log.e("FILTER COMPLETE: ", "count = " + count);
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

    private class FillTheList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            searchFriendsList.clear();
            List<Friend> all = session.getAllUsersFromParseWithoutCurrentAndFriends();
            List<Friend> notAccepted = SessionManager.getNotAcceptedFriendsFromParse();
            boolean notAcceptedAlready;
            for (int i = 0; i < all.size(); i++) {
                notAcceptedAlready = false;
                for (int j = 0; j < notAccepted.size(); j++) {
                    if (all.get(i).getEmail().equals(notAccepted.get(j).getEmail())) {
                        all.get(i).alreadyInvited = true;
                    }
                }

                searchFriendsList.add(all.get(i));

            }
            //searchFriendsList.addAll(session.getAllUsersFromParseWithoutCurrentAndFriends());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter.notifyDataSetChanged();
        }
    }
}