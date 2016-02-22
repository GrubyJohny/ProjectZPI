package zpi.squad.app.grouploc.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import zpi.squad.app.grouploc.MainActivity;
import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;

/**
 * Created by gruby on 19.02.2016.
 */
public class MyReceiver extends ParsePushBroadcastReceiver {

    protected void onPushReceive(final Context mContext, Intent intent) {
        //enter your custom here generateNotification();
        super.onPushReceive(mContext, intent);
        Log.e("onPushReceive", "weszło" );
        Toast.makeText(mContext, "NO i weszło", Toast.LENGTH_LONG).show();

        JSONObject pushData=null;
        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(pushData!=null)
        {
            try {
                switch(pushData.getInt("kind_of_notification")){
                    case 101: //friendship request to accept
                            {
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.context, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle("Accept friends invitation");
                                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.context, "Friendship accepted", Toast.LENGTH_LONG).show();
                                    }
                                });
                                builder.setCancelable(true);
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.setMessage("Do you want to accept invitation from " + "friend_name" + "?");
                                builder.show();  //i tu wywala błąd:
                                /*
                                 java.lang.RuntimeException: Unable to start receiver zpi.squad.app.grouploc.utils.MyReceiver: android.view.WindowManager$BadTokenException: Unable to add window -- token null is not for an application
                                 ...
                                 Caused by: android.view.WindowManager$BadTokenException: Unable to add window -- token null is not for an application
                                * */

                            }

                    default : break;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Log.e("srail: ", ((String) pushData.get("friend_email")).toString() );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Bundle b = intent.getExtras();

    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        Log.e("OPEN", "OPENIĘTO");

    }

    //private class AcceptFriendship extends AsyncTask<>
}