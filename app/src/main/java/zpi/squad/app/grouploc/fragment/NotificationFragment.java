package zpi.squad.app.grouploc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.adapter.NotificationAdapter;
import zpi.squad.app.grouploc.domain.Notification;

public class NotificationFragment extends Fragment {

    ListView notificationListView;
    ArrayList<Notification> notificationsList;
    private NotificationAdapter adapter;

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

        notificationsList = new ArrayList<>();
        notificationsList.add(new Notification("to", "jest", "pierwsze", "probne", "piekne", "i", "swietne", "powiadomienie", 0));
        notificationsList.add(new Notification("to", "jest", "pierwsze", "probne", "piekne", "i", "swietne", "powiadomienie", 0));
        notificationsList.add(new Notification("to", "jest", "pierwsze", "probne", "piekne", "i", "swietne", "powiadomienie", 0));

        adapter = new NotificationAdapter(getActivity().getApplicationContext(), notificationsList);
        notificationListView = (ListView) getActivity().findViewById(R.id.notificationsListView);
        notificationListView.setAdapter(adapter);

        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getActivity().getApplicationContext(), "POZYCJA " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
