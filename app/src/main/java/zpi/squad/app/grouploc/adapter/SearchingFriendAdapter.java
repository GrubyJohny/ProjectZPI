package zpi.squad.app.grouploc.adapter;

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

import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.domain.Friend;

public class SearchingFriendAdapter extends ArrayAdapter<Friend> implements Filterable {
    private final ArrayList<Friend> helpList;
    private SessionManager session = SessionManager.getInstance();
    private ArrayList<Friend> items;
    private ArrayList<Friend> filteredList = new ArrayList<>();
    private ParseQuery<ParseUser> query;
    private ParseUser temp;
    //private ArrayList<Friend> alreadyFriendsList ;
    boolean alreadyIsFriend = false;
    List<ParseUser> usersFromQuery;

    public SearchingFriendAdapter(Context context, ArrayList<Friend> items) {
        super(context, R.layout.search_friend_list_row, items);
        this.items = items;
        helpList = session.getAllUsersFromParseWithoutCurrentAndFriends();

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

                if (constraint == null || constraint.length() == 0) {
                    result.values = helpList;
                    result.count = helpList.size();
                } else {

                    if (filteredList == null)
                        filteredList = new ArrayList<>();
                    else
                        filteredList.clear();

                    for (int a = 0; a < helpList.size(); a++)
                        if (helpList.get(a).getName().toLowerCase().contains(constraint.toString().trim().toLowerCase()))
                            filteredList.add(helpList.get(a));


                   /* try {
                        query = ParseUser.getQuery();
                        query.whereContains("name_lowercase", constraint.toString().trim().toLowerCase());

                        usersFromQuery = query.find();        //lista wszystkich userów spełniających warunki query - czyli nazwa pasująca do wpisanego tekstu

                        for (int i = 0; i < usersFromQuery.size(); i++) {
                            alreadyIsFriend = false;        //czy aktualnie jest już w znajomych, bo jeśli tak, to nie powinien się znaleźć w "filteredList"
                            temp = usersFromQuery.get(i).fetchIfNeeded();                     // i-ty element listy wynikwoej query

                            for (Friend f : helpList)
                                if (f.getEmail().equals(temp.getEmail()))   //jeśli i-ty element list wynikowej query znajduje się już na liście znajomych
                                    alreadyIsFriend = true;                 //to znaczy, że jest już naszym znajomym

                            if (!alreadyIsFriend)                           //jeśli nie, trafia do listy filteredList
                                filteredList.add(new Friend(temp.getObjectId(), temp.get("name").toString(), temp.getEmail(), (temp.get("photo").toString()), ((ParseGeoPoint) temp.get("location")).getLatitude(), ((ParseGeoPoint) temp.get("location")).getLongitude()));

                        }

                    } catch (Exception e) {
                        e.getLocalizedMessage();
                        e.printStackTrace();
                    }*/


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