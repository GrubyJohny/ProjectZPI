package zpi.squad.app.grouploc.adapter;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.domain.Friend;

public class SearchingFriendAdapter extends ArrayAdapter<Friend> implements Filterable {
    private SessionManager session = SessionManager.getInstance();
    private ArrayList<Friend> items;
    private ParseQuery<ParseUser> query;
    private ParseUser temp;
    private ArrayList<Friend> alreadyFriendsList ;

    public SearchingFriendAdapter(Context context, ArrayList<Friend> items) {
        super(context, R.layout.search_friend_list_row, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Friend friend = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_friend_list_row, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.txt);
        ImageView photo = (ImageView) convertView.findViewById(R.id.img);
        // Populate the data into the template view using the data object
        name.setText(friend.getName());
        photo.setImageBitmap(decodeBase64ToBitmap(friend.getPhoto()));
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults result = new FilterResults();
                List<Friend> allFriends = session.getAllUsersFromParse();
                if (constraint == null || constraint.length() == 0) {
                    result.values = allFriends;
                    result.count = allFriends.size();
                } else {

                    final ArrayList<Friend> filteredList = new ArrayList<Friend>();

                    try {
                            query = ParseUser.getQuery();
                            query.whereContains("name_lowercase", constraint.toString().trim().toLowerCase());
                            query.whereNotEqualTo("name_lowercase", ParseUser.getCurrentUser().get("name_lowercase"));
                            query.clearCachedResult();

                            alreadyFriendsList = SessionManager.getInstance().getFriendsList();

                            List<ParseUser> list = query.find();

                            for (int i = 0; i < list.size(); i++) {
                                boolean alreadyIsFriend = false;
                                temp = list.get(i);

                                for (Friend f : alreadyFriendsList)
                                    if (f.getEmail().equals(temp.getEmail()))
                                        alreadyIsFriend = true;

                                if (!alreadyIsFriend)
                                    filteredList.add(new Friend(temp.getObjectId(), temp.get("name").toString(), temp.getEmail(), (temp.get("photo").toString()), ((ParseGeoPoint) temp.get("location")).getLatitude(), ((ParseGeoPoint) temp.get("location")).getLongitude()));

                            }

                    } catch (Exception e) {
                        e.getLocalizedMessage();
                        e.printStackTrace();
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

    public Bitmap decodeBase64ToBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}