package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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


public class Mapka extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private SupportMapFragment fragment;

    public GoogleMap getMyMap() {
        return myMap;
    }

    public GoogleMap myMap;
    private static View view;
    //OstatniKliknietyNaMapi
    private LatLng lastClikOnMap;
    private ScrollView POIScrollView;
    private Button mainPoiButton;
    private Button clearPoiButton;
    PoiJSONParser poiBase = new PoiJSONParser();
    public static Context context;
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
    private ArrayList<MarkerOptions> markersFriends = new ArrayList<MarkerOptions>();
    private ArrayList<ArrayList<MarkerOptions>> activePoiMarkers = new ArrayList<>();
    private boolean poiIsUpToDate = false;
    private Location mCurrentLocation;

    private Marker ostatniMarker;
    private View layoutMarker;


    private List<CustomMarker> markers;

    private SQLiteHandler db;
    private GoogleApiClient mGoogleApiClient;
    Resources res;

    private boolean mRequestingLocationUpdates;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private Button firstMarkerButton;
    private Button secondMarkerButton;
    private Button thirdMarkerButton;
    private Button fourthMarkerButton;
    private Button closeMarkerButton;
    private Button changeMapTypeButton;
    private SessionManager session;

    AppController globalVariable;

    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //res = getResources();

        layoutMarker = (View) getActivity().findViewById(R.id.markerLayout);
        context = getActivity().getApplicationContext();
        globalVariable = (AppController) getActivity().getApplicationContext();
        db = new SQLiteHandler(getActivity().getApplicationContext());
        markers = db.getAllMarkers();


        //mRequestingLocationUpdates = true;
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();


        session = new SessionManager(getActivity().getApplicationContext());


    }

    private void setupPoiButtons() {


        mainPoiButton = (Button) getActivity().findViewById(R.id.buttonPOIFiltering);

        clearPoiButton = (Button) getActivity().findViewById(R.id.ButtonClearPoi);
        clearPoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalVariable.getMyMap().clear();
                activePoiMarkers.clear();
                mainPoiButton.performClick();
                Sender.putMarkersOnMapAgain(markers, globalVariable.getMyMap());
                //setUpMap(false);
            }

        });

        POIScrollView = (ScrollView) getActivity().findViewById(R.id.POIScroll);

        mainPoiButton.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {
                layoutMarker.setVisibility(View.GONE);

                if ((getActivity().findViewById(R.id.POIButtons)).getVisibility() == View.GONE) {

                    if (!poiIsUpToDate) {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.execute();

                    }
                    POIScrollView.scrollTo(0, 0);
                    getActivity().findViewById(R.id.POIButtons).setVisibility(View.VISIBLE);
                } else {
                    getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
                }
            }

        });

        changeMapTypeButton = (Button) getActivity().findViewById(R.id.changeMapTypeButton);
        changeMapTypeButton.setText("Normal");

        changeMapTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalVariable.getMyMap().getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                    globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    changeMapTypeButton.setText("Hybrid");
                } else {
                    globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    changeMapTypeButton.setText("Normal");
                }
                //showMapTypeSelectorDialog();
            }
        });

        //final Button myButtonFood = (Button) findViewById(R.id.ButtonFood);
        final ImageButton myButtonFoodBar = (ImageButton) getActivity().findViewById(R.id.ButtonFoodBar);
        final ImageButton myButtonFoodCoffee = (ImageButton) getActivity().findViewById(R.id.ButtonFoodCoffee);
        final ImageButton myButtonFoodKfc = (ImageButton) getActivity().findViewById(R.id.ButtonFoodKfc);
        final ImageButton myButtonFoodMcDonald = (ImageButton) getActivity().findViewById(R.id.ButtonFoodMcDonald);
        final ImageButton myButtonFoodRestaurant = (ImageButton) getActivity().findViewById(R.id.ButtonFoodRestaurant);
        final ImageButton myButtonShopsMarket = (ImageButton) getActivity().findViewById(R.id.ButtonShopsMarket);
        final ImageButton myButtonShopsStores = (ImageButton) getActivity().findViewById(R.id.ButtonShopsStores);
        final ImageButton myButtonShopsShoppingMalls = (ImageButton) getActivity().findViewById(R.id.ButtonShopsShoppingMall);
        //final Button myButtonShops = (Button) findViewById(R.id.ButtonShops);
        final ImageButton myButtonLeisureClubs = (ImageButton) getActivity().findViewById(R.id.ButtonLeisureClubs);
        final ImageButton myButtonLeisureParks = (ImageButton) getActivity().findViewById(R.id.ButtonLeisureParks);
        //final Button myButtonLeisure = (Button) findViewById(R.id.ButtonLeisure);

        myButtonFoodBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersBars))
                    activePoiMarkers.remove(markersBars);
                else activePoiMarkers.add(markersBars);

                markersSelectionChanged();
            }
        });

        myButtonFoodCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersCoffee))
                    activePoiMarkers.remove(markersCoffee);
                else activePoiMarkers.add(markersCoffee);

                markersSelectionChanged();
            }
        });

        myButtonFoodKfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersKfc))
                    activePoiMarkers.remove(markersKfc);
                else activePoiMarkers.add(markersKfc);

                markersSelectionChanged();
            }
        });

        myButtonFoodMcDonald.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersMcdonalds))
                    activePoiMarkers.remove(markersMcdonalds);
                else activePoiMarkers.add(markersMcdonalds);

                markersSelectionChanged();
            }
        });

        myButtonFoodRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersRestaurants))
                    activePoiMarkers.remove(markersRestaurants);
                else activePoiMarkers.add(markersRestaurants);

                markersSelectionChanged();
            }
        });


        myButtonShopsMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersMarkets))
                    activePoiMarkers.remove(markersMarkets);
                else activePoiMarkers.add(markersMarkets);

                markersSelectionChanged();
            }
        });

        myButtonShopsStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersShops))
                    activePoiMarkers.remove(markersShops);
                else activePoiMarkers.add(markersShops);

                markersSelectionChanged();
            }
        });

        myButtonShopsShoppingMalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersShoppingMalls))
                    activePoiMarkers.remove(markersShoppingMalls);
                else activePoiMarkers.add(markersShoppingMalls);

                markersSelectionChanged();
            }
        });


        myButtonLeisureClubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersNightClubs))
                    activePoiMarkers.remove(markersNightClubs);
                else activePoiMarkers.add(markersNightClubs);

                markersSelectionChanged();
            }
        });
        myButtonLeisureParks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePoiMarkers.contains(markersParks))
                    activePoiMarkers.remove(markersParks);
                else activePoiMarkers.add(markersParks);

                markersSelectionChanged();
            }
        });
    }

    private void markersSelectionChanged() {
        globalVariable.getMyMap().clear();

        for (ArrayList<MarkerOptions> marks : activePoiMarkers) {
            for (int i = 0; i < marks.size(); i++)
                globalVariable.getMyMap().addMarker(marks.get(i));
        }

        for (int i = 0; i < markersFriends.size(); i++)
            globalVariable.getMyMap().addMarker(markersFriends.get(i));


        Sender.putMarkersOnMapAgain(markers, globalVariable.getMyMap());
    }


    class AsyncTaskRunner extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                preparePoiPoints();
                Log.d("POI JOHNY", "poi gotowe");
                poiIsUpToDate = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            String resp = "done";
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation

        }

        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        @Override
        protected void onProgressUpdate(String... text) {

            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_mapka, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return view;

        // return inflater.inflate(R.layout.fragment_mapka, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setUpMap(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.myMapFragment);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.myMapFragment, fragment).commit();
        }

        firstMarkerButton = (Button) getActivity().findViewById(R.id.firstButton);
        secondMarkerButton = (Button) getActivity().findViewById(R.id.secondButton);
        thirdMarkerButton = (Button) getActivity().findViewById(R.id.thirdButton);
        fourthMarkerButton = (Button) getActivity().findViewById(R.id.fourthButton);
        closeMarkerButton = (Button) getActivity().findViewById(R.id.closeMarkerButton);

        closeMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMarker.setVisibility(View.GONE);
            }
        });

        setUpMap(false);
        setMapListener();
        setupPoiButtons();

        inclizaidListenerForMarkerMenu();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (globalVariable.getMyMap() == null) {
            globalVariable.setMyMap(fragment.getMap());
            //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        }
    }

    protected void preparePoiPoints() throws IOException {

        markersKfc = poiBase.getJsonWithSelectedData(0, 0, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "kfclogo");
        markersMcdonalds = poiBase.getJsonWithSelectedData(0, 1, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "mcdonaldslogo");
        markersRestaurants = poiBase.getJsonWithSelectedData(1, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "restaurant");
        markersBars = poiBase.getJsonWithSelectedData(2, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "bar");
        markersCoffee = poiBase.getJsonWithSelectedData(3, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "coffee");
        markersNightClubs = poiBase.getJsonWithSelectedData(4, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "nightclub");
        markersParks = poiBase.getJsonWithSelectedData(5, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "park");
        markersShoppingMalls = poiBase.getJsonWithSelectedData(6, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "shoppingmall");
        markersShops = poiBase.getJsonWithSelectedData(7, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "shop");
        markersMarkets = poiBase.getJsonWithSelectedData(8, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), "market");
    }

    public void setMapListener() {
        globalVariable.getMyMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                globalVariable.setLastClikOnMap(latLng);
                MarkerDialog markerDialog = new MarkerDialog();
                markerDialog.show(getChildFragmentManager(), "Marker Dialog");

                globalVariable.getMyMap().addMarker(new MarkerOptions().position(latLng).draggable(true));
                layoutMarker.setVisibility(View.GONE);
            }
        });

        globalVariable.getMyMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();
                LatLng origin = new LatLng(latitude, longitude);
                latitude = marker.getPosition().latitude;
                longitude = marker.getPosition().longitude;
                LatLng dest = new LatLng(latitude, longitude);

             /*   Uri gmmIntentUri= Uri.parse("google.navigation:q="+latitude+","+longitude);
                Intent mapIntent=new Intent(Intent.ACTION_VIEW,gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);*/

                /*String url = getActivity().getDirectionUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();

                downloadTask.execute(url);*/
                return true;
            }
        });

        globalVariable.getMyMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ostatniMarker = marker;
                layoutMarker.setX((float) globalVariable.getMyMap().getProjection().toScreenLocation(marker.getPosition()).x - layoutMarker.getWidth() / 2);
                layoutMarker.setY((float) globalVariable.getMyMap().getProjection().toScreenLocation(marker.getPosition()).y - layoutMarker.getHeight() + 180);
                getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
                layoutMarker.setVisibility(View.VISIBLE);
                return true;
            }
        });

        globalVariable.getMyMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
                layoutMarker.setVisibility(View.GONE);
            }
        });
    }

    private void setUpMap(boolean hardSetup) {


        globalVariable.setMyMap(((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.myMapFragment)).getMap());
        //Log.d(AppController.TAG,"my map to"+myMap);
        globalVariable.getMyMap().setMyLocationEnabled(true);

        globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);

        Sender.putMarkersOnMapAgain(markers, globalVariable.getMyMap());

        if (mCurrentLocation != null) {
            double latitude = mCurrentLocation.getLatitude();
            double longitude = mCurrentLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            if (hardSetup) {

                globalVariable.getMyMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                //myMap.animateCamera(CameraUpdateFactory.zoomTo(15),3000,null);
            }
        } else
            Log.e(AppController.TAG, "ostania znana lokacja jest nulem");


        globalVariable.getMyMap().setOnCameraChangeListener(getCameraChangeListener());

    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (ostatniMarker != null) {
                    layoutMarker.setX((float) globalVariable.getMyMap().getProjection().toScreenLocation(ostatniMarker.getPosition()).x - layoutMarker.getWidth() / 2);
                    layoutMarker.setY((float) globalVariable.getMyMap().getProjection().toScreenLocation(ostatniMarker.getPosition()).y - layoutMarker.getHeight() + 180);
                }
                getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
                //addItemsToMap(markers);
            }
        };
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(AppController.TAG, "Podlaczony do api service");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        setUpMap(true);


        //setMapListener();
        if (mRequestingLocationUpdates) {
            // startLocationUpdates();
        }
    }

    /*
        //Startujemy nas³uchiwanie zmian lokacji
        protected void startLocationUpdates() {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        //Zatrzymujemy nas³uchiwanie o zmianach lokacji
        protected void stopLocationUpdates() {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    */
    @Override
    public void onConnectionSuspended(int i) {

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /*protected void startLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }*/


    private String downloadUrl(String strUrl) throws IOException {
        Log.d("co tam ", strUrl);
        String data = "";
        InputStream isStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            //Tworzymy po³êczenie przez protokó³ http, ¿eby po³¹czyæ sie z adresem url
            urlConnection = (HttpURLConnection) url.openConnection();
            //£¹czymy siê z nim
            urlConnection.connect();

            //No to teraz zczytujemy dane
            isStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(isStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception url", e.toString());
        } finally {
            isStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        //Pobieranie danych w innym w¹tku ni¿ ten opowiedzialny za wyœwietlanie grafiki
        @Override
        protected String doInBackground(String... url) {

            //String do przechowywanie odberanych danych
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        //Zrób w w¹tku wyœwitlaj¹cym grafikê, potym jak wykonasz doInBackground
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParseTask parseTask = new ParseTask();
            //wystartuj w¹tek przetwrzaj¹cy obiekt JSON
            parseTask.execute(result);
        }
    }

    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        //Przetwrzanie danych w w¹tku innym ni¿ ten odpowiedzialny za wyœwietlanie grafiki
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                //Zacznij ekstrachowaæ dane
                routes = parser.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        //Wykonaj w w¹tku graficznym po wykonaniu metody doInBackground
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration;
            if (result.size() < 1) {
                Toast.makeText(getActivity().getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
            //Odpowidzanie wszytkich mo¿liwych tras
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                //Przechodzenie i-tej drogi
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    if (j == 0) {
                        //Zczytaj dystans z listy
                        distance = point.get("disance");
                        continue;
                    } else if (j == 1) {
                        //Zczytaj czas podró¿y
                        duration = point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                //Dodanie wszystkich punktów na drodze do LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);

            }
            globalVariable.getMyMap().addPolyline(lineOptions);


        }
    }


    private String getDirectionUrl(LatLng origin, LatLng dest) {
        //Sk¹d wyruszamy
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        //Quo vadis
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Sensor enabled
        String sensor = "sensor=false";
        //Sk³adanie w ca³oœæ, aby móc przekazaæ to web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        //Definiowanie formatu wyniku
        String output = "json";
        //Z³o¿enie koñcowego ³añcucha URL, mo¿e pocz¹tek tego url warto zapisaæ jako sta³¹?
        String url = "http://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    public void inclizaidListenerForMarkerMenu() {
        firstMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(AppController.TAG, "odebra³em zdarzenie");
                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();
                LatLng origin = new LatLng(latitude, longitude);
                latitude = ostatniMarker.getPosition().latitude;
                longitude = ostatniMarker.getPosition().longitude;
                LatLng dest = new LatLng(latitude, longitude);
                String url = getDirectionUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                layoutMarker.setVisibility(View.GONE);
                //no to zaczynamy zabawê
                downloadTask.execute(url);
            }
        });
        secondMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();
                LatLng origin = new LatLng(latitude, longitude);
                latitude = ostatniMarker.getPosition().latitude;
                longitude = ostatniMarker.getPosition().longitude;
                LatLng dest = new LatLng(latitude, longitude);
                layoutMarker.setVisibility(View.GONE);

                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        thirdMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = session.getUserId();
                double latitude = ostatniMarker.getPosition().latitude;
                double longitude = ostatniMarker.getPosition().longitude;
                CustomMarker custom = ToolsForMarkerList.getSpecificMarker(markers, latitude, longitude);
                String name = ostatniMarker.getTitle();
                if (name == null)
                    name = "brak";
                layoutMarker.setVisibility(View.GONE);

                Sender.sendMarker(getActivity().getApplicationContext(), uid, latitude, longitude, name, custom, ostatniMarker);
            }
        });
        fourthMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = ostatniMarker.getPosition().latitude;
                double longitude = ostatniMarker.getPosition().longitude;
                CustomMarker toRemove = ToolsForMarkerList.getSpecificMarker(markers, latitude, longitude);
                markers.remove(toRemove);
                globalVariable.getMyMap().clear();
                Sender.putMarkersOnMapAgain(markers, globalVariable.getMyMap());
                layoutMarker.setVisibility(View.GONE);
                /*Sender.sendRequestAboutMarkers(session.getUserId(),markers,myMap);*/


             /*   ArrayList<Friend> friends=db.getAllFriends();
                String whereClause=Sender.makeStatementAboutFriendsList(friends);

                ArrayList<CustomMarker> friendsMarker=new ArrayList<CustomMarker>();
                Sender.sendRequestAboutFriendsCoordinate(whereClause,friendsMarker,myMap);*/
            }
        });
    }

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = globalVariable.getMyMap().getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

}
