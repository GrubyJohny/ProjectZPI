package zpi.squad.app.grouploc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FriendList extends ArrayAdapter<ListViewItem> {


    public FriendList(Context context, List<ListViewItem> items) {
        super(context, R.layout.one_row, items);
        Log.d("TERAZ", "Fragment z listView");
    }

    @Override
    public void add(ListViewItem object) {
        super.add(object);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if(view == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.one_row, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.img);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.txt);
            viewHolder.emailText = (TextView) view.findViewById(R.id.emailTXT);

            view.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) view.getTag();
        }

        ListViewItem item = getItem(position);
        viewHolder.ivIcon.setImageDrawable(item.image);
        viewHolder.tvTitle.setText(item.name);
        viewHolder.emailText.setText(item.email);




        /*LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.one_row, parent, false);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web[position]);
        imageView.setImageResource(imageId[position]);*/

        return view;
    }

    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView emailText;
    }
}