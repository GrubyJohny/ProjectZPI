package zpi.squad.app.grouploc.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NewFriendshipRequestDialogActivity extends Activity {
    Bundle extras;
    ConfirmFriendship confirm = new ConfirmFriendship();
    SendFriendshipAcceptanceNotification sendAcceptNotif = new SendFriendshipAcceptanceNotification();

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

                        Thread tem = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                confirm.execute(extras.getString("friendship_id"));
                                sendAcceptNotif.execute(extras.getString(("friendship_id")));
                            }
                        });
                        dialog.cancel();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
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

    private class SendFriendshipAcceptanceNotification extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Friendship");
            query.whereEqualTo("objectId", params[0]);
            Log.e("MOZE TU", " WEJdZIE");
            try {
                List<ParseObject> res = query.find();
                if (res.size() > 0) {
                    String notifReceiver = "";
                    //String frinedshipId= res.get(0).fetch().getObjectId();
                    Log.e("ID OBDZEKTU: ", ((ParseObject) res.get(0).fetch().get("friend1")).getObjectId());
                    if (((ParseObject) res.get(0).fetch().get("friend1")).getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                        notifReceiver = ((ParseObject) res.get(0).fetch().get("friend2")).getString("email");
                    else
                        notifReceiver = ((ParseObject) res.get(0).fetch().get("friend1")).getString("email");

                    sendFriendshipAcceptanceNotification(notifReceiver, ParseUser.getCurrentUser().getString("name"), params[0]);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendFriendshipAcceptanceNotification(String email, String friendName, String friendshipId) throws JSONException {
        String notificationId = null;

        try {
            ParseObject notific = new ParseObject("Notification");
            notific.put("senderEmail", ParseUser.getCurrentUser().getEmail());
            notific.put("receiverEmail", email);
            notific.put("kindOfNotification", 102);
            notific.put("markedAsRead", false);
            notific.put("extra", friendshipId);


            notific.save();
            notific.fetch();
            notificationId = notific.getObjectId();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseQuery notificationQuery = ParseInstallation.getQuery().whereEqualTo("name", email);
        ParsePush notification = new ParsePush();
        notification.setQuery(notificationQuery);
        notification.setMessage("Użytkownik " + ParseUser.getCurrentUser().get("name") + " zaakceptował twoje zaproszenie do grona znajomych!");

        JSONObject message = new JSONObject();
        message.put("kind_of_notification", 102);
        message.put("friend_email", ParseUser.getCurrentUser().getEmail());
        message.put("friendship_id", friendshipId);
        message.put("origin_sender_name", ParseUser.getCurrentUser().get("name"));
        message.put("notification_id", notificationId);

        notification.setData(message);
        notification.setExpirationTimeInterval(60 * 60 * 24 * 7 * 4); //4 weeks
        notification.sendInBackground();
    }
}