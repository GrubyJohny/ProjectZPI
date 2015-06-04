package zpi.squad.app.grouploc;

import android.app.Application;
import android.text.TextUtils;

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

    public void addToMarkers(CustomMarker mark){
        markers.add(mark);

    }
    public void setMarkers(List<CustomMarker> markers){
        this.markers=markers;
    }
    public List<CustomMarker> getMarkers()
    {
        return markers;
    }
}