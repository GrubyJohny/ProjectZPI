package zpi.squad.app.grouploc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static zpi.squad.app.grouploc.POISpecies.BAR;
import static zpi.squad.app.grouploc.POISpecies.COFFEE;
import static zpi.squad.app.grouploc.POISpecies.KFC;
import static zpi.squad.app.grouploc.POISpecies.MARKET;
import static zpi.squad.app.grouploc.POISpecies.McDonald;
import static zpi.squad.app.grouploc.POISpecies.NIGHT_CLUB;
import static zpi.squad.app.grouploc.POISpecies.PARK;
import static zpi.squad.app.grouploc.POISpecies.RESTERAUNT;
import static zpi.squad.app.grouploc.POISpecies.SHOPPING_MALL;
import static zpi.squad.app.grouploc.POISpecies.STORE;
import static zpi.squad.app.grouploc.POISpecies.getRightSpecies;

public class Mapka extends Fragment implements GoogleApiClient.ConnectionCallbacks,MarkerDialog.NoticeDialogListener {

    private SupportMapFragment fragment;

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
    private ArrayList<MarkerOptions> markersFriends = new ArrayList<MarkerOptions>();//?
    private ArrayList<ArrayList<MarkerOptions>> activePoiMarkers = new ArrayList<>();
    private HashMap<POISpecies,HashMap<String,Marker>> active = new HashMap<POISpecies,HashMap<String, Marker>>();
    private HashMap<String,Polyline> visibleRouts=new HashMap<String,Polyline>();

    private boolean poiIsUpToDate = false;
    private Location mCurrentLocation;

    private Marker ostatniMarker;
    private View layoutMarker;
    private View tabs;


    private List<CustomMarker> markers;
    private HashMap<String,Marker> googleMarkers;


    private SQLiteHandler db;
    private GoogleApiClient mGoogleApiClient;
    Resources res;

    private boolean mRequestingLocationUpdates;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private ImageButton firstMarkerButton;
    private ImageButton secondMarkerButton;
    private ImageButton thirdMarkerButton;
    private ImageButton fourthMarkerButton;
    private ImageButton fifthMarkerButton;
    //private Button closeMarkerButton;
    private Button changeMapTypeButton;
    private SessionManager session;



    AppController globalVariable;

    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    int width;
    int height;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //res = getResources();

        layoutMarker = (View) getActivity().findViewById(R.id.markerLayout);
        tabs = (View) getActivity().findViewById(R.id.tabss);
        context = getActivity().getApplicationContext();
        globalVariable = (AppController) getActivity().getApplicationContext();
        db = new SQLiteHandler(getActivity().getApplicationContext());
        markers = db.getAllMarkers();
        googleMarkers=new HashMap<String,Marker>();
        for(CustomMarker m : markers){
            if(m.isSaveOnServer()){

            }
        }
        globalVariable.setMarkers(markers);

        //mRequestingLocationUpdates = true;
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        session = new SessionManager(getActivity().getApplicationContext());

        width = context.getResources().getDisplayMetrics().widthPixels;
        height = context.getResources().getDisplayMetrics().heightPixels;
    }

    private void setupPoiButtons() {


        mainPoiButton = (Button) getActivity().findViewById(R.id.buttonPOIFiltering);

        clearPoiButton = (Button) getActivity().findViewById(R.id.ButtonClearPoi);
        clearPoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<HashMap<String,Marker>> allMarkers= active.values();
                for(HashMap<String,Marker> markerMap:allMarkers) {
                    Collection<Marker> markerList= markerMap.values();
                    for (Marker m : markerList){
                        m.remove();
                    }
                }
                active.clear();
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
                    layoutMarker.setBackgroundColor(Color.LTGRAY);
                    globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    changeMapTypeButton.setText("Hybrid");
                } else {
                    layoutMarker.setBackgroundColor(Color.parseColor("#EEEEEE"));
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
                if (active.containsKey(BAR))
                    deleteSelectedPOIFromMap(BAR);
                else addSelectedPOItoMap(markersBars,BAR);

            }
        });

        myButtonFoodCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(COFFEE))
                    deleteSelectedPOIFromMap(COFFEE);
                else addSelectedPOItoMap(markersCoffee,COFFEE);
            }
        });

        myButtonFoodKfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(KFC))
                    deleteSelectedPOIFromMap(KFC);
                else addSelectedPOItoMap(markersKfc,KFC);
            }
        });

        myButtonFoodMcDonald.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(McDonald))
                    deleteSelectedPOIFromMap(McDonald);
                else addSelectedPOItoMap(markersMcdonalds,McDonald);
            }
        });

        myButtonFoodRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(RESTERAUNT))
                    deleteSelectedPOIFromMap(RESTERAUNT);
                else addSelectedPOItoMap(markersRestaurants,RESTERAUNT);
            }
        });


        myButtonShopsMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(MARKET))
                    deleteSelectedPOIFromMap(MARKET);
                else addSelectedPOItoMap(markersMarkets,MARKET);
            }
        });

        myButtonShopsStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(STORE))
                    deleteSelectedPOIFromMap(STORE);
                else addSelectedPOItoMap(markersShops,STORE);
            }
        });

        myButtonShopsShoppingMalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(SHOPPING_MALL))
                    deleteSelectedPOIFromMap(SHOPPING_MALL);
                else addSelectedPOItoMap(markersShoppingMalls,SHOPPING_MALL);
            }
        });


        myButtonLeisureClubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(NIGHT_CLUB))
                    deleteSelectedPOIFromMap(NIGHT_CLUB);
                else addSelectedPOItoMap(markersNightClubs,NIGHT_CLUB);
            }
        });
        myButtonLeisureParks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active.containsKey(PARK))
                    deleteSelectedPOIFromMap(PARK);
                else addSelectedPOItoMap(markersParks,PARK);
            }
        });
    }

    private void hide2OptionsFromMarker() {
        thirdMarkerButton.setVisibility(View.GONE);
        fourthMarkerButton.setVisibility(View.GONE);
    }

    private void hide3OptionsFromMarker() {
        thirdMarkerButton.setVisibility(View.GONE);
        fourthMarkerButton.setVisibility(View.GONE);
        fifthMarkerButton.setVisibility(View.GONE);
    }


    private void showSomeOptionsFromMarker() {
        thirdMarkerButton.setVisibility(View.VISIBLE);
        fourthMarkerButton.setVisibility(View.VISIBLE);
        fifthMarkerButton.setVisibility(View.VISIBLE);
    }

/*    private void markersSelectionChanged() {
        globalVariable.getMyMap().clear();

        for (ArrayList<MarkerOptions> marks : activePoiMarkers) {
            for (int i = 0; i < marks.size(); i++)
                globalVariable.getMyMap().addMarker(marks.get(i));
        }

        for (int i = 0; i < markersFriends.size(); i++)
            globalVariable.getMyMap().addMarker(markersFriends.get(i));


        Sender.putMarkersOnMapAgain(markers, globalVariable.getMyMap(), null);
    }*/

    public void addSelectedPOItoMap(List<MarkerOptions> POItoAdd,POISpecies tag)
    {
        HashMap<String,Marker> googleMarkers=new HashMap<>();
        for(MarkerOptions markOption :POItoAdd)
        {
            Marker marker= globalVariable.getMyMap().addMarker(markOption.snippet("POI,"+tag));
            googleMarkers.put(marker.getId(), marker);
        }
        active.put(tag,googleMarkers);
    }

    public void deleteSelectedPOIFromMap(POISpecies tag)
    {
        Collection<Marker> markers=active.remove(tag).values();
        for(Marker m:markers) {
            if(visibleRouts.containsKey(m.getId()))
                deleteRoute(m.getId());
            m.remove();

        }

    }
    public void deleteRoute(String tag)
    {
        Polyline route=visibleRouts.remove(tag);
        route.remove();
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

        firstMarkerButton = (ImageButton) getActivity().findViewById(R.id.firstButton);
        secondMarkerButton = (ImageButton) getActivity().findViewById(R.id.secondButton);
        thirdMarkerButton = (ImageButton) getActivity().findViewById(R.id.thirdButton);
        fourthMarkerButton = (ImageButton) getActivity().findViewById(R.id.fourthButton);
        fifthMarkerButton = (ImageButton) getActivity().findViewById(R.id.fifthButton);
        /*closeMarkerButton = (Button) getActivity().findViewById(R.id.closeMarkerButton);

        closeMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMarker.setVisibility(View.GONE);
            }
        });*/




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
                markerDialog.setTargetFragment(Mapka.this,0);
                markerDialog.show(getFragmentManager(), "Marker Dialog");




              //  layoutMarker.setVisibility(View.GONE);
            }
        });


        globalVariable.getMyMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ostatniMarker = marker;
                globalVariable.getMyMap().animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                layoutMarker.setX((float) globalVariable.getMyMap().getProjection().toScreenLocation(marker.getPosition()).x - layoutMarker.getWidth() / 2);
                layoutMarker.setY((float) globalVariable.getMyMap().getProjection().toScreenLocation(marker.getPosition()).y - layoutMarker.getHeight() + 180);
                TextView name = (TextView) layoutMarker.findViewById(R.id.titleOfMarker);
                Log.d("tittle", name + "");
                name.setText(marker.getTitle());
                getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
                String snippet = marker.getSnippet();
                String ids[] = snippet.split(",");

                if(ids[0].equals("POI")){ // jest POI
                    hide2OptionsFromMarker();
                    Log.d("MARKERY", "POI");
                }
                else if(ids[0].isEmpty()){ // nie ma na serwerze
                    showSomeOptionsFromMarker();
                    Log.d("MARKERY", "nie ma na serwerze");
                }
                else if(ids[1].isEmpty()){ // nie ma w sqllite
                    thirdMarkerButton.setVisibility(View.GONE);
                    Log.d("MARKERY", "nie ma w sqllite");
                }
                else if(ids.length == 1){ // znajomy
                    hide3OptionsFromMarker();
                    Log.d("MARKERY", "znajomy");
                }

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
        Log.d("PUT START","jedziemy z ustawianiem mapy");

        globalVariable.setMyMap(((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.myMapFragment)).getMap());
        //Log.d(AppController.TAG,"my map to"+myMap);
        globalVariable.getMyMap().setMyLocationEnabled(true);

        globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);

        Sender.putMarkersOnMapAgain(markers, globalVariable.getMyMap(),googleMarkers);

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
                    if (/*layoutMarker.getX() < 0 || */layoutMarker.getY() < (tabs.getY() + tabs.getMeasuredHeight() * 2) /*|| layoutMarker.getX() + layoutMarker.getWidth() > width || layoutMarker.getY() + layoutMarker.getHeight() > height*/) {
                        layoutMarker.setVisibility(View.GONE);
                    } else {
                        layoutMarker.setX((float) globalVariable.getMyMap().getProjection().toScreenLocation(ostatniMarker.getPosition()).x - layoutMarker.getWidth() / 2);
                        layoutMarker.setY((float) globalVariable.getMyMap().getProjection().toScreenLocation(ostatniMarker.getPosition()).y - layoutMarker.getHeight() + 180);
                    }
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
        setMapListener();
        setupPoiButtons();

        if (mRequestingLocationUpdates) {
            // startLocationUpdates();
        }
    }

    /*
        //Startujemy nas�uchiwanie zmian lokacji
        protected void startLocationUpdates() {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        //Zatrzymujemy nas�uchiwanie o zmianach lokacji
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
            //Tworzymy po��czenie przez protok� http, �eby po��czy� sie z adresem url
            urlConnection = (HttpURLConnection) url.openConnection();
            //��czymy si� z nim
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
        String markID;
        public DownloadTask (String markID)
        {
            super();
            this.markID=markID;
        }
        //Pobieranie danych w innym w�tku ni� ten opowiedzialny za wy�wietlanie grafiki
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

        //Zr�b w w�tku wy�witlaj�cym grafik�, potym jak wykonasz doInBackground
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParseTask parseTask = new ParseTask(markID);
            //wystartuj w�tek przetwrzaj�cy obiekt JSON
            parseTask.execute(result);
        }
    }

    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
       String markID;
        public ParseTask(String markID)
        {
            this.markID=markID;
        }

        //Przetwrzanie danych w w�tku innym ni� ten odpowiedzialny za wy�wietlanie grafiki
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                //Zacznij ekstrachowa� dane
                routes = parser.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        //Wykonaj w w�tku graficznym po wykonaniu metody doInBackground
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
            //Odpowidzanie wszytkich mo�liwych tras
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
                        //Zczytaj czas podr�y
                        duration = point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                //Dodanie wszystkich punkt�w na drodze do LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);

            }
            //Dodaje do słownika z wszystkimi drogami
           Polyline polyline= globalVariable.getMyMap().addPolyline(lineOptions);
            visibleRouts.put(markID,polyline);


        }
    }


    private String getDirectionUrl(LatLng origin, LatLng dest) {
        //Sk�d wyruszamy
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        //Quo vadis
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Sensor enabled
        String sensor = "sensor=false";
        //Sk�adanie w ca�o��, aby m�c przekaza� to web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        //Definiowanie formatu wyniku
        String output = "json";
        //Z�o�enie ko�cowego �a�cucha URL, mo�e pocz�tek tego url warto zapisa� jako sta��?
        String url = "http://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    public void inclizaidListenerForMarkerMenu() {
        firstMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(AppController.TAG, "odebra�em zdarzenie");

               // String[] ids = ostatniMarker.getSnippet().split(",");
                String markerId = ostatniMarker.getId();

                if(visibleRouts.containsKey(markerId))
                {
                    deleteRoute(markerId);
                    Log.d("Fuck","usuwam");
                }else{
                    double latitude = mCurrentLocation.getLatitude();
                    double longitude = mCurrentLocation.getLongitude();
                    LatLng origin = new LatLng(latitude, longitude);
                    latitude = ostatniMarker.getPosition().latitude;
                    longitude = ostatniMarker.getPosition().longitude;

                    LatLng dest = new LatLng(latitude, longitude);
                    String url = getDirectionUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask(markerId);

                    //no to zaczynamy zabaw�
                    downloadTask.execute(url);
                }
                layoutMarker.setVisibility(View.GONE);


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

        fifthMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = session.getUserId();
                double latitude = ostatniMarker.getPosition().latitude;
                double longitude = ostatniMarker.getPosition().longitude;
                CustomMarker custom = ToolsForMarkerList.getSpecificMarkerByLatitudeAndLongitude(markers, latitude, longitude);
                String name = ostatniMarker.getTitle();
                if (name == null)
                    name = "brak";
                layoutMarker.setVisibility(View.GONE);

                Sender.shareMarker(getActivity().getApplicationContext(), uid, name, Double.toString(latitude), Double.toString(longitude));
            }
        });

        //Zapisywanie na stałe
        thirdMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = session.getUserId();
                double latitude = ostatniMarker.getPosition().latitude;
                double longitude = ostatniMarker.getPosition().longitude;
                CustomMarker custom = ToolsForMarkerList.getSpecificMarkerByLatitudeAndLongitude(markers, latitude, longitude);
                String name = ostatniMarker.getTitle();
                if (name == null)
                    name = "brak";
                layoutMarker.setVisibility(View.GONE);
                ostatniMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarkerhiblue));

                Sender.sendMarker(getActivity().getApplicationContext(), custom, ostatniMarker, db);
            }
        });

        fourthMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String snippet = ostatniMarker.getSnippet();
                String[] ids = snippet.split(",");
                String markId= ostatniMarker.getId();
                if(ids.length==1)
                //Przyjaciel
                {
                    Toast.makeText(getActivity(),"Fukcjonalność jeszcze nie zaimplementowana",Toast.LENGTH_SHORT).show();
                }
                else
                    if(ids[0].equals("POI"))
                    //POI
                    {
                        String kind=ids[1];
                        POISpecies species=getRightSpecies(kind);
                        HashMap<String,Marker> mapWithSelectedKindOfPOI=active.get(species);
                        Marker m=mapWithSelectedKindOfPOI.remove(markId);
                        m.remove();
                        if(mapWithSelectedKindOfPOI.isEmpty())
                            active.remove(species);


                    }else
                    {
                        String markerIdMySql = ids[0];
                        String markerIdSQLITE = ids[1];
                        Log.d("REMOVE_MARKER", "MySQL id " + markerIdMySql);
                        Log.d("REMOVE_MARKER", "SQLite id " + markerIdSQLITE);
                        CustomMarker toRemove = ToolsForMarkerList.getSpecificMarker(markers, markerIdSQLITE);
                        markers.remove(toRemove);
                        boolean znacznik = db.removeMarker(markerIdSQLITE);
                        Log.d("REMOVE_MARKER", " Operacja usuwania zako�czy�a si� sukcesem" + znacznik);
                        if (toRemove.isSaveOnServer()) {
                            Log.d("REMOVE_MARKER", "Usuwam z serwera");
                            Sender.sendRequestAboutRemoveMarker(markerIdMySql, globalVariable.getMyMap(), markers);
                        }

                        Marker marker=googleMarkers.remove(markerIdSQLITE);
                        Log.d("REMOVE_MARKER",marker.getSnippet()+" "+markerIdSQLITE+"OSTATNI"+ostatniMarker.getSnippet());
                        ostatniMarker.remove();


                    }
                if (visibleRouts.containsKey(markId))
                    deleteRoute(markId);




                layoutMarker.setVisibility(View.GONE);
                /*Sender.sendRequestAboutMarkers(session.getUserId(),markers,myMap);*/


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

    @Override
    public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog) {

        MarkerDialog md = (MarkerDialog) dialog;
        String name = md.getName();
        Log.d("Marker Dialog", name);
        Log.d("Marker Dialog", "myMap " + globalVariable.getMyMap());
        Log.d("Marker Dialog", "my LatLong " + globalVariable.getLastClikOnMap());



        CustomMarker nowyMarker = new CustomMarker(null, session.getUserId(), globalVariable.getLastClikOnMap().latitude, globalVariable.getLastClikOnMap().longitude, name);
        globalVariable.addToMarkers(nowyMarker);
        long id = db.addMarker(nowyMarker);
        nowyMarker.setMarkerIdSQLite(Long.toString(id));
        String markerIdExtrenal = "NULL";
        String markerIdInteler = Long.toString(id);
        Marker marker=globalVariable.getMyMap().addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarkerhi)).position(globalVariable.getLastClikOnMap()).draggable(true).title(name).snippet(markerIdExtrenal + "," + markerIdInteler));
        googleMarkers.put(markerIdInteler,marker);
        Log.d("ADD_MARKER", markerIdExtrenal + "," + markerIdInteler);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(md.getInput().getWindowToken(), 0);
    }

}
