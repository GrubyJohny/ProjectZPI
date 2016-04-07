package zpi.squad.app.grouploc.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zpi.squad.app.grouploc.AppController;
import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.adapters.FriendAdapter;
import zpi.squad.app.grouploc.domains.Friend;
import zpi.squad.app.grouploc.fragments.ChangePasswordFragment;
import zpi.squad.app.grouploc.fragments.ChangePhotoFragment;
import zpi.squad.app.grouploc.fragments.MapFragment;
import zpi.squad.app.grouploc.fragments.NotificationFragment;
import zpi.squad.app.grouploc.fragments.SettingsFragment;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, NavigationView.OnNavigationItemSelectedListener/*, android.support.v4.app.FragmentManager.OnBackStackChangedListener*/ {

    private SessionManager session;
    private LocationManager locationManager;
    public static final int PICK_FROM_CAMERA = 1;
    public static final int PICK_FROM_GALLERY = 2;
    public static final int CROP_IMAGE = 3;

    public static Context context;

    //Andoridowy obiekt przechowujący dane o położeniu(np latitude, longitude, kiedy zostało zarejestrowane)
    private Location mCurrentLocation;
    //obiekt będący parametrem, przy wysłaniu żądania o aktualizację lokacji
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    // private Date mLastUpdateDate = new Date();
    private ParseGeoPoint mParseLocation = new ParseGeoPoint();

    //Obiekt w ogólności reprezentujący googlowe api service,
    //jest często przekazywany jako argument, gdy coś o tego api chcemy
    private GoogleApiClient mGoogleApiClient;

    AppController globalVariable;
    DrawerLayout drawer;
    NavigationView navigationViewLeft;
    private ImageView navigationViewLeftProfilePicture;
    private TextView navigationViewLeftFullName;
    NavigationView navigationViewRight;
    Bitmap mainPhoto;

    MapFragment mapFragment;
    ChangePhotoFragment changePhotoFragment;
    ChangePasswordFragment changePasswordFragment;
    NotificationFragment notificationFragment;
    SettingsFragment settingsFragment;

    final static String mapTAG = "MAP";
    final static String photoTAG = "PHOTO";
    final static String passwordTAG = "PASSWORD";
    final static String notificationTAG = "NOTIFICATION";
    final static String settingsTAG = "SETTINGS";

    private ListView friendsListView;
    private EditText inputSearch;
    public static FriendAdapter adapter;
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
        StrictMode.setThreadPolicy(policy);
        createLocationRequest();
        buildGoogleApiClient();

        mGoogleApiClient.connect();
        session = SessionManager.getInstance(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        initializeFragments();

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
        navigationViewLeft.getMenu().getItem(0).setChecked(true);

        if (session.isLoggedByFacebook()) {
            navigationViewLeft.getMenu().removeItem(R.id.nav_password);
        }

        View headerLeft = navigationViewLeft.getHeaderView(0);

        navigationViewLeftProfilePicture = (ImageView) headerLeft.findViewById(R.id.profilePicture);
        mainPhoto = CommonMethods.getInstance().clipBitmap(CommonMethods.getInstance().decodeBase64ToBitmap(session.getUserPhoto()), navigationViewLeftProfilePicture);
        navigationViewLeftProfilePicture.setImageBitmap(mainPhoto);

        navigationViewLeftFullName = (TextView) headerLeft.findViewById(R.id.Fullname);
        navigationViewLeftFullName.setText(session.getUserName());

        navigationViewRight = (NavigationView) findViewById(R.id.nav_view_right);
        navigationViewRight.setNavigationItemSelectedListener(this);

        View headerRight = navigationViewRight.getHeaderView(0);

        addFriendButton = (FloatingActionButton) findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchingFriendsActivity.class);
                startActivity(intent);
            }
        });

        friendListSettings();

        inputSearch = (EditText) headerRight.findViewById(R.id.filterFriendsInput);
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

        Log.e("LOKALIZACJA: ", session.getCurrentLocation().latitude + ", " + session.getCurrentLocation().longitude);

        try {
            ParseInstallation.getCurrentInstallation().put("name", ParseUser.getCurrentUser().getEmail());
            ParseInstallation.getCurrentInstallation().saveInBackground();
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void friendListSettings() {
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
                                //MapFragment.getMap().addGroundOverlay(new GroundOverlayOptions().image(BitmapDescriptorFactory.fromBitmap(CommonMethods.getInstance().decodeBase64ToBitmap(item.getPhoto()))).position(new LatLng(item.getLocation().getLatitude(), item.getLocation().getLongitude()), 20).visible(true));
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
//                                        commonMethods.reloadSearchingFriendsData(SearchingFriendsActivity.adapter);
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
    }

    private void initializeFragments() {
        mapFragment = new MapFragment();
        changePhotoFragment = new ChangePhotoFragment();
        changePasswordFragment = new ChangePasswordFragment();
        notificationFragment = new NotificationFragment();
        settingsFragment = new SettingsFragment();
    }

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
                    user.put("photo", CommonMethods.getInstance().encodeBitmapTobase64(photo));
                    user.saveInBackground();
                    session.setUserPhoto(CommonMethods.getInstance().encodeBitmapTobase64(photo));

                    mainPhoto = CommonMethods.getInstance().clipBitmap(CommonMethods.getInstance().decodeBase64ToBitmap(session.getUserPhoto()), navigationViewLeftProfilePicture);
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
                    user.put("photo", CommonMethods.getInstance().encodeBitmapTobase64(photo));
                    user.saveInBackground();
                    session.setUserPhoto(CommonMethods.getInstance().encodeBitmapTobase64(photo));
                    mainPhoto = CommonMethods.getInstance().clipBitmap(CommonMethods.getInstance().decodeBase64ToBitmap(session.getUserPhoto()), navigationViewLeftProfilePicture);
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
        } else if (id == R.id.nav_notifications) {
            if (getSupportFragmentManager().findFragmentByTag(notificationTAG) == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, notificationFragment, notificationTAG).addToBackStack(mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            } else {
                if (!notificationFragment.isVisible())
                    getSupportFragmentManager().popBackStack();
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
        } else if (id == R.id.nav_settings) {
            if (getSupportFragmentManager().findFragmentByTag(settingsTAG) == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, settingsFragment, settingsTAG).addToBackStack(mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            } else {
                if (!settingsFragment.isVisible())
                    getSupportFragmentManager().popBackStack();
            }
        } else if (id == R.id.nav_logout) {
            logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (id != R.id.nav_about)
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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

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
        } else if (getSupportFragmentManager().getBackStackEntryCount() >= 2) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container, mapFragment, mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            navigationViewLeft.getMenu().getItem(0).setChecked(true);
        } else {
            super.onBackPressed();
            navigationViewLeft.getMenu().getItem(0).setChecked(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            session.requestLocationUpdate = true;
            startLocationUpdates();
        }

        CommonMethods.getInstance().reloadFriendsData(adapter);
    }

    @Override
    protected void onPause() {
        //session.requestLocationUpdate = false;
        super.onPause();
        // stopLocationUpdates();
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

