package zpi.squad.app.grouploc.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.activities.NewFriendshipRequestDialogActivity;
import zpi.squad.app.grouploc.fragments.NotificationFragment;
import zpi.squad.app.grouploc.helpers.CommonMethods;

/**
 * Created by gruby on 19.02.2016.
 */
public class MyReceiver extends ParsePushBroadcastReceiver {
    JSONObject pushData = null;
    CommonMethods commonMethods;

    @Override
    protected void onPushReceive(Context mContext, Intent intent) {
        //enter your custom here generateNotification();
        super.onPushReceive(mContext, intent);

        commonMethods = new CommonMethods(mContext);

        commonMethods.reloadNotificationsData(NotificationFragment.adapter);

        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));

            if (pushData != null) {

                SessionManager.getInstance().refreshNotificationsList();

                switch (pushData.getInt("kind_of_notification")) {
                    case 101: //friendship request to accept
                    {
                        Log.e("NOTIF 101: ", "friendship requested");
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
                        Log.e("NOTIF 102: ", "friendship accepted");
                        commonMethods.reloadNotificationsData(NotificationFragment.adapter);

                        break;
                    }
                    case 103: //friendship deleted - need to refresh friends list
                    {

                        break;
                    }
                    case 104: //marker received from friend
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