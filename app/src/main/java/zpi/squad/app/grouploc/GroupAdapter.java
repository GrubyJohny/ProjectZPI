package zpi.squad.app.grouploc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class GroupAdapter extends ArrayAdapter<GroupList> {

    public GroupAdapter(Context context, List<GroupList> items) {
        super(context, R.layout.grouplist_item, items);
    }

    @Override
    public void add(GroupList object) {
        super.add(object);
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if(view == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.grouplist_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.groupName = (TextView) view.findViewById(R.id.groupName);
            viewHolder.date = (TextView) view.findViewById(R.id.groupDate);
            viewHolder.adminName = (TextView) view.findViewById(R.id.groupAdmin);

            view.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) view.getTag();
        }

        GroupList item = getItem(position);
        viewHolder.groupName.setText(item.groupName);
        viewHolder.date.setText(item.date);
        viewHolder.adminName.setText(item.adminName);
        return view;
    }

    private static class ViewHolder {
        TextView groupName;
        TextView date;
        TextView adminName;
    }
}
