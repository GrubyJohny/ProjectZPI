package zpi.squad.app.grouploc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Mapka extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private SupportMapFragment fragment;
    private GoogleMap myMap;
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
    //lista aktywnych markerów poi, które maj¹ byæ wyœwietlane na mapie
    private ArrayList<ArrayList<MarkerOptions>> activePoiMarkers = new ArrayList<>();
    //¿eby nie trzeba by³o za ka¿dym razem generowaæ poi;
    //przy ruchu kamer¹ jednak chyba trzeba bêdzie coœ z tym jeszcze pokombinowaæ
    private boolean poiIsUpToDate = false;
    private Location mCurrentLocation;

    private Marker ostatniMarker;
    private View layoutMarker;


    private List<CustomMarker> markers;

    private SQLiteHandler db;
    private GoogleApiClient mGoogleApiClient;
    Resources res;

    private boolean mRequestingLocationUpdates;

    private LocationRequest mLocationRequest;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();

        layoutMarker = (View) getActivity().findViewById(R.id.markerLayout);
        context = getActivity().getApplicationContext();
        db = new SQLiteHandler(getActivity().getApplicationContext());
        markers=db.getAllMarkers();

        mRequestingLocationUpdates = true;
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mCurrentLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);



    }

    private void setupPoiButtons() {

        mainPoiButton = (Button) getActivity().findViewById(R.id.buttonPOIFiltering);

        clearPoiButton = (Button) getActivity().findViewById(R.id.ButtonClearPoi);
        clearPoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMap.clear();
                activePoiMarkers.clear();
                mainPoiButton.performClick();
                Sender.putMarkersOnMapAgain(markers, myMap);
                //setUpMap(false);
            }

        });

        POIScrollView = (ScrollView) getActivity().findViewById(R.id.POIScroll);

        mainPoiButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if((getActivity().findViewById(R.id.POIButtons)).getVisibility() == View.GONE){

                    if(!poiIsUpToDate) {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.execute();

                    }
                    POIScrollView.scrollTo(0,0);
                    getActivity().findViewById(R.id.POIButtons).setVisibility(View.VISIBLE);
                }
                else{
                    getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
                }
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
                if(activePoiMarkers.contains(markersBars))
                    activePoiMarkers.remove(markersBars);
                else    activePoiMarkers.add(markersBars);

                markersSelectionChanged();
            }});

        myButtonFoodCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersCoffee))
                    activePoiMarkers.remove(markersCoffee);
                else    activePoiMarkers.add(markersCoffee);

                markersSelectionChanged();
            }});

        myButtonFoodKfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersKfc))
                    activePoiMarkers.remove(markersKfc);
                else    activePoiMarkers.add(markersKfc);

                markersSelectionChanged();
            }});

        myButtonFoodMcDonald.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersMcdonalds))
                    activePoiMarkers.remove(markersMcdonalds);
                else    activePoiMarkers.add(markersMcdonalds);

                markersSelectionChanged();
            }});

        myButtonFoodRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersRestaurants))
                    activePoiMarkers.remove(markersRestaurants);
                else    activePoiMarkers.add(markersRestaurants);

                markersSelectionChanged();
            }});


        myButtonShopsMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersMarkets))
                    activePoiMarkers.remove(markersMarkets);
                else    activePoiMarkers.add(markersMarkets);

                markersSelectionChanged();
            }});

        myButtonShopsStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersShops))
                    activePoiMarkers.remove(markersShops);
                else    activePoiMarkers.add(markersShops);

                markersSelectionChanged();
            }});

        myButtonShopsShoppingMalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersShoppingMalls))
                    activePoiMarkers.remove(markersShoppingMalls);
                else    activePoiMarkers.add(markersShoppingMalls);

                markersSelectionChanged();
            }});



        myButtonLeisureClubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersNightClubs))
                    activePoiMarkers.remove(markersNightClubs);
                else    activePoiMarkers.add(markersNightClubs);

                markersSelectionChanged();
            }});
        myButtonLeisureParks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activePoiMarkers.contains(markersParks))
                    activePoiMarkers.remove(markersParks);
                else    activePoiMarkers.add(markersParks);

                markersSelectionChanged();
            }});
    }

    private void markersSelectionChanged()
    {
        myMap.clear();

        for(ArrayList<MarkerOptions> marks : activePoiMarkers)
        {
            for(int i=0; i<marks.size(); i++)
                myMap.addMarker(marks.get(i));
        }

        //tutaj dodajê jeszcze markery od Pana Sanczo (bo chyba powinny byæ wyœwietlane zawsze)
        Sender.putMarkersOnMapAgain(markers, myMap);
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

        setUpMap(true);
        setMapListener();
        setupPoiButtons();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (myMap == null) {
            myMap = fragment.getMap();
            //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        }
    }

    protected void preparePoiPoints() throws IOException {

        markersKfc = poiBase.getJsonWithSelectedData(0,0,new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()),"kfclogo");
        markersMcdonalds = poiBase.getJsonWithSelectedData(0,1,new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()),"mcdonaldslogo");
        markersRestaurants = poiBase.getJsonWithSelectedData(1, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "restaurant" );
        markersBars = poiBase.getJsonWithSelectedData(2, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "bar");
        markersCoffee = poiBase.getJsonWithSelectedData(3, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "coffee" );
        markersNightClubs = poiBase.getJsonWithSelectedData(4, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "nightclub" );
        markersParks = poiBase.getJsonWithSelectedData(5, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "park" );
        markersShoppingMalls= poiBase.getJsonWithSelectedData(6, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "shoppingmall" );
        markersShops= poiBase.getJsonWithSelectedData(7, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "shop" );
        markersMarkets = poiBase.getJsonWithSelectedData(8, new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()), "market" );
    }
    public void setMapListener() {
        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                lastClikOnMap = latLng;
                MarkerDialog markerDialog = new MarkerDialog();
                //markerDialog.show(getChildFragmentManager(), "Marker Dialog");

                myMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                layoutMarker.setVisibility(View.GONE);
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

              /*  Uri gmmIntentUri= Uri.parse("google.navigation:q="+latitude+","+longitude);
                Intent mapIntent=new Intent(Intent.ACTION_VIEW,gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);*/

                /*String url = getActivity().getDirectionUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();

                downloadTask.execute(url);*/
                return true;
            }
        });

        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ostatniMarker = marker;
                layoutMarker.setX((float) myMap.getProjection().toScreenLocation(marker.getPosition()).x - layoutMarker.getWidth() / 2 + 40);
                layoutMarker.setY((float) myMap.getProjection().toScreenLocation(marker.getPosition()).y - layoutMarker.getHeight() / 2 - 30);
                layoutMarker.setVisibility(View.VISIBLE);
                return true;
            }
        });

        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
                layoutMarker.setVisibility(View.GONE);
            }
        });
    }

    private void setUpMap(boolean hardSetup) {

        myMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.myMapFragment)).getMap();
        //Log.d(AppController.TAG,"my map to"+myMap);
        myMap.setMyLocationEnabled(true);
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        Sender.putMarkersOnMapAgain(markers, myMap);

        if (mCurrentLocation != null) {
            double latitude = mCurrentLocation.getLatitude();
            double longitude = mCurrentLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            if (hardSetup) {

                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                //myMap.animateCamera(CameraUpdateFactory.zoomTo(15),3000,null);
            }
        } else
            Log.e(AppController.TAG, "ostania znana lokacja jest nulem");


        myMap.setOnCameraChangeListener(getCameraChangeListener());

    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener()
    {
        return new GoogleMap.OnCameraChangeListener()
        {
            @Override
            public void onCameraChange(CameraPosition position)
            {
                if(ostatniMarker != null) {
                    layoutMarker.setX((float) myMap.getProjection().toScreenLocation(ostatniMarker.getPosition()).x - layoutMarker.getWidth() / 2 + 40);
                    layoutMarker.setY((float) myMap.getProjection().toScreenLocation(ostatniMarker.getPosition()).y - layoutMarker.getHeight()/2 - 30);
                }
                //getActivity().findViewById(R.id.POIButtons).setVisibility(View.GONE);
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
        mCurrentLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //  setUpMap(true);


        //setMapListener();
        /*if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }*/
    }

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
}
