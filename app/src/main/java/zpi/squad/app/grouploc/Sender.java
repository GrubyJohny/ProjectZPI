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

    public static void sendMarker(final Context context,final String id, final double latitude, final double longitude, final String name,final CustomMarker cM,final Marker m) {
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String TAG = "Sending markers";
                Log.d(TAG, response.toString());
                try{
                    JSONObject jObj = new JSONObject(response);

                    boolean error = jObj.getBoolean("error");
                    if(!error) {
                        Log.d(TAG, "Wysyłanie zakończyło się sukcesem!!!");
                        String id = jObj.getString("markerid");

                        cM.setMarkerId(id);
                        m.setSnippet(id);
                        Toast.makeText(context,"Zapisano na trwałe marker z id: "+id,Toast.LENGTH_SHORT).show();

                    }
                    else
                        Log.d(TAG,"Coś się spierdoliło!!!");


                }catch(Exception e){

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
                params.put("id", id);
                params.put("latitude", latitude + "");
                params.put("longitude", longitude + "");
                params.put("name",name);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(request, "nowy_marker");
    }

    public static void sendRequestAboutMarkers(final String id,final List<CustomMarker> forResult,final GoogleMap map)
    {
        final String TAG = "Getting markers";
        Log.d(TAG, "dawaj "+id);
        StringRequest request=new StringRequest(Request.Method.POST,AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, response.toString());
                forResult.clear();
                try {
                    JSONObject jObj=new JSONObject(response);
                    JSONArray markersArray=jObj.getJSONArray("notifications");
                    for(int i=0;i<markersArray.length();i++)
                    {
                        JSONObject marker=markersArray.getJSONObject(i);
                        int markerid=marker.getInt("markerid");
                        int uid=marker.getInt("uid");
                        double latitude=marker.getDouble("latitude");
                        double longitude=marker.getDouble("longitude");
                        String name=marker.getString("name");
                        CustomMarker customMarker =new CustomMarker(markerid+"",uid+"",latitude,longitude,name);
                        customMarker.setSaveOnServer(true);
                        forResult.add(customMarker);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "size "+forResult.size());
                Sender.putMarkersOnMapAgain(forResult,map);


            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("onErrorResponse", "Wydarzyło się coś strasznego!!!!!");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params= new HashMap<>();
                params.put("tag","daj_markery");
                params.put("id",id);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "nowy_prosba");

    }

    public static void sendRequestAboutFriendsCoordinate(final String whereClause, final List<CustomMarker> forResult,final GoogleMap map)
    {
        final String TAG = "Getting friendsCoordinate";
        Log.d(TAG, whereClause);
        StringRequest request=new StringRequest(Request.Method.POST,AppConfig.URL_LOGIN,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                forResult.clear();
                Log.d(TAG, response);
                try {
                    JSONObject jObj=new JSONObject(response);
                    if(!jObj.getBoolean("error"))
                    {
                        String question=jObj.getString("clause");
                        Log.d(TAG, question);
                        JSONArray array=jObj.getJSONArray("coordinates");
                        for(int i=0;i<array.length();i++)
                        {
                            JSONObject friendCoordinate=array.getJSONObject(i);
                            String id=friendCoordinate.getString("F_ID");
                            Double latitude=friendCoordinate.getDouble("latitude");
                            Double longitude=friendCoordinate.getDouble("longitude");
                            CustomMarker marker=new CustomMarker(id,latitude,longitude);
                            forResult.add(marker);
                        }
                    }
                    else
                    {
                        Log.d(TAG, "cos poszlo nie tak");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"on response, size forResult+ "+forResult.size());
                Sender.putMarkersOnMapAgain(forResult,map);

            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map=new HashMap<String,String>();
                map.put("tag","getFriendsCoordinate");
                map.put("where",whereClause);
                return map;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "nowa_prosba");
    }
    public static void putMarkersOnMapAgain(List<CustomMarker> markers,GoogleMap myMap)
    {
        for(CustomMarker cM:markers)
        {
            myMap.addMarker(new MarkerOptions().position(new LatLng(cM.getLatitude(), cM.getLongitude())).title(cM.getUserId()));

            Log.d("put", "dodaje a jak" + cM.getLatitude()+","+cM.getLongitude());
        }
       //myMap.addMarker(new MarkerOptions().position(new LatLng(51.109383,17.057973)));
    }


    public static String makeStatementAboutFriendsList(ArrayList<Friend> friendsList)
    {
        Friend current;
        StringBuilder result=new StringBuilder();
        if(!friendsList.isEmpty())
        {
            current=friendsList.get(0);
            result.append("U_ID="+current.getFriendID());
        }
        for(int i=1;i<friendsList.size();i++)
        {
            current=friendsList.get(i);
            result.append(" OR U_ID="+current.getFriendID());
        }
        return result.toString();
    }

}
