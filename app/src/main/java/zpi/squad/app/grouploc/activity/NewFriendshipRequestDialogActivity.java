package zpi.squad.app.grouploc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class NewFriendshipRequestDialogActivity extends Activity {
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = this.getIntent().getExtras();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Friendship request")
                .setMessage("Do you want to accept invitation from " + extras.getString("new_friend_name"))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ConfirmFriendship confirm = new ConfirmFriendship();
                        confirm.execute(extras.getString("friendship_id"));

                        //tutaj jeszcze powiadomienie a zaakceptowaniu zaproszenia

                        dialog.cancel();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                        getFragmentManager().popBackStack();

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
            Toast.makeText(getApplicationContext(), "Friend added!", Toast.LENGTH_LONG).show();
        }
    }
}