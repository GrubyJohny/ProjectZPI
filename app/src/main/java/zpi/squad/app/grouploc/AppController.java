package zpi.squad.app.grouploc;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;

import java.util.List;

import zpi.squad.app.grouploc.config.AppConfig;
import zpi.squad.app.grouploc.domains.CustomMarker;
import zpi.squad.app.grouploc.fragments.MapFragment;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();
    public static GoogleMap myMap;
    private static AppController mInstance;
    private LatLng lastClikOnMap;
    private List<CustomMarker> markers;
    private ProgressDialog dialog;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public static GoogleMap getMyMap() {
        return myMap;
    }

    public void setMyMap(GoogleMap myMap) {
        this.myMap = myMap;
    }

    public static boolean checkConn(Context ctx) {
        String tag = "Sprawdzanie połączeia internetowego";
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        if (info == null) {
            Log.d(tag, "Instancja klasy NetworkInfo jest refencją null");
            return false;

        } else if (!info.isConnected()) {
            Log.d(tag, "Wykryty stan: not connected");
            return false;
        } else if (!info.isAvailable()) {
            Log.d(tag, "Wykryty stan: not available");
            return false;
        } else {
            Log.d(tag, "Wszystko wporządku, jest połączenie");
            return true;
        }


    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public void setDialog(ProgressDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        try {
            //Parse.enableLocalDatastore(getApplicationContext());
            Parse.initialize(getApplicationContext(), AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);
            ParseFacebookUtils.initialize(this);
            ParseInstallation.getCurrentInstallation().saveInBackground();

        } catch (Exception e) {
            e.getLocalizedMessage();
            e.printStackTrace();
        }

        myMap = MapFragment.getMap();
    }
}