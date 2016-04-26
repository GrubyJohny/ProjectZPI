package zpi.squad.app.grouploc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;

import zpi.squad.app.grouploc.domains.Friend;
import zpi.squad.app.grouploc.domains.MyMarker;
import zpi.squad.app.grouploc.fragments.MapFragment;

/**
 * Created by sanczo on 2015-05-22.
 */
public class MarkerDialog extends DialogFragment {
    LatLng markerLocation;
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    private String name;
    private EditText input;

    public MarkerDialog() {
    }

    @SuppressLint("ValidFragment")
    public MarkerDialog(LatLng latLng) {
        markerLocation = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EditText getInput() {
        return input;
    }

    public void setInput(EditText input) {
        this.input = input;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mListener = (NoticeDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }
    }
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View customDialog = inflater.inflate(R.layout.dialog_marker, null);

        input = (EditText) customDialog.findViewById(R.id.markName);
        builder.setView(customDialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        name = input.getText().toString();
                        try {
                            SaveMarker saveMarker = new SaveMarker();
                            saveMarker.execute();
                        } catch (Exception e) {
                            e.getLocalizedMessage();
                            e.printStackTrace();
                        }
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MarkerDialog.this.getDialog().cancel();
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                });

        return builder.create();
    }

    private void saveMarker() {
        ParseObject marker = new ParseObject("Marker");

        marker.put("name", name);
        marker.put("localization", new ParseGeoPoint(MapFragment.lastClikOnMap.latitude, MapFragment.lastClikOnMap.longitude));
        marker.put("owner", ParseUser.getCurrentUser());
//        marker.put("icon", CommonMethods.getInstance().encodeBitmapTobase64(Bitmap));

        try {
            marker.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }


    private class RefreshMarkers extends AsyncTask<Void, Void, Void> {

        HashMap<MarkerOptions, MyMarker> ownMarkers;
        HashMap<MarkerOptions, MyMarker> sharedMarkers;
        HashMap<MarkerOptions, Friend> friendsMarkers;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MapFragment.actualShowingOnMapMarkers.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {

            SessionManager.getInstance().refreshOwnMarkers();
            ownMarkers = SessionManager.getInstance().getOwnMarkers();

            SessionManager.getInstance().refreshSharedMarkers();
            sharedMarkers = SessionManager.getInstance().getSharedMarkers();

            //tego nie odświezam w tym miejscu
            friendsMarkers = SessionManager.getInstance().getFriendsMarkers();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            for (MarkerOptions m : ownMarkers.keySet())
                MapFragment.actualShowingOnMapMarkers.put(MapFragment.getMap().addMarker(m).getId(), m);

            for (MarkerOptions m : sharedMarkers.keySet())
                MapFragment.actualShowingOnMapMarkers.put(MapFragment.getMap().addMarker(m).getId(), m);

            for (MarkerOptions m : friendsMarkers.keySet())
                MapFragment.actualShowingOnMapMarkers.put(MapFragment.getMap().addMarker(m).getId(), m);
        }
    }


    private class SaveMarker extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //to tak chwilowo, może ktos inteligentny i sprytny wymysli cos lepszego...
            //albo Ty Slawomir?
            Toast.makeText(getContext().getApplicationContext(), "Wait a while please.", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            saveMarker();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            RefreshMarkers refreshMarkers = new RefreshMarkers();
            refreshMarkers.execute();
        }
    }

}