package com.example.marcin.lokalizator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private SessionManager session;
    private ViewFlipper myViewFlipper;
    private float lastX;
    private Spinner spinner1;
    private Spinner spinner2;
    private ImageButton circleButton;
    private Button noticeButton;
    private View layoutSettings;
    private View layoutFlipper;
    private GoogleMap myMap;
    private LocationManager locationManager;
    private Button mainPoiButton;
    private Button clearPoiButton;
    private Button confirm;
    private Button cancel;
    private ProgressDialog pDialog;
    PoiJSONParser poiBase = new PoiJSONParser();
    private ArrayList<MarkerOptions> markersRestaurants = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersKfc = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersMcdonalds = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersBars = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersCoffee = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersShoppingMalls = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersShops = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersMarkets = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersNightClubs = new ArrayList<MarkerOptions>();
    private ArrayList<MarkerOptions> markersParks = new ArrayList<MarkerOptions>();


    //Andoridowy obiekt przechowujący dane o położeniu(np latitude, longitude, kiedy zostało zarejestrowane)
    private Location mCurrentLocation;
    private Runnable sender;//wątek, który będzie wysyłał info o położoniu użytkownika do bazy
   // private SQLiteHandler db;//obiekt obsługujący lokalną androidową bazę danych
    //obiekt będący parametrem, przy wysłaniu żądania o aktualizację lokacji
    private LocationRequest mLocationRequest;

    //Obiekt w ogólności reprezentujący googlowe api service,
    //jest często przekazywany jako argument, gdy coś o tego api chcemy
    private GoogleApiClient mGoogleApiClient;

    //Flaga mowiąca o tym czy chcemy monitorować lokalizację
    private boolean mRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        session = new SessionManager(this);
    //    db = new SQLiteHandler(getApplicationContext());

        sender = createSendThread();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        //Inicjalizacja mGoogleApiClient i rozpoczęcie połączenia
        mRequestingLocationUpdates = true;
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();


       // new Thread(sender, "Watek do wysyłania koordynatów").start();

        mainSpinner();
        notifications();

        circleButton = (ImageButton) findViewById(R.id.circleButton);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.johny);
        Bitmap bitmap_round = clipBitmap(icon);
        circleButton.setImageBitmap(bitmap_round);

        noticeButton = (Button) findViewById(R.id.noticeButton);

        addListenerOnButton();
        addListenerOnSpinner();
        addListenerOnSpinner2();

        setupPoiButtons();
        setUpViewFlipper();

        layoutSettings = (View) findViewById(R.id.settingsLayout);
        layoutFlipper = (View) findViewById(R.id.flipperLayout);

        SettingButtons();
/*
        try {
            preparePoiPoints();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        Button button = (Button)findViewById(R.id.button);
        final EditText editText = (EditText)findViewById(R.id.friendEmail);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friend = editText.getText().toString();
                sendFriendshipRequest(session.getUserId(), friend);
                editText.setText("");
            }
        });

    }

    private void setupPoiButtons() {

        (findViewById(R.id.ButtonFood)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonFoodBar)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonFoodCoffee)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonFoodKfc)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonFoodMcDonald)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonFoodRestaurant)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonShops)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonShopsStores)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonShopsMarket)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonShopsShoppingMall)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonLeisure)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonLeisureClubs)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonLeisureParks)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonMyPlaces)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ButtonClearPoi)).setVisibility(View.INVISIBLE);


        mainPoiButton = (Button) findViewById(R.id.buttonPOIFiltering);

        clearPoiButton = (Button) findViewById(R.id.ButtonClearPoi);
        clearPoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                //TUTAJ TRZEBA DODAC JAKAS METODĘ onMapStart czy coś w tym stylu, bo nie ma możliwości,
                // żeby usunąć kilka pojedynczych markerów, tylko trzeba całkiem mapę wyczyścić.
                // no i chodzi o to, żeby dodać po tym czyszczeniu jakieś punkty początkowe.
                // nie wiem czy setupMap() to dobre rozwiązanie tutaj
                mainPoiButton.performClick();
                setUpMap(true);
            }

        });

        mainPoiButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if (findViewById(R.id.ButtonFood).getVisibility() == View.INVISIBLE) {
                    (findViewById(R.id.ButtonFood)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonShops)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonLeisure)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonMyPlaces)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonClearPoi)).setVisibility(View.VISIBLE);
                } else {
                    (findViewById(R.id.ButtonFood)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodBar)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodCoffee)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodKfc)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodMcDonald)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodRestaurant)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShops)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsStores)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsMarket)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsShoppingMall)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonLeisure)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonLeisureClubs)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonLeisureParks)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonMyPlaces)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonClearPoi)).setVisibility(View.INVISIBLE);
                }
            }

        });

        final Button myButtonFood = (Button) findViewById(R.id.ButtonFood);
        final Button myButtonFoodBar = (Button) findViewById(R.id.ButtonFoodBar);
        final Button myButtonFoodCoffee = (Button) findViewById(R.id.ButtonFoodCoffee);
        final Button myButtonFoodKfc = (Button) findViewById(R.id.ButtonFoodKfc);
        final Button myButtonFoodMcDonald = (Button) findViewById(R.id.ButtonFoodMcDonald);
        final Button myButtonFoodRestaurant = (Button) findViewById(R.id.ButtonFoodRestaurant);
        final Button myButtonShopsMarket = (Button) findViewById(R.id.ButtonShopsMarket);
        final Button myButtonShopsStores = (Button) findViewById(R.id.ButtonShopsStores);
        final Button myButtonShopsShoppingMalls = (Button) findViewById(R.id.ButtonShopsShoppingMall);
        final Button myButtonShops = (Button) findViewById(R.id.ButtonShops);
        final Button myButtonLeisureClubs = (Button) findViewById(R.id.ButtonLeisureClubs);
        final Button myButtonLeisureParks = (Button) findViewById(R.id.ButtonLeisureParks);
        final Button myButtonLeisure = (Button) findViewById(R.id.ButtonLeisure);

        //  FOOOD AREA

        myButtonFoodBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersBars.size(); i++)
                    myMap.addMarker(markersBars.get(i));
                setUpMap(false);

            }

        });

        myButtonFoodCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersCoffee.size(); i++)
                    myMap.addMarker(markersCoffee.get(i));
                setUpMap(false);

            }

        });

        myButtonFoodKfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersKfc.size(); i++)
                    myMap.addMarker(markersKfc.get(i));
                setUpMap(false);

            }

        });

        myButtonFoodMcDonald.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersMcdonalds.size(); i++)
                    myMap.addMarker(markersMcdonalds.get(i));
                setUpMap(false);

            }

        });

        myButtonFoodRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersRestaurants.size(); i++)
                    myMap.addMarker(markersRestaurants.get(i));
                setUpMap(false);

            }

        });

        myButtonFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myButtonShopsMarket.getVisibility()==View.VISIBLE)
                {
                    (findViewById(R.id.ButtonShopsStores)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsMarket)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsShoppingMall)).setVisibility(View.INVISIBLE);
                }
                if(myButtonLeisureClubs.getVisibility()==View.VISIBLE)
                {
                    (findViewById(R.id.ButtonLeisureParks)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonLeisureClubs)).setVisibility(View.INVISIBLE);
                }

                if(myButtonFoodBar.getVisibility()==View.INVISIBLE)
                {
                    (findViewById(R.id.ButtonFoodBar)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonFoodCoffee)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonFoodKfc)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonFoodMcDonald)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonFoodRestaurant)).setVisibility(View.VISIBLE);
                }
                else
                {
                    (findViewById(R.id.ButtonFoodBar)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodCoffee)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodKfc)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodMcDonald)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodRestaurant)).setVisibility(View.INVISIBLE);
                }

            }

        });
        // END OF FOOD AREA

        // SHOPS AREA

        myButtonShopsMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersMarkets.size(); i++)
                    myMap.addMarker(markersMarkets.get(i));
                setUpMap(false);

            }

        });

        myButtonShopsStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersShops.size(); i++)
                    myMap.addMarker(markersShops.get(i));
                setUpMap(false);

            }

        });

        myButtonShopsShoppingMalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersShoppingMalls.size(); i++)
                    myMap.addMarker(markersShoppingMalls.get(i));
                setUpMap(false);

            }

        });

        myButtonShops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myButtonFoodBar.getVisibility()==View.VISIBLE)
                {
                    (findViewById(R.id.ButtonFoodBar)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodCoffee)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodKfc)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodMcDonald)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodRestaurant)).setVisibility(View.INVISIBLE);
                }
                if(myButtonLeisureClubs.getVisibility()==View.VISIBLE)
                {
                    (findViewById(R.id.ButtonLeisureParks)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonLeisureClubs)).setVisibility(View.INVISIBLE);
                }

                if(myButtonShopsMarket.getVisibility()==View.INVISIBLE)
                {
                    (findViewById(R.id.ButtonShopsStores)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonShopsMarket)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonShopsShoppingMall)).setVisibility(View.VISIBLE);
                }
                else
                {
                    (findViewById(R.id.ButtonShopsStores)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsMarket)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsShoppingMall)).setVisibility(View.INVISIBLE);
                }

            }

        });
        // END OF SHOPS AREA

        //LEISURE AREA

        myButtonLeisureClubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersNightClubs.size(); i++)
                    myMap.addMarker(markersNightClubs.get(i));
                setUpMap(false);

            }

        });

        myButtonLeisureParks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersParks.size(); i++)
                    myMap.addMarker(markersParks.get(i));
                setUpMap(false);

            }

        });

        myButtonLeisure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myButtonShopsMarket.getVisibility()==View.VISIBLE)
                {
                    (findViewById(R.id.ButtonShopsStores)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsMarket)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonShopsShoppingMall)).setVisibility(View.INVISIBLE);
                }
                if(myButtonFoodBar.getVisibility()==View.VISIBLE)
                {
                    (findViewById(R.id.ButtonFoodBar)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodCoffee)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodKfc)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodMcDonald)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonFoodRestaurant)).setVisibility(View.INVISIBLE);
                }

                if(myButtonLeisureClubs.getVisibility()==View.INVISIBLE)
                {
                    (findViewById(R.id.ButtonLeisureClubs)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.ButtonLeisureParks)).setVisibility(View.VISIBLE);
                }
                else
                {
                    (findViewById(R.id.ButtonLeisureClubs)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.ButtonLeisureParks)).setVisibility(View.INVISIBLE);
                }

            }

        });


    }


    private void mainSpinner() {
        spinner1 = (Spinner) findViewById(R.id.spinner);
        String[] spinnerOptions = {"Nie dotykać", "Settings", "Log out"};
        ArrayAdapter<String> circleButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptions);
        spinner1.setAdapter(circleButtonOptions);
    }

    private void notifications() {
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        String[] spinner2Options = {"notice 1", "notice 2", "notice 3", "notice 4", "notice 5", "notice 6"};
        ArrayAdapter<String> noticeButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinner2Options);
        spinner2.setAdapter(noticeButtonOptions);
    }

    public void SettingButtons() {
        confirm = (Button) findViewById(R.id.confirmSettingsButton);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutFlipper.setVisibility(View.VISIBLE);
                layoutSettings.setVisibility(View.INVISIBLE);
            }
        });

        cancel = (Button) findViewById(R.id.cancelSettingsButton);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutFlipper.setVisibility(View.VISIBLE);
                layoutSettings.setVisibility(View.INVISIBLE);
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

    /**
     * Ot metoda, na szybko inicjalizująca obiekt mLocationRequest
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Inicjalizuje obiekt mGoogleApiClient
     * takim fajnym łańcuszkiem wywołań
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //Startujemy nasłuchiwanie zmian lokacji
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //Zatrzymujemy nasłuchiwanie o zmianach lokacji
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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

        stopLocationUpdates();
      //  db.deleteFriends();

        session.logOut();

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
        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner2.performClick();
            }
        });

    }

    public void addListenerOnSpinner() {
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) position) {
                    case 0:
                        break;
                    case 1:
                        layoutSettings.setVisibility(View.VISIBLE);
                        layoutFlipper.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        logOut(this);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void addListenerOnSpinner2() {
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) position) {
                    case 0:
                        //USTAWIENIA TO DO
                        break;
                    case 1:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpMap(boolean hardSetup) //dodałem parametr, który określa czy przy wywołaniu ma być animacja kamery
    {

            myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.myMapFragment)).getMap();
            myMap.setMyLocationEnabled(true);
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        myMap.addMarker(new MarkerOptions().position(new LatLng(51.111508, 17.060268)).title("Rondo Reagana"));
        myMap.addMarker(new MarkerOptions().position(new LatLng(51.113825, 17.065890)).title("Akademik"));


        LatLng latLng = new LatLng(0, 0);
        Criteria criteria = new Criteria();
        if (criteria != null) {
            String provider = locationManager.getBestProvider(criteria, true);
            Location myLocation = locationManager.getLastKnownLocation(provider);


            if(mCurrentLocation!=null) {
                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();
                latLng = new LatLng(latitude, longitude);
                if(hardSetup) {
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    myMap.animateCamera(CameraUpdateFactory.zoomTo(15), 3000, null);
                }
            }
            else
                Log.d(AppController.TAG,"ostania znana lokacja jest nulem");
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


    /**
     * Tutaj definiujemy jakie operacje mają się odbyć po połączeniu z google service
     *
     * @param bundle - obiekt zawieta ewentualne dane zwracane przez usługę
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(AppController.TAG, "Podłączony do api service");
        mCurrentLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        setUpMap(true);
        setMapListener();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Metoda intefejsu LocationListener
     *
     * @param location - po prostu zwraca obiekt z danymi o aktualnej lokalizacji
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(AppController.TAG, "lokalizacja zostła zaktualizowana");
        float latitude=(float)location.getLatitude();
        float longitude=(float)location.getLongitude();
        //Toast.makeText(getApplicationContext(), "Szerokość + " + latitude + " Długość: " + longitude, Toast.LENGTH_SHORT).show();
        sendCordinate(session.getUserId(), (float) latitude, (float) longitude);
    }

    public static Bitmap clipBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

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

    protected void preparePoiPoints() throws IOException {

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location myLocation = locationManager.getLastKnownLocation(provider);

        markersKfc = poiBase.getJsonWithSelectedData(0,0,new LatLng(myLocation.getLatitude(),myLocation.getLongitude()),"kfclogo");
        markersMcdonalds = poiBase.getJsonWithSelectedData(0,1,new LatLng(myLocation.getLatitude(),myLocation.getLongitude()),"mcdonaldslogo");
        markersRestaurants = poiBase.getJsonWithSelectedData(1, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "restaurant" );
        markersBars = poiBase.getJsonWithSelectedData(2, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "bar");
        markersCoffee = poiBase.getJsonWithSelectedData(3, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "coffee" );
        markersNightClubs = poiBase.getJsonWithSelectedData(4, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "nightclub" );
        markersParks = poiBase.getJsonWithSelectedData(5, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "park" );
        markersShoppingMalls= poiBase.getJsonWithSelectedData(6, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "shoppingmall" );
        markersShops= poiBase.getJsonWithSelectedData(7, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "shop" );
        markersMarkets = poiBase.getJsonWithSelectedData(8, new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), "market" );



    }

    public void setMapListener()
    {
        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                myMap.addMarker(new MarkerOptions().position(latLng).draggable(true));

            }
        });

        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();
                LatLng origin = new LatLng(latitude, longitude);
                latitude = marker.getPosition().latitude;
                longitude = marker.getPosition().longitude;
                LatLng dest = new LatLng(latitude, longitude);


                String url = MainActivity.this.getDirectionUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                //no to zaczynamy zabawę
                downloadTask.execute(url);
                return true;
            }
        });
    }
    /**
     * Metoda tworząca wątek, który w sumie i tak jest już nie potrzebny, bo za wysyłynie danych o
     * lokacji będzie odpowiedzialny ChangeLocationListener, przynajniej ten kod nie straszy już w onCreate()
     * @return Wątek, który ma za zadanie wysyłać co pięć sekund współrzędne do bazy danych
     */
    private Runnable createSendThread()
    {
        return new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {
                        if (session.isLoggedIn()) {

                            Criteria criteria = new Criteria();
                            String provider = locationManager.getBestProvider(criteria, true);
                            Location myLocation = locationManager.getLastKnownLocation(provider);

                            //double latitude = myLocation.getLatitude();
                            //double longitude = myLocation.getLongitude();

                            double latitude = 54.5;
                            double longitude = 19.2;
                            sendCordinate(session.getUserId(), (float) latitude, (float) longitude);
                        }
                        Thread.sleep(5000);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    //Rozpoczynam pisanie kodu odpowiedzialnego za pokazanie dokładnej trasy


private String getDirectionUrl(LatLng origin, LatLng dest){
   //Skąd wyruszamy
    String str_origin="origin="+origin.latitude+","+origin.longitude;

    //Quo vadis
    String str_dest="destination="+dest.latitude+","+dest.longitude;
    //Sensor enabled
    String sensor="sensor=false";
    //Składanie w całość, aby móc przekazać to web service
    String parameters=str_origin+"&"+str_dest+"&"+sensor;
    //Definiowanie formatu wyniku
    String output="json";
   //Złożenie końcowego łańcucha URL, może początek tego url warto zapisać jako stałą?
    String url="http://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
    return url;
}
    //no to pobierzmy tego jsona
    private String downloadUrl(String strUrl) throws IOException
    {
        Log.d("co tam ",strUrl);
        String data="";
        InputStream isStream=null;
        HttpURLConnection urlConnection=null;
        try{
            URL url=new URL(strUrl);
            //Tworzymy połęczenie przez protokół http, żeby połączyć sie z adresem url
            urlConnection=(HttpURLConnection)url.openConnection();
            //Łączymy się z nim
            urlConnection.connect();

            //No to teraz zczytujemy dane
            isStream=urlConnection.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(isStream));
            StringBuffer sb=new StringBuffer();

            String line="";
            while ((line=br.readLine())!=null)
            {
                sb.append(line);
            }
            data=sb.toString();
            br.close();
        }catch (Exception e) {
            Log.d("Exception url", e.toString());
        }
        finally {
            isStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    //Osobne zadanie do pobierania danych
    private class DownloadTask extends AsyncTask<String,Void,String>
    {
        //Pobieranie danych w innym wątku niż ten opowiedzialny za wyświetlanie grafiki
        @Override
        protected String doInBackground(String... url)
        {

            //String do przechowywanie odberanych danych
            String data="";
            try{
                data=MainActivity.this.downloadUrl(url[0]);
            }catch(Exception e)
            {
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        //Zrób w wątku wyświtlającym grafikę, potym jak wykonasz doInBackground
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            ParseTask parseTask=new ParseTask();
            //wystartuj wątek przetwrzający obiekt JSON
            parseTask.execute(result);
        }
    }

    //A class to parse the Google Places in JSON format
    private class ParseTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        //Przetwrzanie danych w wątku innym niż ten odpowiedzialny za wyświetlanie grafiki
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData)
        {
            JSONObject jObject;
            List<List<HashMap<String,String>>> routes=null;
            try{
                jObject=new JSONObject(jsonData[0]);
                DirectionsJSONParser parser=new DirectionsJSONParser();
                //Zacznij ekstrachować dane
                routes=parser.parse(jObject);

            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return routes;
        }

        //Wykonaj w wątku graficznym po wykonaniu metody doInBackground
        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result)
        {
            ArrayList<LatLng> points=null;
            PolylineOptions lineOptions=null;
            MarkerOptions markerOptions=new MarkerOptions();
            String distance="";
            String duration;
            if(result.size()<1)
            {
                Toast.makeText(MainActivity.this.getBaseContext(),"No Points",Toast.LENGTH_SHORT).show();
                return;
            }
            //Odpowidzanie wszytkich możliwych tras
            for(int i=0; i<result.size();i++)
            {
                points=new ArrayList<LatLng>();
                lineOptions=new PolylineOptions();
                //Przechodzenie i-tej drogi
                List<HashMap<String,String>> path=result.get(i);
                for(int j=0;j<path.size();j++)
                {
                    HashMap<String,String> point=path.get(j);
                    if(j==0)
                    {
                        //Zczytaj dystans z listy
                        distance=point.get("disance");
                        continue;
                    }else if(j==1)
                    {
                        //Zczytaj czas podróży
                        duration=point.get("duration");
                        continue;
                    }
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));
                    LatLng position=new LatLng(lat,lng);
                    points.add(position);
                }
                //Dodanie wszystkich punktów na drodze do LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.BLUE);

            }
            MainActivity.this.myMap.addPolyline(lineOptions);


        }
    }
    private void sendFriendshipRequest(final String id, final String email) {

        String tag_string_req = "req_friendshipRequest";
        pDialog.setMessage("Sending Request for Friendship");
        showDialog();
        final String TAG = "Friendship request";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Friendship request Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {

                        Toast.makeText(getApplicationContext(), "Pomyślnie wysłano zaproszenie", Toast.LENGTH_LONG).show();

                    } else {

                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Exception - problem z połączeniem", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Friendship request Error: " + error.toString());

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "friendshipRequest");
                params.put("sender", id);
                params.put("receiverEmail", email);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
