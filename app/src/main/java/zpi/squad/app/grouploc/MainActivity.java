package zpi.squad.app.grouploc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.*;
import android.app.AlertDialog;
import android.support.v4.app.*;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.*;
import android.content.ActivityNotFoundException;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ToolTipView.OnToolTipViewClickedListener, NavigationView.OnNavigationItemSelectedListener {

    private SessionManager session;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    Vibrator v;
    private View tabLayout;
    protected boolean inhibit_spinner = true;
    private Spinner spinner1, spinner2, spinner3;
    public static ImageButton circleButton;
    private ImageButton noticeButton;
    private ImageButton messageButton;
    private Button friendsButton;
    private Button groupButton;
    private Button BackToMapButton;
    private Button addFriendButton;
    private View layoutSettings;
    private View layoutGroup;
    private View layoutMarker;
    private GoogleMap myMap;
    EditText searchingGroupText;
    private LocationManager locationManager;
    private Button mainPoiButton;
    private Button clearPoiButton;
    private Button confirm;
    private Button cancel;
    private Button changeImgFromGallery;
    private Button changeImgFromCamera;
    private Button changeImgFromFacebook;
    private Button changeImgFromAdjust;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private static final int CROP_IMAGE = 3;
    private ImageButton firstMarkerButton;
    private ImageButton secondMarkerButton;
    private ImageButton thirdMarkerButton;
    private ImageButton fourthMarkerButton;
    //private Button closeMarkerButton;
    private Marker ostatniMarker;
    private FragmentTabHost tabhost;
    private Bitmap bitmap_round;

    private ScrollView POIScrollView;
    PoiJSONParser poiBase = new PoiJSONParser();
    public static Context context;
    ArrayList<Notification> readNotifications = new ArrayList<Notification>();


    //Andoridowy obiekt przechowujący dane o położeniu(np latitude, longitude, kiedy zostało zarejestrowane)
    private Location mCurrentLocation;
    //obiekt będący parametrem, przy wysłaniu żądania o aktualizację lokacji
    private LocationRequest mLocationRequest;

    //Obiekt w ogólności reprezentujący googlowe api service,
    //jest często przekazywany jako argument, gdy coś o tego api chcemy
    private GoogleApiClient mGoogleApiClient;

    //Flaga mowiąca o tym czy chcemy monitorować lokalizację
    private boolean mRequestingLocationUpdates;

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

    MapFragment mapFragment;
    SettingsFragment settingsFragment;

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
        //tabLayout = (View) findViewById(R.id.tabLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapFragment = new MapFragment();
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, mapFragment).commit();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationViewLeft = (NavigationView) findViewById(R.id.nav_view_left);
        navigationViewLeft.setNavigationItemSelectedListener(this);

        navigationViewLeftProfilePicture = (ImageView) findViewById(R.id.profilePicture);
        Bitmap mainPhoto = clipBitmap(session.decodeBase64ToBitmap(session.getUserPhoto()), navigationViewLeftProfilePicture);
        navigationViewLeftProfilePicture.setImageBitmap(mainPhoto);

        navigationViewLeftFullName = (TextView) findViewById(R.id.Fullname);
        navigationViewLeftFullName.setText(session.getUserName());

        navigationViewRight = (NavigationView) findViewById(R.id.nav_view_right);
        navigationViewRight.setNavigationItemSelectedListener(this);


        mRequestingLocationUpdates = true;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        //circleButton = (ImageButton) findViewById(R.id.circleButton);

        searchingGroupText = (EditText) findViewById(R.id.searchingGroupText);

//        tabhostInit();

//        mainSpinner();
//        notifications();
//        messages();
//        setupCircleButtonWithProfileImage();
//        noticeAndMessageButtons();

//        addListenerOnButton();
//        addListenerOnSpinner();
//        addListenerOnSpinner2();
//        addListenerOnSpinner3();

        layoutSettings = (View) findViewById(R.id.settingsLayout);

//        SettingButtons();

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

        /*if (hintsL > 0) {
            session.setHintsLeft(session.getHintsLeft() - 1);

            final Handler myHandler = new Handler();

            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(4000, 3999) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            if (myToolTipView == null) {
                                addMyToolTipView();
                            }
                        }

                        @Override
                        public void onFinish() {
                            if (myToolTipView != null) {
                                myToolTipView.remove();
                                myToolTipView = null;
                            }
                        }
                    }.start();
                }
            }, 3000);
        }*/

        //tylko do testu czy pobiera
        session.getFriendsList();

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

    private void addFriendEmailToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("Type here your friend email")
                .withShadow()
                .withColor(Color.RED)
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);
        friendEmailToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.friendEmail));
        friendEmailToolTipView.setOnToolTipViewClickedListener(MainActivity.this);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setupCircleButtonWithProfileImage() {
        Bitmap icon = null;

        try {
            icon = session.decodeBase64ToBitmap(SessionManager.getInstance(context).getUserPhoto());
        } catch (Exception e) {

        }

        //w razie gdyby nie bylo zadnego zdjecia, to dziwny domyslny ryj laduje na profilowym
        if (icon == null)
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.image3);

        bitmap_round = clipBitmap(icon, circleButton);
        circleButton.setImageBitmap(bitmap_round);

    }

    private void noticeAndMessageButtons() {
        //  noticeButton = (ImageButton) findViewById(R.id.noticeButton);
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.notificon);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 150, 150, true);
        Bitmap bitmap_round = clipBitmap(bMapScaled, noticeButton);

        noticeButton.setImageBitmap(bitmap_round);

        /*messageButton = (ImageButton) findViewById(R.id.messageButton);
        Bitmap bMap1 = BitmapFactory.decodeResource(getResources(), R.drawable.messageicon);
        Bitmap bMapScaled1 = Bitmap.createScaledBitmap(bMap1, 150, 150, true);
        Bitmap bitmap_round1 = clipBitmap(bMapScaled1, messageButton);

        messageButton.setImageBitmap(bitmap_round1);*/
    }

    private void notifications() {
        // spinner2 = (Spinner) findViewById(R.id.spinner2);
        readNotifications.add(0, new Notification("", "", "", "", "", "", "", "", 0));
        NotificationAdapter noticeButtonOptions = new NotificationAdapter(this, readNotifications) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                } else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }

                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };
        ;
        spinner2.setAdapter(noticeButtonOptions);
    }

    public void SettingButtons() {
        confirm = (Button) findViewById(R.id.confirmSettingsButton);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseFacebookUtils.initialize(MainActivity.this);

                if (!ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
                    ParseFacebookUtils.linkWithReadPermissionsInBackground(ParseUser.getCurrentUser(), MainActivity.this, LoginActivity.permissions, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
                                Log.d("HURRA", "Woohoo, user logged in with Facebook!");
                            }
                        }
                    });

                } else if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
                    ParseFacebookUtils.unlinkInBackground(ParseUser.getCurrentUser(), new SaveCallback() {
                        @Override
                        public void done(ParseException ex) {
                            if (ex == null) {
                                Log.d("NIE HURRA", "The user is no longer associated with their Facebook account.");
                            }
                        }
                    });
                }


                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tabhost.getApplicationWindowToken(), 0);
                layoutSettings.setVisibility(View.INVISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
            }
        });

        cancel = (Button) findViewById(R.id.cancelSettingsButton);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tabhost.getApplicationWindowToken(), 0);
                layoutSettings.setVisibility(View.INVISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
            }
        });

        changeImgFromGallery = (Button) findViewById(R.id.changeImgFromGalleryButton);
        changeImgFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 500);
                intent.putExtra("outputY", 500);

                try {

                    intent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(intent,
                            "Complete action using"), PICK_FROM_GALLERY);

                } catch (ActivityNotFoundException e) {

                }
            }


        });

        changeImgFromCamera = (Button) findViewById(R.id.changeImgFromCameraButton);
        changeImgFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 500);
                intent.putExtra("outputY", 500);

                try {

                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, PICK_FROM_CAMERA);

                } catch (ActivityNotFoundException e) {

                }
            }

        });

        changeImgFromFacebook = (Button) findViewById(R.id.buttonImageFromFacebook);

        changeImgFromFacebook.setOnClickListener(new View.OnClickListener()

                                                 {
                                                     @Override
                                                     public void onClick(View v) {

                                                         Bitmap toCrop = null;
                                                         String previouslyEncodedImage = "";
/*
                                                         if (shre.getString("facebook_image_data", "") != "") {
                                                             previouslyEncodedImage = shre.getString("facebook_image_data", "");
                                                             toCrop = decodeBase64ToBitmap(previouslyEncodedImage);
                                                         }
*/

                                                         Uri uri = getImageUri(getApplicationContext(), toCrop);
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
                                                 }

        );

        changeImgFromAdjust = (Button) findViewById(R.id.buttonImageFromAdjust);
        changeImgFromAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap toCrop = profilePictureRaw;

                Uri uri = getImageUri(getApplicationContext(), toCrop);
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
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            if (requestCode == PICK_FROM_GALLERY) {
                Bundle extras2 = data.getExtras();
                if (extras2 != null) {
                    Bitmap photo = extras2.getParcelable("data");

                    profilePictureRaw = photo;
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("photo", session.encodeBitmapTobase64(profilePictureRaw));
                    user.saveInBackground();

                    //Log.d("ZJDECIE", encodeBitmapTobase64(profilePictureRaw));
                    SessionManager.getInstance(context).setUserPhoto(session.encodeBitmapTobase64(profilePictureRaw));

                    Bitmap bitmap_round = clipBitmap(photo, circleButton);
                    circleButton.setImageBitmap(bitmap_round);

                    FileOutputStream fos = context.openFileOutput(AppConfig.IMAGE_PHOTO_FILENAME, Context.MODE_PRIVATE);
                    bitmap_round.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();


                }
            } else if (requestCode == PICK_FROM_CAMERA) {
                Bundle extras2 = data.getExtras();
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
            } else if (requestCode == CROP_IMAGE) {
                Bundle extras2 = data.getExtras();
                Bitmap photo = extras2.getParcelable("data");

                profilePictureRaw = photo;
                ParseUser user = ParseUser.getCurrentUser();
                user.put("photo", session.encodeBitmapTobase64(profilePictureRaw));
                user.saveInBackground();

                //Log.d("ZJDECIE", encodeBitmapTobase64(profilePictureRaw));
                SessionManager.getInstance(context).setUserPhoto(session.encodeBitmapTobase64(profilePictureRaw));


                Bitmap bitmap_round = clipBitmap(photo, circleButton);
                circleButton.setImageBitmap(bitmap_round);

                FileOutputStream fos = context.openFileOutput(AppConfig.IMAGE_PHOTO_FILENAME, Context.MODE_PRIVATE);
                bitmap_round.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();


            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_about) {

        }
        else if (id == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container, settingsFragment).commit();
        }
        else if (id == R.id.nav_logout) {
            logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logOut() {
        //stopLocationUpdates();

        session.logOut();

        Intent closeIntent = new Intent(this, LoginActivity.class);
        startActivity(closeIntent);
        finish();
    }

    public void addListenerOnButton() {

        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner1.performClick();
                if (myToolTipView != null) {
                    myToolTipView.remove();
                    myToolTipView = null;
                }
            }
        });
        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner2.performClick();
            }
        });
        /*messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner3.performClick();
            }
        });*/
    }


    /**
     * Tutaj definiujemy jakie operacje mają się odbyć po połączeniu z google service
     *
     * @param bundle - obiekt zawieta ewentualne dane zwracane przez usługę
     */
    @Override
    public void onConnected(Bundle bundle) {
        //Log.d(AppController.TAG, "Podłączony do api service");

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

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
        //Log.d(AppController.TAG, "Location has changed");
        float latitude = (float) location.getLatitude();
        float longitude = (float) location.getLongitude();

        // TO DO:
        // trzeba tutaj dopisać aktualizację współrzędnych!
        // TO DO.

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

    /**
     * Metoda tworząca wątek, który w sumie i tak jest już nie potrzebny, bo za wysyłynie danych o
     * lokacji będzie odpowiedzialny ChangeLocationListener, przynajniej ten kod nie straszy już w onCreate()
     *
     * @return Wątek, który ma za zadanie wysyłać co pięć sekund współrzędne do bazy danych
     */
    private Runnable createSendThread() {
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
                            // sendCordinate(session.getUserId(), (float) latitude, (float) longitude);
                        }
                        Thread.sleep(5000);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onBackPressed() {
        /*if (layoutSettings.getVisibility() == View.VISIBLE) {
            layoutSettings.setVisibility(View.INVISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
        } else if (tabLayout.getVisibility() == View.VISIBLE) {
            new AlertDialog.Builder(this).
                    setTitle("Really Exit")
                    .setMessage("Are You sure you want to exit")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        }*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (layoutMarker.getVisibility() == View.VISIBLE) {
            layoutMarker.animate()
                    .translationY(0)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            layoutMarker.setVisibility(View.GONE);
                        }
                    });
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
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
                            sendFriendshipAcceptance(session.getUserId(), not.getSenderId(), not.getSenderName(), not.getSenderEmail());


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

    private void sendFriendshipAcceptance(final String senderId, final String myreceiverid, final String receiverName, final String receiverEmail) {

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
    }

    private void sendGroupRequestAcceptance(final String senderId, final String groupId) {

        String tag_string_req = "req_friendshipRequest";
        pDialog.setMessage("Sending group request acceptance");
        showDialog();
        final String TAG = "addFriendToGroup";

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

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

    @Override
    public void onToolTipViewClicked(final ToolTipView toolTipView) {
        if (myToolTipView == toolTipView) {
            myToolTipView = null;
        }/* else if (mGreenToolTipView == toolTipView) {
            mGreenToolTipView = null;
        } else if (mBlueToolTipView == toolTipView) {
            mBlueToolTipView = null;
        } else if (mPurpleToolTipView == toolTipView) {
            mPurpleToolTipView = null;
        } else if (mOrangeToolTipView == toolTipView) {
            mOrangeToolTipView = null;
        }*/
    }

    /*@Override
    public void onClick(final View view) {
        int id = view.getId();
        if (id == R.id.circleButton) {
            if (myToolTipView == null) {
                addMyToolTipView();
            } else {
                myToolTipView.remove();
                myToolTipView = null;
            }
        }
    }*/

}
