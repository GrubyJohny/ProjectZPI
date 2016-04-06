package zpi.squad.app.grouploc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import zpi.squad.app.grouploc.fragments.MapFragment;

/**
 * Created by sanczo on 2015-05-22.
 */
public class MarkerDialog extends DialogFragment {
    LatLng markerLocation;

    public MarkerDialog(LatLng latLng) {
        markerLocation = latLng;
    }

    public String getName() {
        return name;
    }

    public EditText getInput() {
        return input;
    }

    public void setInput(EditText input) {
        this.input = input;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private EditText input;

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mListener = (NoticeDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }
    }

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
                        saveMarker();
                        MapFragment.getMap().addMarker(new MarkerOptions()
                                .title(name)
                                .position(markerLocation)
                                .snippet("own")
                                .visible(true)
                                .draggable(false));
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
        marker.put("localization", new ParseGeoPoint(MapFragment.getMap().getCameraPosition().target.latitude, MapFragment.getMap().getCameraPosition().target.longitude));
        marker.put("owner", ParseUser.getCurrentUser());
//        marker.put("icon", CommonMethods.getInstance().encodeBitmapTobase64(Bitmap));

        marker.saveInBackground();
    }
}