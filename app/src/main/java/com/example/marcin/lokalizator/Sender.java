package com.example.marcin.lokalizator;

import android.util.Log;

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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sanczo on 2015-05-18.
 */
public class Sender {

    public static void sendMarker(final String id, final double latitude, final double longitude, final String name) {
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String TAG = "Sending markers";
                Log.d(TAG, response.toString());
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(!error)
                        Log.d(TAG,"Wysyłanie zakończyło się sukcesem!!!");
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



}
