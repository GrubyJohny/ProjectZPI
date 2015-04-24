package com.example.marcin.lokalizator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private WebView myMapView;
    private Button poiFilteringButton;
    private Button confirm;
    private Button cancel;
    private RadioGroup radioGroupPoi;
    private HashMap<Integer, Marker> visibleMarkers = new HashMap<Integer, Marker>();
    private List<MarkerOptions> markers;
    private ArrayList<MarkerOptions> markersKFC;
    private ArrayList<MarkerOptions> markersMcDonalds;
    //Andoridowy obiekt przechowujący dane o położeniu(np latitude, longitude, kiedy zostało zarejestrowane)
    private Location mCurrentLocation;
    private Runnable sender;//wątek, który będzie wysyłał info o położoniu użytkownika do bazy
    private SQLiteHandler db;//obiekt obsługujący lokalną androidową bazę danych
    //obiekt będący parametrem, przy wysłaniu żądania o aktualizację lokacji
    private LocationRequest mLocationRequest;

    //Obiekt w ogólności reprezentujący googlowe api service,
    //jest często przekazywany jako argument, gdy coś o tego api chcemy
    private GoogleApiClient mGoogleApiClient;

    //Flaga muwiąca o tym czy chcemy monitorować lokalizację
    private boolean mRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new SQLiteHandler(getApplicationContext());

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

        session = new SessionManager(this);
        new Thread(sender, "Watek do wysyłania koordynatów").start();

        mainSpinner();
        notifications();

        circleButton = (ImageButton) findViewById(R.id.circleButton);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.kfclogo);
        Bitmap bitmap_round = clipBitmap(icon);
        circleButton.setImageBitmap(bitmap_round);

        noticeButton = (Button) findViewById(R.id.noticeButton);

        addListenerOnButton();
        addListenerOnSpinner();
        addListenerOnSpinner2();

        radioButtonyGrubego();

        setUpViewFlipper();
        //setUpMap();
        //setupMapWebView();
        preparePoiPoints();
        //setMapListener();

        layoutSettings = (View) findViewById(R.id.settingsLayout);
        layoutFlipper = (View) findViewById(R.id.flipperLayout);

        SettingButtons();
    }

    private void radioButtonyGrubego() {
        final RadioButton radioAll = (RadioButton) findViewById(R.id.radioButton3);
        radioAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersMcDonalds.size(); i++)
                    myMap.addMarker(markersMcDonalds.get(i));

                for (int i = 0; i < markersKFC.size(); i++)
                    myMap.addMarker(markersKFC.get(i));
            }

        });
        final RadioButton radiokfc = (RadioButton) findViewById(R.id.radioButton);
        radiokfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myMap.clear();
                for (int i = 0; i < markersKFC.size(); i++) {
                    myMap.addMarker(markersKFC.get(i));

                }
            }


        });
        final RadioButton radioMcDonalds = (RadioButton) findViewById(R.id.radioButton2);
        radioMcDonalds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                for (int i = 0; i < markersMcDonalds.size(); i++) {
                    myMap.addMarker(markersMcDonalds.get(i));

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

    private void addListenerOnPoiButton() {
        poiFilteringButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if (findViewById(R.id.radioButton).getVisibility() == View.INVISIBLE) {
                    (findViewById(R.id.radioButton)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.radioButton2)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.radioButton3)).setVisibility(View.VISIBLE);
                } else {
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
                Toast tost = Toast.makeText(getApplicationContext(), "Hip, Hip + " + c2, Toast.LENGTH_SHORT);
                tost.show();


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

    private void setUpMap() {
        myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.myMapFragment)).getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.setMyLocationEnabled(true);


        myMap.addMarker(new MarkerOptions().position(new LatLng(51.111508, 17.060268)).title("Rondo Reagana"));
        myMap.addMarker(new MarkerOptions().position(new LatLng(51.113825, 17.065890)).title("Akademik"));
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng latLng = new LatLng(0, 0);
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

        // View placeHolder = findViewById(R.id.placeHolderFragment);

      /*  FriendsFragment friendsFragment = FriendsFragment.newInstance("","");
        android.support.v4.app.FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.placeHolderFragment, friendsFragment).commit();
*/

        //View view1 = View.inflate(this, R.layout.fragment_friends, null);
        // myViewFlipper.addView(view1);
    }

    private void setupMapWebView() {
        myMapView = (WebView) findViewById(R.id.myMapFragment);
        myMapView.clearCache(true);
        myMapView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = myMapView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myMapView.addJavascriptInterface(new WebAppInterface(this), "Android");
        myMapView.loadUrl("https://www.google.com/maps/d/edit?mid=zHXxWf8z-mCE.k-4RjVSIl5O8");

    }

    /**
     * Tutaj definiujemy jakie operacje mają się odbyć po połączeniu z google service
     *
     * @param bundle - obiekt zawieta ewentualne dane zwracane przez usługę
     */
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
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

    private void preparePoiPoints() {
        //add KFC
        markersKFC = new ArrayList<MarkerOptions>();
        markersKFC.add(new MarkerOptions().position(new LatLng(51.088720, 16.999470)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.098590, 17.037649)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.099488, 17.028555)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.107786, 17.032295)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.108399, 17.039837)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.112278, 17.059493)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.119768, 16.989862)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.131706, 17.062039)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
        markersKFC.add(new MarkerOptions().position(new LatLng(51.142201, 17.088718)).title("KFC").icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));

        //add Mcdonalds
        markersMcDonalds = new ArrayList<MarkerOptions>();
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.106616, 16.948973)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.047800, 16.958560)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.054236, 16.975421)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.119674, 16.988372)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.060817, 16.993000)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.118044, 16.999969)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.116333, 17.024023)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.150980, 17.026410)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.109436, 17.033035)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.108075, 17.037220)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.108230, 17.039610)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.082358, 17.048646)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.095464, 17.056789)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.112505, 17.059547)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
        markersMcDonalds.add(new MarkerOptions().position(new LatLng(51.142060, 17.088680)).title("McDonald's").icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));

        //add markers
        /*for (int i = 0; i < markersKFC.size(); i++)
            myMap.addMarker(markersKFC.get(i));

        for (int i = 0; i < markersMcDonalds.size(); i++)
            myMap.addMarker(markersMcDonalds.get(i));*/


        poiFilteringButton = (Button) findViewById(R.id.buttonPOIFiltering);
        addListenerOnPoiButton();
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

                return false;
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
                            sendCordinate(db.getId(), (float) latitude, (float) longitude);
                        }
                        Thread.sleep(5000);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
