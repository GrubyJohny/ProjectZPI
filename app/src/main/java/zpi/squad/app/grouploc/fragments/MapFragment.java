package zpi.squad.app.grouploc.fragments;

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
import android.widget.ListView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

import zpi.squad.app.grouploc.AppController;
import zpi.squad.app.grouploc.MarkerDialog;
import zpi.squad.app.grouploc.POISpecies;
import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.activities.MainActivity;
import zpi.squad.app.grouploc.domains.CustomMarker;
import zpi.squad.app.grouploc.domains.Friend;
import zpi.squad.app.grouploc.domains.MyMarker;
import zpi.squad.app.grouploc.utils.DirectionsJSONParser;
import zpi.squad.app.grouploc.utils.PoiJSONParser;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnStreetViewPanoramaReadyCallback, GoogleApiClient.ConnectionCallbacks, MarkerDialog.NoticeDialogListener {

    public static Context context;
    //private GoogleMapOptions mapOptions = new GoogleMapOptions().liteMode(true);
    public static GoogleApiClient mGoogleApiClient;
    //OstatniKliknietyNaMapi
    public static LatLng lastClikOnMap;
    private static View view;
    private static GoogleMap map;
    public HashMap<Marker, MyMarker> allMarkers = new HashMap<>();
    public HashMap<String, MarkerOptions> actualShowingOnMapMarkers = new HashMap<>();
    PoiJSONParser poiBase = new PoiJSONParser();
    Resources res;
    AppController globalVariable;
    int width;
    int height;
    ArrayList<MyMarker> markers;
    ArrayList<MyMarker> friendsMarkers;
    ArrayList<Friend> friendsToShare = new ArrayList<Friend>();
    private SupportMapFragment fragment;
    private ArrayList<ArrayList<MarkerOptions>> activePoiMarkers = new ArrayList<>();
    private HashMap<POISpecies, HashMap<String, Marker>> active = new HashMap<POISpecies, HashMap<String, Marker>>();
    private HashMap<String, Polyline> visibleRouts = new HashMap<String, Polyline>();
    private boolean poiIsUpToDate = false;
    private Location mCurrentLocation;
    private Marker ostatniMarker;
    private View tabs;
    private List<CustomMarker> markers_old;
    private HashMap<String, Marker> googleMarkers;
    private boolean mRequestingLocationUpdates;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private SessionManager session;

    public static GoogleMap getMap() {
        return map;
    }

    public static void moveMapCamera(LatLng position) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(17)
                .bearing(0)
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity().getApplicationContext();
        globalVariable = (AppController) getActivity().getApplicationContext();
        session = SessionManager.getInstance(context);

        //globalVariable.setMarkers(markers_old);

        //mRequestingLocationUpdates = true;
        //createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        width = context.getResources().getDisplayMetrics().widthPixels;
        height = context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        fragment.onCreate(savedInstanceState);
        fragment.getMapAsync(this);

        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
//        inclizaidListenerForMarkerMenu();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        AppController.myMap = map;
        map.setBuildingsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);

        final LatLng location = (mCurrentLocation == null ? session.getCurrentLocation() : new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        moveMapCamera(location);

        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                getMap().animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), new GoogleMap.CancelableCallback() {

                    @Override
                    public void onFinish() {
                        if (marker.getSnippet().equals("own")) {
                            new BottomSheet.Builder(getActivity()).grid().title(marker.getTitle()).sheet(R.menu.menu_bottom_own).listener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case R.id.polyline:
                                            doPolyline(marker);
                                            break;
                                        case R.id.navigate:
                                            doNavigation(marker);
                                            break;
                                        case R.id.share:
                                            dialogShare(marker);
                                            break;
                                        case R.id.delete:
                                            //TODO
                                            break;
                                    }
                                }
                            }).show();

                        } else if (marker.getSnippet().equals("friends")) {

                            new BottomSheet.Builder(getActivity()).grid().title(marker.getTitle()).sheet(R.menu.menu_bottom_friend).listener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case R.id.polyline:
                                            doPolyline(marker);
                                            break;
                                        case R.id.navigate:
                                            doNavigation(marker);
                                            break;
                                    }
                                }
                            }).show();
                        } else if (marker.getSnippet().startsWith("from ")) { //udostępniony przez znajomego

                            new BottomSheet.Builder(getActivity()).grid().title(marker.getTitle()).sheet(R.menu.menu_bottom_friend).listener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case R.id.polyline:
                                            doPolyline(marker);
                                            break;
                                        case R.id.navigate:
                                            doNavigation(marker);
                                            break;
                                    }
                                }
                            }).show();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });

                return true;
            }

            private void dialogShare(final Marker marker) {

                final ArrayList<Friend> friends = SessionManager.getInstance().getFriendsList();

                final CharSequence[] items = new CharSequence[friends.size()];

                for (int i = 0; i < friends.size(); i++)
                    items[i] = friends.get(i).getName();

                final ArrayList seletedItems = new ArrayList();

                AlertDialog shareDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Choose friends to share")
                        .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                if (isChecked) {
                                    seletedItems.add(indexSelected);
                                } else if (seletedItems.contains(indexSelected)) {
                                    seletedItems.remove(Integer.valueOf(indexSelected));
                                }
                            }
                        }).setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                for (int i = 0; i < seletedItems.size(); i++)
                                    Log.e("wybrany: ", seletedItems.get(i).toString());

                                friendsToShare = new ArrayList<Friend>();

                                for (int i = 0; i < friends.size(); i++) {
                                    for (int j = 0; j < seletedItems.size(); j++) {
                                        if (String.valueOf(i).equals(seletedItems.get(j).toString()))
                                            friendsToShare.add(friends.get(i));
                                    }
                                }

                                ShareMarkerForSelectedFriends shareMarkerForSelectedFriends = new ShareMarkerForSelectedFriends();
                                shareMarkerForSelectedFriends.execute(marker);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setNeutralButton("Share for all", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ListView list = ((AlertDialog) dialog).getListView();
                                for (int i = 0; i < list.getCount(); i++) {
                                    list.setItemChecked(i, true);
                                }

                                ShareMarkerForAllFriends shareAll = new ShareMarkerForAllFriends();
                                shareAll.execute(marker);

                            }
                        }).create();
                shareDialog.show();
            }

            private void doNavigation(Marker marker) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + marker.getPosition().latitude + "," + marker.getPosition().longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }

            private void doPolyline(Marker marker) {
                if (visibleRouts.size() > 0) {
                    deleteRoute(visibleRouts.keySet().iterator().next());
                    visibleRouts.clear();
                }
                LatLng origin = location;
                LatLng dest = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                String url = getDirectionUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask(marker.getId());
                downloadTask.execute(url);

            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                lastClikOnMap = latLng;
                MarkerDialog markerDialog = new MarkerDialog(latLng);
                markerDialog.setTargetFragment(MapFragment.this, 0);
                markerDialog.show(getFragmentManager(), "Marker Dialog");
            }
        });

        new PrepareMarkers().execute();
    }

    private void prepareMarkers() {

        HashMap<MarkerOptions, MyMarker> ownMarkers = session.getOwnMarkers();
        for (MarkerOptions m : ownMarkers.keySet())
            actualShowingOnMapMarkers.put(map.addMarker(m).getId(), m);

        HashMap<MarkerOptions, MyMarker> sharedMarkers = session.getSharedMarkers();
        for (MarkerOptions m : sharedMarkers.keySet())
            actualShowingOnMapMarkers.put(map.addMarker(m).getId(), m);

        HashMap<MarkerOptions, Friend> friendsMarkers = session.getFriendsMarkers();
        for (MarkerOptions m : friendsMarkers.keySet())
            actualShowingOnMapMarkers.put(map.addMarker(m).getId(), m);
/*
                            .icon(BitmapDescriptorFactory.fromBitmap(CommonMethods.getInstance().clipBitmap(friendsMarkers.get(i).getIcon(), 150, 150)))
        */
        // .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(friendsMarkers.get(i).getIcon())))
    }

    public void deleteRoute(String tag) {
        Polyline route = visibleRouts.remove(tag);
        route.remove();
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setZoomGesturesEnabled(true);
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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle("Map");
        //setUpMap(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setUpMap(boolean hardSetup) {

        globalVariable.setMyMap(((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap());
        //Log.d(AppController.TAG,"my map to"+myMap);
        globalVariable.getMyMap().setMyLocationEnabled(true);

        //globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
        globalVariable.getMyMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);

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
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        /*setUpMap(true);
        setMapListener();
        setupPoiButtons();

        if (mRequestingLocationUpdates) {
             startLocationUpdates();
        }*/
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

    /*protected void startLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }*/

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private String downloadUrl(String strUrl) throws IOException {

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
            Log.e("Exception url", e.toString());
        } finally {
            isStream.close();
            urlConnection.disconnect();
        }
        return data;
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

    @Override
    public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog) {
/*
        MarkerDialog md = (MarkerDialog) dialog;
        String name = md.getName();
        Log.d("MyMarker Dialog", name);
        Log.d("MyMarker Dialog", "myMap " + globalVariable.getMyMap());
        Log.d("MyMarker Dialog", "my LatLong " + globalVariable.getLastClikOnMap());



        CustomMarker nowyMarker = new CustomMarker(null, session.getUserId(), globalVariable.getLastClikOnMap().latitude, globalVariable.getLastClikOnMap().longitude, name);
        globalVariable.addToMarkers(nowyMarker);
        long id = db.addMarker(nowyMarker);
        nowyMarker.setMarkerIdSQLite(Long.toString(id));
        String markerIdExtrenal = "NULL";
        String markerIdInteler = Long.toString(id);
        MyMarker marker=globalVariable.getMyMap().addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarkerhi)).position(globalVariable.getLastClikOnMap()).draggable(true).title(name).snippet(markerIdExtrenal + "," + markerIdInteler));
        googleMarkers.put(markerIdInteler,marker);
        Log.d("ADD_MARKER", markerIdExtrenal + "," + markerIdInteler);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(md.getInput().getWindowToken(), 0);*/
    }

    private void sendMarkerSharedNotification(ArrayList<Friend> receivers, Marker marker) {

        for (int i = 0; i < receivers.size(); i++) {

            ParseObject notific = new ParseObject("Notification");
            notific.put("senderEmail", ParseUser.getCurrentUser().getEmail());
            notific.put("senderName", ParseUser.getCurrentUser().get("name"));
            notific.put("receiverEmail", receivers.get(i).getEmail());
            notific.put("kindOfNotification", 103);
            notific.put("markedAsRead", false);
            notific.put("extra", marker.getId());

            try {
                notific.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }


    }

    class AsyncTaskRunner extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                //preparePoiPoints();
                //Log.d("POI JOHNY", "poi gotowe");
                //poiIsUpToDate = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            String resp = "done";
            return resp;
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        String markID;

        public DownloadTask(String markID) {
            super();
            this.markID = markID;
        }

        //Pobieranie danych w innym w�tku ni� ten opowiedzialny za wy�wietlanie grafiki
        @Override
        protected String doInBackground(String... url) {

            //String do przechowywanie odberanych danych
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.e("Background Task", e.toString());
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

        public ParseTask(String markID) {
            this.markID = markID;
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
            Polyline polyline = map.addPolyline(lineOptions);
            visibleRouts.put(markID, polyline);
        }
    }

    private class PrepareMarkers extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //SessionManager.getInstance().refreshOwnMarkers();
            //sprawdzę jeszcze czy refreshowanie powinno być tutaj
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            prepareMarkers();
        }
    }

    private class ShareMarkerForAllFriends extends AsyncTask<Marker, Void, Void> {
        @Override
        protected Void doInBackground(Marker... params) {

            try {
                ParseObject originMarker = ParseQuery.getQuery("Marker").whereEqualTo("objectId", SessionManager.getInstance().getOwnMarkers().get(actualShowingOnMapMarkers.get(params[0].getId())).getObjectId()).getFirst().fetchIfNeeded();
                List<Friend> fri = SessionManager.getInstance().getFriendsList();
                for (int i = 0; i < fri.size(); i++) {
                    ParseObject marker = new ParseObject("SharedMarker");
                    marker.put("marker", originMarker);
                    marker.put("sharedUser", fri.get(i).getParseUser());

                    marker.saveInBackground();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                SessionManager.getInstance().refreshOwnMarkers();
                //no i trzeba będzie oczywiście odświeżyć markery na mapie bo się sypie jak chce się udostępnić świeżo zapisany marker...
            }

            sendMarkerSharedNotification(SessionManager.getInstance().getFriendsList(), params[0]);

            return null;
        }
    }

    private class ShareMarkerForSelectedFriends extends AsyncTask<Marker, Void, Void> {
        @Override
        protected Void doInBackground(Marker... params) {

            if (friendsToShare.size() > 0) {
                try {
                    ParseObject originMarker = ParseQuery.getQuery("Marker").whereEqualTo("objectId", session.getOwnMarkers().get(actualShowingOnMapMarkers.get(params[0].getId())).getObjectId()).getFirst().fetchIfNeeded();
                    List<Friend> fri = friendsToShare;
                    for (int i = 0; i < fri.size(); i++) {
                        ParseObject marker = new ParseObject("SharedMarker");
                        marker.put("marker", originMarker);
                        marker.put("sharedUser", fri.get(i).getParseUser());

                        marker.saveInBackground();
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            sendMarkerSharedNotification(friendsToShare, params[0]);


            return null;
        }

    }

    private class RefreshFriendsPosition extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            SessionManager.getInstance().getRefreshedFriendsMarkers();
            return null;
        }
    }

}
