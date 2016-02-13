package zpi.squad.app.grouploc.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.adapter.SearchingFriendAdapter;
import zpi.squad.app.grouploc.domain.Friend;

public class SearchingFriendsActivity extends AppCompatActivity {
    EditText searchFriendInput;
    ListView searchFriendListView;
    private SearchingFriendAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_friends);

        ArrayList<Friend> searchFriendsList = new ArrayList<>();

        adapter = new SearchingFriendAdapter(this, searchFriendsList);
        searchFriendListView = (ListView) findViewById(R.id.searchingFriendsListView);
        searchFriendListView.setAdapter(adapter);
        searchFriendListView.setTextFilterEnabled(true);

        searchFriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Friend item;

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                item = (Friend) searchFriendListView.getItemAtPosition(position);

                // TUTAJ ZROB MECHANIZM DODAWANIA :)

                Toast.makeText(getApplicationContext(), "You added: " + item.getName(), Toast.LENGTH_LONG).show();
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
}
