package zpi.squad.app.grouploc;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static AppController mInstance;

    private LatLng lastClikOnMap;
    private GoogleMap myMap;
    private List<CustomMarker> markers;
    private ProgressDialog dialog;

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
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public GoogleMap getMyMap() {
        return myMap;
    }

    public void setMyMap(GoogleMap myMap) {
        this.myMap = myMap;
    }

    public void setLastClikOnMap(LatLng lastClikOnMap) {
        this.lastClikOnMap = lastClikOnMap;
    }

    public LatLng getLastClikOnMap() {
        return lastClikOnMap;
    }

    public void addToMarkers(CustomMarker mark) {
        markers.add(mark);

    }

    public void addNewMarker(CustomMarker marker){
        markers.add(marker);
       // Sender.putMarkersOnMapAgain(markers, myMap);
    }

    public void setMarkers(List<CustomMarker> markers) {
        this.markers = markers;
    }

    public List<CustomMarker> getMarkers() {
        return markers;
    }


    public static boolean checkConn( Context ctx)
    {
        String tag="Sprawdzanie połączeia internetowego";
        ConnectivityManager conMgr=(ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=conMgr.getActiveNetworkInfo();
        if(info==null) {
            Log.d(tag, "Instancja klasy NetworkInfo jest refencją null");
            return false;

        }
        else
            if(!info.isConnected())
            {
                Log.d(tag, "Wykryty stan: not connected");
                return false;
            }

            else
                if(!info.isAvailable())
                {
                    Log.d(tag, "Wykryty stan: not available");
                    return false;
                }

                else{
                    Log.d(tag, "Wszystko wporządku, jest połączenie");
                    return true;
                }


    }
}