package com.example.marcin.lokalizator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.location.LocationManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity {

    private SessionManager session;
    private ViewFlipper myViewFlipper;
    private float lastX;
    private Spinner spinner1;
    private ImageButton circleButton;
    private GoogleMap myMap;
    private LocationManager locationManager;
    private Button poiFilteringButton;
    private RadioGroup radioGroupPoi;
    private HashMap<Integer, Marker> visibleMarkers = new HashMap<Integer, Marker>();
    private List<MarkerOptions> markers;
    private ArrayList<MarkerOptions> markersKFC;
    private ArrayList<MarkerOptions> markersMcDonalds;

    private Runnable sender;//wątek, który będzie wysyłał info o położoniu użytkownika do bazy
    private SQLiteHandler db;//obiekt obsługujący lokalną androidową bazę danych

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new SQLiteHandler(getApplicationContext());
        sender = new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {
                        if (session.isLoggedIn()) {

                            Criteria criteria = new Criteria();
                            String provider = locationManager.getBestProvider(criteria, true);
                            Location myLocation = locationManager.getLastKnownLocation(provider);

                            double latitude = myLocation.getLatitude();
                            double longitude = myLocation.getLongitude();

                            //double latitude = 54.5;
                            //double longitude = 19.2;
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
        new Thread(sender, "Watek do wysyłania koordynatów").start();
        spinner1 = (Spinner) findViewById(R.id.spinner);
        String[] spinnerOptions = {"Settings", "Log out"};
        ArrayAdapter<String> circleButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptions);
        spinner1.setAdapter(circleButtonOptions);

        circleButton = (ImageButton) findViewById(R.id.circleButton);
        addListenerOnButton();
        addListenerOnSpinner();

        final RadioButton radioAll = (RadioButton) findViewById(R.id.radioButton3);
        radioAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for(int i=0; i<markersMcDonalds.size(); i++)
                    myMap.addMarker(markersMcDonalds.get(i));

                for(int i=0; i<markersKFC.size(); i++)
                    myMap.addMarker(markersKFC.get(i));
            }

        });
        final RadioButton radiokfc = (RadioButton) findViewById(R.id.radioButton);
        radiokfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  myMap.clear();
                    for(int i=0; i<markersKFC.size(); i++)
                    {
                       myMap.addMarker(markersKFC.get(i));

                    }
                }


        });
        final RadioButton radioMcDonalds = (RadioButton) findViewById(R.id.radioButton2);
        radioMcDonalds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for(int i=0; i<markersMcDonalds.size(); i++)
                {
                    myMap.addMarker(markersMcDonalds.get(i));

                }
            }

        });


        setUpViewFlipper();
        setUpMap();
        preparePoiPoints();

    }

    private void addListenerOnPoiButton() {
        poiFilteringButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if (findViewById(R.id.radioButton).getVisibility() == View.INVISIBLE) {
                    (findViewById(R.id.radioButton)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.radioButton2)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.radioButton3)).setVisibility(View.VISIBLE);
                }
                else
                {
                    (findViewById(R.id.radioButton)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.radioButton2)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.radioButton3)).setVisibility(View.INVISIBLE);
                }
            }

        });
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


    public void sendCordinate(final String id, final float c1, final float c2) {
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
               // Toast tost = Toast.makeText(getApplicationContext(), ""+c1+" " +c2, Toast.LENGTH_SHORT);
               // tost.show();


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

                Log.e("MAna", "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "koordynaty");
                params.put("id", id);
                params.put("koordynat1", c1 + "");
                params.put("koordynat2", c2 + "");

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(request, "request_coordinates");
    }


    public void addListenerOnButton() {

        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner1.performClick();
            }
        });

    }

    public void addListenerOnSpinner() {
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) position) {
                    case 0:
                        //USTAWIENIA TO DO
                        break;
                    case 1:
                        logOut(this);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpMap() {
        myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.myMapFragment)).getMap();
        myMap.setMyLocationEnabled(true);


        myMap.addMarker(new MarkerOptions().position(new LatLng(51.111508, 17.060268)).title("Rondo Reagana"));
        myMap.addMarker(new MarkerOptions().position(new LatLng(51.113825, 17.065890)).title("Akademik"));


        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng latLng;
        Criteria criteria = new Criteria();
        if (criteria != null) {
            String provider = locationManager.getBestProvider(criteria, true);
            Location myLocation = locationManager.getLastKnownLocation(provider);

            double latitude = myLocation.getLatitude();
            double longitude = myLocation.getLongitude();
            latLng = new LatLng(latitude, longitude);

            myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            myMap.animateCamera(CameraUpdateFactory.zoomTo(15), 3000, null);
        }

        myMap.setOnCameraChangeListener(getCameraChangeListener());

    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener()
    {
        return new GoogleMap.OnCameraChangeListener()
        {
            @Override
            public void onCameraChange(CameraPosition position)
            {
                //addItemsToMap(markers);
            }
        };
    }


    private void setUpViewFlipper() {

        myViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        myViewFlipper.setHorizontalScrollBarEnabled(true);

    }

    private void preparePoiPoints()
    {
        //add KFC
        markersKFC = new ArrayList<MarkerOptions>();
        markersKFC.add(new MarkerOptions().position(new LatLng(51.088720, 16.999470)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.098590, 17.037649)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.099488, 17.028555)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.107786, 17.032295)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.108399, 17.039837)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.112278, 17.059493)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.119768, 16.989862)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.131706, 17.062039)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position( new LatLng(51.142201, 17.088718)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));

        //add Mcdonalds
        markersMcDonalds = new ArrayList<MarkerOptions>();
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.106616,	16.948973)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.047800,	16.958560)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.054236,	16.975421)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.119674,	16.988372)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.060817,	16.993000)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.118044,	16.999969)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.116333,	17.024023)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.150980,	17.026410)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.109436,	17.033035)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.108075,	17.037220)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.108230,	17.039610)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.082358,	17.048646)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.095464,	17.056789)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.112505,	17.059547)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.142060,	17.088680)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));

        //add markers
        for(int i=0; i<markersKFC.size(); i++)
            myMap.addMarker(markersKFC.get(i));

        for(int i=0; i<markersMcDonalds.size(); i++)
            myMap.addMarker(markersMcDonalds.get(i));



        poiFilteringButton = (Button) findViewById(R.id.buttonPOIFiltering);
        addListenerOnPoiButton();





    }


}
