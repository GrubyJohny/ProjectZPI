package zpi.squad.app.grouploc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by sanczo on 2015-05-22.
 */
public class MarkerDialog extends DialogFragment {


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
        View customDialog=inflater.inflate(R.layout.dialog_marker, null);

        input=(EditText)customDialog.findViewById(R.id.markName);

        builder.setView(customDialog)

                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        name = input.getText().toString();

                        mListener.onDialogPositiveClick(MarkerDialog.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MarkerDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

}
