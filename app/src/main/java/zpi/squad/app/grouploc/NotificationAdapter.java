package zpi.squad.app.grouploc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Slawek on 2015-05-15.
 */
public class NotificationAdapter extends ArrayAdapter {

    View notificationLayout;

    public NotificationAdapter(Context context, List<Notification> items) {
        super(context, R.layout.notification_item, items);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.notification_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.notificationTextView = (TextView) convertView.findViewById(R.id.notificationTextView);
            viewHolder.notificationDate = (TextView) convertView.findViewById(R.id.notificationDate);
            viewHolder.notificationImg = (ImageView) convertView.findViewById(R.id.notificationImg);

            notificationLayout = convertView.findViewById(R.id.notificationLayout);

            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Notification item = (Notification) getItem(position);
        viewHolder.notificationTextView.setText(item.toString());
        viewHolder.notificationDate.setText(item.getCreatedAt());
        viewHolder.notificationImg.setImageResource(R.drawable.image3);
        /*if(item.isChecked())
            viewHolder.notificationLayout.setBackgroundResource(R.color.material_blue_grey_900);*/
        if(item.isChecked())
            notificationLayout.setBackgroundResource(R.color.material_blue_grey_900);
        return convertView;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        return getDropDownView(position,view,parent);
    }

    private static class ViewHolder {
        TextView notificationTextView;
        TextView notificationDate;
        ImageView notificationImg;
    }
}
