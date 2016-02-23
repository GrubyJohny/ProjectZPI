package zpi.squad.app.grouploc.utils;

import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.activity.NewFriendshipRequestDialogActivity;

/**
 * Created by gruby on 19.02.2016.
 */
public class MyReceiver extends ParsePushBroadcastReceiver {
    JSONObject pushData = null;

    @Override
    protected void onPushReceive(Context mContext, Intent intent) {
        //enter your custom here generateNotification();
        super.onPushReceive(mContext, intent);

        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));

            if (pushData != null) {

                SessionManager.getInstance().refreshNotificationsList();

                switch (pushData.getInt("kind_of_notification")) {
                    case 101: //friendship request to accept
                    {
                        //z poziomou tego gnoja Receivera nie da rady wyświetlać żadnych okienek, dlatego
                        //osobna aktywność, mam nadzieję, że to przeczytasz zanim mnie zjedziesz jak burą sukę :D
                        Intent i = new Intent(mContext, NewFriendshipRequestDialogActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("new_friend_name", pushData.getString("new_friend_name"));
                        i.putExtra("friendship_id", pushData.getString("friendship_id"));
                        i.putExtra("notification_id", pushData.getString("notification_id"));

                        mContext.startActivity(i);

                        break;

                    }
                    case 102: //friendship request accepted - info only and need to refresh friends list
                    {
                        break;
                    }
                    case 103: //friendship deleted - need to refresh friends list
                    {

                        break;
                    }

                    default:
                        break;

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPushOpen(final Context context, Intent intent) {
        super.onPushOpen(context, intent);

    }

}