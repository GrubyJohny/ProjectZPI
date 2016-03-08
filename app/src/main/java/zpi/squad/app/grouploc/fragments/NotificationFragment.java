package zpi.squad.app.grouploc.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.activities.MainActivity;
import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.adapters.NotificationAdapter;
import zpi.squad.app.grouploc.domains.Notification;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class NotificationFragment extends Fragment {

    ListView notificationListView;
    public static ArrayList<Notification> notificationsList;
    public static NotificationAdapter adapter;
    ConfirmFriendship confirm = new ConfirmFriendship();
    private Activity actualActivity;
    private SessionManager session = SessionManager.getInstance();
    CommonMethods commonMethods;

    public NotificationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commonMethods = new CommonMethods(getActivity().getApplicationContext());

        notificationsList = SessionManager.getInstance().getNotificationsList();

        adapter = new NotificationAdapter(getActivity().getApplicationContext(), notificationsList);
        notificationListView = (ListView) getActivity().findViewById(R.id.notificationsListView);
        notificationListView.setAdapter(adapter);

        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                if (!((Notification) adapter.getItem(position)).isMarkedAsRead()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder
                            .setTitle("Friendship request")
                            .setMessage("Do you want to accept invitation from " + ((Notification) adapter.getItem(position)).getSenderEmail())
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    confirm.execute(((Notification) adapter.getItem(position)).getMessage());
                                    //tutaj jeszcze powiadomienie a zaakceptowaniu zaproszenia

                                    commonMethods.reloadFriendsData(MainActivity.adapter);
                                    notificationsList.get(position).setMarkedAsRead(true);
                                    new MarkAsReadNotification().execute(((Notification) adapter.getItem(position)).getNotificationId());
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        actualActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class MarkAsReadNotification extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Notification");
            query.whereEqualTo("objectId", params[0]);

            try {
                System.out.println(query.count());
                List<ParseObject> res = query.find();
                if (res.size() > 0) {
                    res.get(0).fetch().put("markedAsRead", true);
                    res.get(0).save();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ConfirmFriendship extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendship");
            query.whereEqualTo("objectId", params[0]);

            try {
                List<ParseObject> res = query.find();
                if (res.size() > 0) {
                    res.get(0).fetch().put("accepted", true);
                    res.get(0).save();
                    Log.e("ZAPROSZENIE ", "PRZYJĘTE!");
                    //albo tutaj
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(actualActivity.getApplicationContext(), "Friend added!", Toast.LENGTH_LONG).show();
            commonMethods.reloadFriendsData(MainActivity.adapter);
            Log.e("FRIEND ADDED", "SUCCESSFULLY!");
        }
    }

    private void reloadNotificationsData() {
        session.refreshNotificationsList();
        List<Notification> objects = session.getNotificationsList();
        adapter.clear();
        adapter.addAll(objects);
        adapter.notifyDataSetChanged();
    }
}
