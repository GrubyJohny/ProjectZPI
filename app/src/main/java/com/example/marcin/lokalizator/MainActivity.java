package com.example.marcin.lokalizator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private SessionManager session;
    private ViewFlipper myViewFlipper;
    private float lastX;
    private Spinner spinner1;
    private Button circleButton;
    private GoogleMap myMap;
    private LocationManager locationManager;
    private Runnable sender;//wątek, który będzie wysyłał info o położoniu użytkownika do bazy
    private SQLiteHandler db;//obiekt obsługujący lokalną androidową bazę danych

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db=new SQLiteHandler(getApplicationContext());
        sender=new Runnable() {
            @Override
            public void run() {

                try {
                    while(true) {
                        if (session.isLoggedIn()) {

                            Criteria criteria = new Criteria();
                            String provider = locationManager.getBestProvider(criteria, true);
                            Location myLocation = locationManager.getLastKnownLocation(provider);

                            double latitude = myLocation.getLatitude();
                            double longitude = myLocation.getLongitude();
                            sendCordinate(db.getId(), (float) latitude, (float) longitude);
                        }
                        Thread.sleep(5000);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        session = new SessionManager(this);
        new Thread(sender,"Watek do wysyłania koordynatów").start();
        spinner1 = (Spinner) findViewById(R.id.spinner);
        String[] spinnerOptions = {"Settings", "Log out"};
        ArrayAdapter<String> circleButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptions);
        spinner1.setAdapter(circleButtonOptions);

        circleButton = (Button) findViewById(R.id.circleButton);

        addListenerOnButton();
        addListenerOnSpinner();

        setUpViewFlipper();
        setUpMap();

    }



    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut(AdapterView.OnItemSelectedListener view) {
        db.deleteUsers();
        session.setLogin(false);
        Intent closeIntent = new Intent(this, LoginActivity.class);
        startActivity(closeIntent);
    }

    // Method to handle touch event like left to right swap and right to left swap
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN: {
                lastX = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = touchevent.getX();

                // if left to right swipe on screen
                if (lastX < currentX) {
                    // If no more View/Child to flip
                    if (myViewFlipper.getDisplayedChild() == 0)
                        break;

                    myViewFlipper.setInAnimation(this, R.anim.slide_in_from_left);
                    myViewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);

                    myViewFlipper.showNext();
                }

                // if right to left swipe on screen
                if (lastX > currentX) {
                    if (myViewFlipper.getDisplayedChild() == 1)
                        break;

                    myViewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
                    myViewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

                    myViewFlipper.showPrevious();
                }
                break;
            }
        }
        return false;
    }


    public void addListenerOnButton() {

        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner1.performClick();
            }
        });

    }

    public void addListenerOnSpinner(){
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch((int)position)
                {
                    case 0:
                        //USTAWIENIA TO DO
                        break;
                    case 1:
                        logOut(this);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setUpMap() {
        myMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.myMapFragment)).getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.setMyLocationEnabled(true);


        myMap.addMarker(new MarkerOptions().position(new LatLng(51.111508, 17.060268)).title("Rondo Reagana"));
        myMap.addMarker(new MarkerOptions().position(new LatLng(51.113825, 17.065890)).title("Akademik"));
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng latLng = new LatLng(0,0);
        Criteria criteria = new Criteria();
        if(criteria!=null) {
            String provider = locationManager.getBestProvider(criteria, true);
            Location myLocation = locationManager.getLastKnownLocation(provider);

            double latitude = myLocation.getLatitude();
            double longitude = myLocation.getLongitude();
            latLng = new LatLng(latitude, longitude);

        }
        myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        myMap.animateCamera(CameraUpdateFactory.zoomTo(15), 3000, null);

    }

    private void setUpViewFlipper() {

        myViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        myViewFlipper.setHorizontalScrollBarEnabled(true);

        View view1 = View.inflate(this, R.layout.activity_friends, null);
        myViewFlipper.addView(view1);
    }


    public  void sendCordinate(final String id,final float c1,final float c2)
    {
        StringRequest request=new StringRequest(Request.Method.POST,AppConfig.URL_LOGIN,new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Toast tost=Toast.makeText(getApplicationContext(),"Hip, Hip + "+c2,Toast.LENGTH_SHORT);
                tost.show();


            }
        },new Response.ErrorListener() {
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

                }else if (error instanceof TimeoutError) {
                    Log.d("TimeoutError>>>>>>>>>", "TimeoutError.......");

                }

                Log.e("MAna", "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {

            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("tag", "koordynaty");
                params.put("id",id);
                params.put("koordynat1", c1+"");
                params.put("koordynat2", c2+"");

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(request, "request_coordinates");
    }
}
