package zpi.squad.app.grouploc;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sanczo on 2015-05-18.
 */
public class Sender {
    private static HashMap<String,Marker> friends=new HashMap<String,Marker>(); //oj to jest tak bardzo, bardzo roboczo

    public static void sendMarker(final Context context, final CustomMarker cM, final Marker m, final SQLiteHandler db) {
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String TAG = "Sending markers";
                Log.d(TAG, "Odpowiedź " + response);
                try {
                    JSONObject jObj = new JSONObject(response);

                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String mySqlID = jObj.getString("markerid");
                        Log.d(TAG, "Wysyłanie zakończyło się sukcesem!!!");

                        Log.d("PUT", "mySqlID ustawiony dla tej instancji customMarkera to " + mySqlID);
                        String markerIdExtrenal = (mySqlID == null || mySqlID.equals("")) ? "NULL" : mySqlID;
                        String sqliteID = cM.getMarkerIdSQLite();
                        Log.d("PUT", "sqLiteID ustawiony dla tej instancji customMarkera to " + sqliteID);
                        String markerIdInteler = (sqliteID == null || sqliteID.equals("")) ? "NULL" : sqliteID;
                        String snippet = markerIdExtrenal + "," + markerIdInteler;

                        cM.setMarkerIdMySQL(mySqlID);
                        cM.setSaveOnServer(true);
                        m.setSnippet(snippet);
                        db.updateExternalId(cM.getMarkerIdSQLite(), mySqlID);
                        Toast.makeText(context, "Zapisano na trwałe marker z id: " + mySqlID, Toast.LENGTH_SHORT).show();

                    } else
                        Log.d(TAG, "Coś się spierdoliło!!!");


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("big error", "Wyłapałem błędem w łep");
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    Log.d("NoConnectionError>", "NoConnectionError.......");

                } else if (error instanceof AuthFailureError) {
                    Log.d("AuthFailureError>", "AuthFailureError.......");

                } else if (error instanceof ServerError) {
                    Log.d("ServerError>>>>>>>>>", "ServerError.......");

                } else if (error instanceof NetworkError) {
                    Log.d("NetworkError>>>>>>>>>", "NetworkError.......");

                } else if (error instanceof ParseError) {
                    Log.d("ParseError>>>>>>>>>", "ParseError.......");

                } else if (error instanceof TimeoutError) {
                    Log.d("TimeoutError>>>>>>>>>", "TimeoutError.......");

                }

                Log.e("OnLocationChanged", "Notification Error: " + error.getMessage());

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "nowy_marker");
                params.put("id", cM.getUserId());
                params.put("latitude", Double.toString(cM.getLatitude()));
                params.put("longitude", Double.toString(cM.getLongitude()));
                params.put("name", cM.getName());

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(request, "nowy_marker");
    }

    public static void sendRequestAboutMarkers(final String id, final List<CustomMarker> forResult, final GoogleMap map) {
        final String TAG = "Getting markers";
        Log.d(TAG, "dawaj " + id);
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, response.toString());
                forResult.clear();
                try {
                    JSONObject jObj = new JSONObject(response);
                    JSONArray markersArray=jObj.getJSONArray("markers");
                    for (int i = 0; i < markersArray.length(); i++) {
                        JSONObject marker = markersArray.getJSONObject(i);
                        int markerid = marker.getInt("markerid");
                        int uid = marker.getInt("uid");
                        double latitude = marker.getDouble("latitude");
                        double longitude = marker.getDouble("longitude");
                        String name = marker.getString("name");
                        CustomMarker customMarker = new CustomMarker(markerid + "", uid + "", latitude, longitude, name);
                        customMarker.setSaveOnServer(true);
                        forResult.add(customMarker);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "size " + forResult.size());
                Sender.putMarkersOnMapAgain(forResult, map);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("onErrorResponse", "Wydarzyło się coś strasznego!!!!!");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "daj_markery");
                params.put("id", id);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "nowy_prosba");

    }

    public static void sendRequestAboutFriendsCoordinate(final String whereClause, final List<CustomMarker> forResult, final GoogleMap map) {
        final String TAG = "Getting friendsCoordinate";
        Log.d(TAG, whereClause);
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                forResult.clear();
                Log.d(TAG, response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    if (!jObj.getBoolean("error")) {
                        String question = jObj.getString("clause");
                        Log.d(TAG, question);
                        JSONArray array = jObj.getJSONArray("coordinates");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject friendCoordinate = array.getJSONObject(i);
                            String id = friendCoordinate.getString("F_ID");
                            Double latitude = friendCoordinate.getDouble("latitude");
                            Double longitude = friendCoordinate.getDouble("longitude");
                            String name=friendCoordinate.getString("F_name");
                            String data=friendCoordinate.getString("data");
                            LatLng latLng=new LatLng(latitude,longitude);

                            if(friends.containsKey(id))
                            {
                                //tylko aktualizuj współrzędne
                                Marker oldFriendMarker= friends.get(id);
                                //ale może najpierw sprawdzę czy faktycznie się zmieniły
                                if(!latLng.equals(oldFriendMarker.getPosition())) {
                                    oldFriendMarker.setPosition(latLng);
                                    Log.d(TAG,"Ten przyjaciel jest już zaznaczony na mapie, ale zmienił swoje położenie");
                                }
                                else
                                {
                                    Log.d(TAG,"Ten przyjaciel jest już zaznaczony na mapie, a w dodatku nie zmienił swojego położenia od ostatniego razu");
                                }

                            }
                            else
                            {
                                //dodaj nowy marker
                                Marker dodany= map.addMarker(new MarkerOptions().title(name).position(latLng));
                                friends.put(id,dodany);
                                Log.d(TAG,"HashMapa friends liczy : "+friends.size()+" elementów");
                            }

                        }
                    } else {
                        Log.d(TAG, "cos poszlo nie tak");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "on response, size forResult+ " + forResult.size());
                Sender.putMarkersOnMapAgain(forResult, map);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("tag", "getFriendsCoordinate");
                map.put("where", whereClause);
                return map;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "nowa_prosba");
    }

    public static void putMarkersOnMapAgain(List<CustomMarker> markers, GoogleMap myMap) {
        for (CustomMarker cM : markers) {
            String mySqlID = cM.getMarkerIdMySQL();
            Log.d("PUT", "mySqlID ustawiony dla tej instancji customMarkera to " + mySqlID);
            String markerIdExtrenal = (mySqlID == null || mySqlID.equals("")) ? "NULL" : mySqlID;
            String sqliteID = cM.getMarkerIdSQLite();
            Log.d("PUT", "sqLiteID ustawiony dla tej instancji customMarkera to " + sqliteID);
            String markerIdInteler = (sqliteID == null || sqliteID.equals("")) ? "NULL" : sqliteID;
            String snippet = markerIdExtrenal + "," + markerIdInteler;

            if(cM.getName().contains("(od ")){
                myMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarkerhigreen)).position(new LatLng(cM.getLatitude(), cM.getLongitude())).title(cM.getName()).snippet(snippet));
            }
            else if(cM.isSaveOnServer()) {
                myMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarkerhiblue)).position(new LatLng(cM.getLatitude(), cM.getLongitude())).title(cM.getName()).snippet(snippet));
            }
            else{
                myMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarkerhi)).position(new LatLng(cM.getLatitude(), cM.getLongitude())).title(cM.getName()).snippet(snippet));
            }
            Log.d("PUT", "Snippet ustawiony dla tej instancji customMarkera to " + snippet);
        }
    }

    public static void sendRequestAboutRemoveMarker(final String id, final GoogleMap myMap, final List<CustomMarker> markers) {
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("DELETE MARKER", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        myMap.clear();
                        putMarkersOnMapAgain(markers, myMap);
                        Log.d("DELETE MARKER", "Wszystko przebiegło zgodnie z planem");
                    } else {
                        Log.d("DELETE MARKER", "Uwaga, niechciany bład");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<String, String>();
                param.put("tag", "delete_marker");
                param.put("id", id);
                return param;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "nowa_prosba");
    }

    public static String makeStatementAboutFriendsList(ArrayList<Friend> friendsList) {
        Friend current;
        StringBuilder result = new StringBuilder();
        if (!friendsList.isEmpty()) {
            current = friendsList.get(0);
            result.append("U_ID=" + current.getFriendID());
        }
        for (int i = 1; i < friendsList.size(); i++) {
            current = friendsList.get(i);
            result.append(" OR U_ID=" + current.getFriendID());
        }
        return result.toString();
    }

    public static void shareMarker(final Context context, final String uid, final String name, final String latitude, final String longitude) {
        final String TAG = "Sharing marker to friends";

        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    if (!jObj.getBoolean("error")) {
                        Toast.makeText(context, "Marker sent successfully", Toast.LENGTH_SHORT);
                    } else {
                        Log.d(TAG, "Sharing marker problem");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("tag", "shareMarker");
                map.put("uid", uid);
                map.put("name", name);
                map.put("latitude", latitude);
                map.put("longitude", longitude);
                return map;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "share marker");
    }

}
