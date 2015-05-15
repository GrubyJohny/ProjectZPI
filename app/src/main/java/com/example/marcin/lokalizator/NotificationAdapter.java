package com.example.marcin.lokalizator;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.SQLOutput;
import java.util.List;

/**
 * Created by Slawek on 2015-05-15.
 */
public class NotificationAdapter extends ArrayAdapter<Notification> {


    public NotificationAdapter(Context context, List<Notification> items) {
        super(context, android.R.layout.simple_spinner_dropdown_item, items);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if(view == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.notification_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.notificationTextView = (TextView) view.findViewById(R.id.notificationTextView);
            viewHolder.notificationDate = (TextView) view.findViewById(R.id.notificationDate);

            view.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) view.getTag();
        }

        Notification item = getItem(position);
        viewHolder.notificationTextView.setText(item.toString());
        viewHolder.notificationDate.setText(item.getCreatedAt());
        /*if(item.isChecked())
            viewHolder.notificationTextView.setBackgroundResource(R.color.material_blue_grey_900);*/
        return view;
    }

    private static class ViewHolder {
        TextView notificationTextView;
        TextView notificationDate;
    }
}
