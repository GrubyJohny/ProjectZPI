package zpi.squad.app.grouploc;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import zpi.squad.app.grouploc.activity.LoginActivity;
import zpi.squad.app.grouploc.activity.SearchingFriendsActivity;
import zpi.squad.app.grouploc.adapter.FriendAdapter;
import zpi.squad.app.grouploc.domain.Friend;
import zpi.squad.app.grouploc.domain.ListViewItem;
import zpi.squad.app.grouploc.domain.Notification;
import zpi.squad.app.grouploc.fragment.ChangePasswordFragment;
import zpi.squad.app.grouploc.fragment.ChangePhotoFragment;
import zpi.squad.app.grouploc.fragment.MapFragment;
import zpi.squad.app.grouploc.utils.PoiJSONParser;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, NavigationView.OnNavigationItemSelectedListener/*, android.support.v4.app.FragmentManager.OnBackStackChangedListener*/ {

    private SessionManager session;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    Vibrator v;
    protected boolean inhibit_spinner = true;
    private Spinner spinner1, spinner2, spinner3;
    private View layoutMarker;
    private GoogleMap myMap;
    private LocationManager locationManager;
    public static final int PICK_FROM_CAMERA = 1;
    public static final int PICK_FROM_GALLERY = 2;
    public static final int CROP_IMAGE = 3;
    private ImageButton firstMarkerButton;
    private ImageButton secondMarkerButton;
    private ImageButton thirdMarkerButton;
    private ImageButton fourthMarkerButton;
    // private Button closeMarkerButton;
    private Marker ostatniMarker;

    private ScrollView POIScrollView;
    PoiJSONParser poiBase = new PoiJSONParser();
    public static Context context;
    ArrayList<Notification> readNotifications = new ArrayList<Notification>();


    //Andoridowy obiekt przechowujący dane o położeniu(np latitude, longitude, kiedy zostało zarejestrowane)
    private Location mCurrentLocation;
    //obiekt będący parametrem, przy wysłaniu żądania o aktualizację lokacji
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    private Date mLastUpdateDate = new Date();
    private ParseGeoPoint mParseLocation = new ParseGeoPoint();

    //Obiekt w ogólności reprezentujący googlowe api service,
    //jest często przekazywany jako argument, gdy coś o tego api chcemy
    private GoogleApiClient mGoogleApiClient;

    //OstatniKliknietyNaMapi
    private LatLng lastClikOnMap;

    Bitmap profilePictureRaw;

    AppController globalVariable;

    private ToolTipView myToolTipView;
    private ToolTipView friendEmailToolTipView;
    ToolTipRelativeLayout toolTipRelativeLayout;

    private boolean hints = false;
    int hintsL;
    DrawerLayout drawer;
    NavigationView navigationViewLeft;
    private ImageView navigationViewLeftProfilePicture;
    private TextView navigationViewLeftFullName;
    NavigationView navigationViewRight;
    Bitmap mainPhoto;

    MapFragment mapFragment;
    ChangePhotoFragment changePhotoFragment;
    ChangePasswordFragment changePasswordFragment;

    final static String mapTAG = "MAP";
    final static String photoTAG = "PHOTO";
    final static String passwordTAG = "PASSWORD";

    private static List<ListViewItem> mItems;
    private ListView friendsListView;
    private EditText inputSearch;
    private FriendAdapter adapter;
    private FloatingActionButton addFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        context = getApplicationContext();
        globalVariable = (AppController) getApplicationContext();
        if (globalVariable.getDialog() != null && globalVariable.getDialog().isShowing()) {
            globalVariable.getDialog().dismiss();
        }
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        StrictMode.setThreadPolicy(policy);
        createLocationRequest();
        buildGoogleApiClient();

        mGoogleApiClient.connect();
        session = SessionManager.getInstance(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mapFragment = new MapFragment();
        changePhotoFragment = new ChangePhotoFragment();
        changePasswordFragment = new ChangePasswordFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, mapFragment, mapTAG).commit();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationViewLeft = (NavigationView) findViewById(R.id.nav_view_left);
        navigationViewLeft.setNavigationItemSelectedListener(this);

        if (session.isLoggedByFacebook()) {
            navigationViewLeft.getMenu().removeItem(R.id.nav_password);
        }

        navigationViewLeftProfilePicture = (ImageView) findViewById(R.id.profilePicture);
        mainPhoto = clipBitmap(session.decodeBase64ToBitmap(session.getUserPhoto()), navigationViewLeftProfilePicture);
        navigationViewLeftProfilePicture.setImageBitmap(mainPhoto);

        navigationViewLeftFullName = (TextView) findViewById(R.id.Fullname);
        navigationViewLeftFullName.setText(session.getUserName());

        navigationViewRight = (NavigationView) findViewById(R.id.nav_view_right);
        navigationViewRight.setNavigationItemSelectedListener(this);

        addFriendButton = (FloatingActionButton) findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchingFriendsActivity.class);
                startActivity(intent);
            }
        });

        ArrayList<Friend> friendsList = new ArrayList<>();
        friendsList.addAll(session.getFriendsList());

        Collections.sort(friendsList, new Comparator<Friend>() {
            @Override
            public int compare(Friend friend2, Friend friend1) {
                return friend2.getName().compareToIgnoreCase(friend1.getName());
            }
        });

        adapter = new FriendAdapter(this, friendsList);

        friendsListView = (ListView) findViewById(R.id.friendsListView);
        friendsListView.setAdapter(adapter);
        friendsListView.setTextFilterEnabled(true);
        supportInvalidateOptionsMenu();

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Friend item;

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                item = (Friend) friendsListView.getItemAtPosition(position);

                CharSequence options[] = new CharSequence[]{"Show on map", "Navigate to", "Delete"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(item.getName());
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "You choose option: " + which, Toast.LENGTH_SHORT).show();
                        drawer.closeDrawer(navigationViewRight);

                        switch (which) {
                            case 0:
                                if (!mapFragment.isVisible()) {
                                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container, mapFragment, mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                                }
                                MapFragment.moveMapCamera(new LatLng(item.getLocation().getLatitude(), item.getLocation().getLongitude()));
                                MapFragment.getMap().addGroundOverlay(new GroundOverlayOptions().image(BitmapDescriptorFactory.fromBitmap(session.decodeBase64ToBitmap(item.getPhoto()))).position(new LatLng(item.getLocation().getLatitude(), item.getLocation().getLongitude()), 20).visible(true));
                                break;
                            case 1:
                                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", item.getLocation().getLatitude(), item.getLocation().getLongitude(), item.getName());
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    try {
                                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                        startActivity(unrestrictedIntent);
                                    } catch (ActivityNotFoundException innerEx) {
                                        Toast.makeText(getApplicationContext(), "Please install a maps application", Toast.LENGTH_LONG).show();
                                    }
                                }
                                break;
                            case 2:

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle("Delete friend");
                                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        DeleteFriendship deleteFriend = new DeleteFriendship();
                                        deleteFriend.execute(item);
                                        dialog.dismiss();
                                        adapter.remove(item);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(getApplicationContext(), "Friend deleted", Toast.LENGTH_LONG).show();


                                    }
                                });
                                builder.setCancelable(true);
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.setMessage("Do you want to remove " + item.getName() + " from your friends ?");
                                builder.show();

                                break;

                            default:
                                break;
                        }

                    }
                });
                builder.show();
                Toast.makeText(context, "You selected: " + item.getName(), Toast.LENGTH_LONG).show();
            }
        });

        inputSearch = (EditText) findViewById(R.id.filterFriendsInput);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }


//        notifications();
//        messages();
//        noticeAndMessageButtons();

//        addListenerOnSpinner();
//        addListenerOnSpinner2();
//        addListenerOnSpinner3();

        layoutMarker = (View) findViewById(R.id.markerLayout);

        firstMarkerButton = (ImageButton) findViewById(R.id.firstButton);
        secondMarkerButton = (ImageButton) findViewById(R.id.secondButton);
        thirdMarkerButton = (ImageButton) findViewById(R.id.thirdButton);
        fourthMarkerButton = (ImageButton) findViewById(R.id.fourthButton);

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setCancelable(false);

        POIScrollView = (ScrollView) findViewById(R.id.POIScroll);

        //  toolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipRelativeLayout);
//        toolTipRelativeLayout.bringToFront();

        hintsL = session.getHintsLeft();

        Log.e("LOKALIZACJA: ", session.getCurrentLocation().latitude + ", " + session.getCurrentLocation().longitude);

        ParseInstallation.getCurrentInstallation().deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                ParseInstallation.getCurrentInstallation().put("name", ParseUser.getCurrentUser().getEmail());
                ParseInstallation.getCurrentInstallation().saveInBackground();
            }
        });

    }

    /*private void addMyToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("Click here for settings")
                .withShadow()
                .withColor(Color.RED)
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);
        myToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.circleButton));
        myToolTipView.setOnToolTipViewClickedListener(MainActivity.this);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Bundle extras2;
            if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
                extras2 = data.getExtras();
                if (extras2 != null) {
                    Bitmap photo = extras2.getParcelable("data");

                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("photo", session.encodeBitmapTobase64(photo));
                    user.saveInBackground();
                    session.setUserPhoto(session.encodeBitmapTobase64(photo));

                    mainPhoto = clipBitmap(session.decodeBase64ToBitmap(session.getUserPhoto()), navigationViewLeftProfilePicture);
                    navigationViewLeftProfilePicture.setImageBitmap(mainPhoto);

                    Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
                extras2 = data.getExtras();
                if (extras2 != null) {
                    Uri uri = data.getData();
                    Intent cropIntent = new Intent("com.android.camera.action.CROP");
                    cropIntent.setDataAndType(uri, "image/*");
                    cropIntent.putExtra("crop", "true");
                    cropIntent.putExtra("aspectX", 1);
                    cropIntent.putExtra("aspectY", 1);
                    cropIntent.putExtra("outputX", 500);
                    cropIntent.putExtra("outputY", 500);
                    cropIntent.putExtra("return-data", true);

                    startActivityForResult(cropIntent, CROP_IMAGE);
                }
            } else if (requestCode == CROP_IMAGE && resultCode == RESULT_OK) {
                extras2 = data.getExtras();
                if (extras2 != null) {
                    Bitmap photo = extras2.getParcelable("data");

                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("photo", session.encodeBitmapTobase64(photo));
                    user.saveInBackground();
                    session.setUserPhoto(session.encodeBitmapTobase64(photo));
                    mainPhoto = clipBitmap(session.decodeBase64ToBitmap(session.getUserPhoto()), navigationViewLeftProfilePicture);
                    navigationViewLeftProfilePicture.setImageBitmap(mainPhoto);

                    Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "You haven't picked Image...", Toast.LENGTH_LONG).show();
                Log.e("RESULT CODE: ", "" + resultCode);
                Log.e("REQUEST CODE: ", "" + requestCode);
            }


        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

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

        // TO JEST PRZYKLAD JAK POMALOWAC IKONKE W MENU, PRZYDA SIE NA KIEDYS
        /*Drawable d =  menu.getItem(0).getIcon();
        d.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);*/
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.friendsButton) {
            if (drawer.isDrawerOpen(navigationViewLeft)) {
                drawer.closeDrawer(navigationViewLeft);
            }
            drawer.openDrawer(navigationViewRight);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("About us");
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setMessage("This application was created by awesome people");
            builder.show();
        } else if (id == R.id.nav_map) {
            if (!mapFragment.isVisible()) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, mapFragment, mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            }
        } else if (id == R.id.nav_password) {
            if (getSupportFragmentManager().findFragmentByTag(passwordTAG) == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, changePasswordFragment, passwordTAG).addToBackStack(mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            } else {
                if (!changePasswordFragment.isVisible())
                    getSupportFragmentManager().popBackStack();
            }
        } else if (id == R.id.nav_photo) {
            if (getSupportFragmentManager().findFragmentByTag(photoTAG) == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, changePhotoFragment, photoTAG).addToBackStack(mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            } else {
                if (!changePhotoFragment.isVisible())
                    getSupportFragmentManager().popBackStack();
            }
        } else if (id == R.id.nav_logout) {
            logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        item.setChecked(true);
        return true;
    }

    public void logOut() {
        //stopLocationUpdates();

        session.logOut();
        session.requestLocationUpdate = false;

        Intent closeIntent = new Intent(this, LoginActivity.class);
        startActivity(closeIntent);
        finish();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Date lastUpdate = new Date();
        mLastUpdateTime = DateFormat.getTimeInstance().format(lastUpdate);
        //Log.e(AppController.TAG, "Location has changed");
        double latitude = (double) location.getLatitude();
        double longitude = (double) location.getLongitude();

        //
        if (session.isLoggedIn() && session.requestLocationUpdate) {
            try {
                session.setUserCurrentLocation(latitude, longitude);

                mParseLocation.setLatitude(latitude);
                mParseLocation.setLongitude(longitude);
                ParseUser.getCurrentUser().fetchIfNeeded().put("location", mParseLocation);
                ParseUser.getCurrentUser().fetchIfNeeded().put("locationUpdateTime", lastUpdate);
                ParseUser.getCurrentUser().saveInBackground();

            } catch (ParseException e) {
                e.getLocalizedMessage();
                e.printStackTrace();
            }
        }

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("****LOKALIZACJA: ", "Podłączony do api service");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

       /* Log.e("onConnected: ", ""+session.requestLocationUpdate );
        if (session.requestLocationUpdate) {
        startLocationUpdates();
        }*/

        startLocationUpdates();
        if (mCurrentLocation != null) {
            MapFragment.moveMapCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        startLocationUpdates();
    }


    public Bitmap clipBitmap(Bitmap bitmap, ImageView x) {
        if (bitmap == null)
            return null;
        final int width = x.getLayoutParams().width;
        final int height = x.getLayoutParams().height;
        bitmap = bitmap.createScaledBitmap(bitmap, width, height, true);

        final int bWidth = bitmap.getWidth();
        final int bHeight = bitmap.getHeight();

        //System.out.println("WYMIARY " + width + "," + height + " ; bitmapa " + bWidth + ", " + bHeight);

        DisplayMetrics metrics = new DisplayMetrics();

        //zakomentowałem to, bo w Sender.java nie mogłem użyć getWindowManager()
        // wygląda jakby działało bez tego w porządku /Johny
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Bitmap outputBitmap = Bitmap.createBitmap(metrics, width, height, Bitmap.Config.ARGB_8888);

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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else if (getSupportFragmentManager().findFragmentByTag(mapTAG).isVisible()) {
            new AlertDialog.Builder(this).
                    setTitle("Really Exit")
                    .setMessage("Are You sure you want to exit ?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            session.requestLocationUpdate = false;
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            session.requestLocationUpdate = true;
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        //session.requestLocationUpdate = false;
        super.onPause();
        // stopLocationUpdates();
    }

    public void onFriendshipRequest(final Notification not) {
        new AlertDialog.Builder(this).
                setTitle("FriendshipRequest")
                .setMessage("Do you want to add " + not.getSenderName() + " to your friends ?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (not.getType().equals("friendshipRequest")) {
                            // sendFriendshipAcceptance(session.getUserId(), not.getSenderId(), not.getSenderName(), not.getSenderEmail());


                        }
                    }
                }).create().show();
    }

    public void onGroupRequest(final Notification not) {
        new AlertDialog.Builder(this).
                setTitle("Group Request")
                .setMessage("Do you want to add " + not.getSenderName() + " to your group ?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (not.getType().equals("groupRequest")) {
                            sendGroupRequestAcceptance(not.getSenderId(), not.getGroupId());


                        }
                    }
                }).create().show();
    }

    private void showDialog() {
        if (!pDialog.isShowing()) {
            pDialog.create();
            pDialog.show();
        }
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

   /* private void sendFriendshipAcceptance(final String senderId, final String myreceiverid, final String receiverName, final String receiverEmail) {

        String tag_string_req = "req_friendshipRequest";
        pDialog.setMessage("Sending friendship acceptance");
        showDialog();
        final String TAG = "addFriend";

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Friendship acceptance Response: " + response.toString());
                hideDialog();

                try {

                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {

                        Toast.makeText(getApplicationContext(), "Acceptance has been sent successfully", Toast.LENGTH_LONG).show();
                        db.addFriend(myreceiverid, receiverName, receiverEmail);
                        FriendsFragment.addFriend(new Friend(myreceiverid, receiverName, receiverEmail, null));


                    } else {

                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Connection problem. Try again.", Toast.LENGTH_LONG).show();
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
                params.put("tag", "addFriend");
                params.put("senderId", senderId);
                params.put("receiverId", myreceiverid);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }*/

    private void sendGroupRequestAcceptance(final String senderId, final String groupId) {

        String tag_string_req = "req_friendshipRequest";
        pDialog.setMessage("Sending group request acceptance");
        showDialog();
        final String TAG = "addFriendToGroup";

        StringRequest strReq = new StringRequest(Request.Method.POST, "" /*AppConfig.URL_REGISTER*/, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Friend to group acceptance Response: " + response.toString());
                hideDialog();

                try {

                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        Toast.makeText(getApplicationContext(), "Acceptance has been sent successfully", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Connection problem. Try again.", Toast.LENGTH_LONG).show();
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
                params.put("tag", "addFriendToGroupAcceptance");
                params.put("uid", senderId);
                params.put("aid", session.getUserId());
                params.put("gid", groupId);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void deleteFriendship(ParseUser friend) {
        ArrayList<Friend> result = new ArrayList<>();

        ParseQuery checkIfFriends1 = new ParseQuery("Friendship");
        checkIfFriends1.whereEqualTo("friend1", ParseUser.getCurrentUser());
        checkIfFriends1.whereEqualTo("friend2", friend);

        ParseQuery checkIfFriends2 = new ParseQuery("Friendship");
        checkIfFriends2.whereEqualTo("friend2", ParseUser.getCurrentUser());
        checkIfFriends2.whereEqualTo("friend1", friend);

        Object[] friendshipsList = null, friendshipsList2 = null;
        ParseObject tempFriendship = null;
        List<ParseObject> friendshipsToDelete = new ArrayList<>();


        try {
            friendshipsList = checkIfFriends1.find().toArray().clone();

            if (friendshipsList.length > 0) {
                for (int i = 0; i < friendshipsList.length; i++) {
                    //to jest typu Friendship
                    tempFriendship = ((ParseObject) friendshipsList[i]);

                    if (tempFriendship.get("accepted").toString().equals("true"))
                        friendshipsToDelete.add(tempFriendship);
                }
            }


            friendshipsList2 = checkIfFriends2.find().toArray().clone();

            if (friendshipsList2.length > 0) {
                for (int i = 0; i < friendshipsList2.length; i++) {
                    //to jest typu Friendship
                    tempFriendship = ((ParseObject) friendshipsList2[i]);

                    if (tempFriendship.get("accepted").toString().equals("true"))
                        friendshipsToDelete.add(tempFriendship);
                }
            }

            if (friendshipsToDelete.size() == 1) {
                ParseInstallation.deleteAll(friendshipsToDelete);
            } else
                Log.i("deleteFriendship: ", "Problem with deleting friendship. Number of friendships different than 1.");

        } catch (ParseException e) {
            Log.e("Parse: ", e.getLocalizedMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("Exception: ", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        View empty = findViewById(R.id.emptyTextInDrawer);
        ListView list = (ListView) findViewById(R.id.friendsListView);
        list.setEmptyView(empty);
    }

    private class DeleteFriendship extends AsyncTask<Friend, Void, Void> {
        @Override
        protected Void doInBackground(Friend... param) {
            deleteFriendship(param[0].getParseUser());
            session.refreshFriendsList();

            return null;
        }
    }
}

