package zpi.squad.app.grouploc.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.List;

import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.adapters.FriendAdapter;
import zpi.squad.app.grouploc.adapters.NotificationAdapter;
import zpi.squad.app.grouploc.adapters.SearchingFriendAdapter;
import zpi.squad.app.grouploc.domains.Friend;
import zpi.squad.app.grouploc.domains.Notification;

public class CommonMethods {
    private SessionManager session;

    public CommonMethods() {
    }

    public CommonMethods(Context context) {
        session = SessionManager.getInstance(context);
    }

    public void reloadNotificationsData(NotificationAdapter adapter) {
        session.refreshNotificationsList();
        List<Notification> objects = session.getNotificationsList();

        //tutj po otrzymaniu powiadomienia wypieprza nulla z adaptera
        // !
        adapter.clear();
        adapter.addAll(objects);
        adapter.notifyDataSetChanged();
    }

    public void reloadFriendsData(FriendAdapter adapter) {
        session.refreshFriendsList();
        List<Friend> objects = session.getFriendsList();
        adapter.clear();
        adapter.addAll(objects);
        adapter.notifyDataSetChanged();
    }

    public void reloadSearchingFriendsData(SearchingFriendAdapter adapter) {
        List<Friend> objects = session.getAllUsersFromParseWithoutCurrentAndFriends();
        adapter.clear();
        adapter.addAll(objects);
        adapter.notifyDataSetChanged();
    }

    // method for bitmap to base64
    public String encodeBitmapTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        //Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    // method for base64 to bitmap
    public Bitmap decodeBase64ToBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public Bitmap clipBitmap(Bitmap bitmap, ImageView x) {
        if (bitmap == null)
            return null;
        final int width = x.getLayoutParams().width;
        final int height = x.getLayoutParams().height;
        bitmap = bitmap.createScaledBitmap(bitmap, width, height, true);

        DisplayMetrics metrics = new DisplayMetrics();

        final Bitmap outputBitmap = Bitmap.createBitmap(metrics, width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float) (width / 2)
                , (float) (height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }
}
