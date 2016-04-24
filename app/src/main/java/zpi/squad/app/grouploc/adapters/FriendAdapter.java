package zpi.squad.app.grouploc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.domains.Friend;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class FriendAdapter extends ArrayAdapter<Friend> implements Filterable {
    private SessionManager session = SessionManager.getInstance();
    private ArrayList<Friend> items;
    private CommonMethods commonMethods;

    public FriendAdapter(Context context, ArrayList<Friend> items) {
        super(context, R.layout.friend_list_row, items);
        this.items = items;
        commonMethods = new CommonMethods(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Friend friend = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_list_row, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.txt);
        TextView streetAndDistance = (TextView) convertView.findViewById(R.id.streetAndDistance);
        ImageView photo = (ImageView) convertView.findViewById(R.id.img);
        // Populate the data into the template view using the data object
        //wpakowałem to do tej samej linijki, co nazwa użytkownika, ale pewnie jakoś to już ładnie porozbijasz ;)
        Double distanceToMe = new ParseGeoPoint(session.getCurrentLocation().latitude, session.getCurrentLocation().longitude).distanceInKilometersTo(friend.getLocation());
        streetAndDistance.setText(distanceToMe.intValue() + " km " + "(" + friend.getLocationName() + " )");
        String text = friend.getName();
        name.setText(text);
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
                List<Friend> allFriends = session.getFriendsList();
                if (constraint == null || constraint.length() == 0) {
                    result.values = allFriends;
                    result.count = allFriends.size();
                } else {
                    ArrayList<Friend> filteredList = new ArrayList<Friend>();
                    for (Friend f : allFriends) {
                        if (f.getName().toLowerCase().contains(constraint.toString().toLowerCase()))
                            filteredList.add(f);
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
}