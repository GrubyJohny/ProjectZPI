package zpi.squad.app.grouploc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.format.Time;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ToolTipView.OnToolTipViewClickedListener{

    private static Resources resources;
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
    private Button firstMarkerButton;
    private Button secondMarkerButton;
    private Button thirdMarkerButton;
    private Button fourthMarkerButton;
    //private Button closeMarkerButton;
    private Marker ostatniMarker;
    private FragmentTabHost tabhost;
    //public final String FACEBOOK_PROFILE_IMAGE = "pikczer.png";


    private ScrollView POIScrollView;
    PoiJSONParser poiBase = new PoiJSONParser();
    public static Context context;
    ArrayList<Notification> readNotifications = new ArrayList<Notification>();


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

    //OstatniKliknietyNaMapi
    private LatLng lastClikOnMap;

    public static final String IMAGE_PHOTO_FILENAME = "facebook_profile_photo";

    SharedPreferences.Editor edit;
    SharedPreferences shre;
    Bitmap profilePictureRaw;

    AppController globalVariable;

    private ToolTipView myToolTipView;
    private ToolTipView friendEmailToolTipView;
    ToolTipRelativeLayout toolTipRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        shre = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit=shre.edit();
        context = getApplicationContext();
        resources = getResources();
        globalVariable = (AppController) getApplicationContext();
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        StrictMode.setThreadPolicy(policy);
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());
        tabLayout = (View) findViewById(R.id.tabLayout);

        sender = createSendThread();
        mRequestingLocationUpdates = true;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        circleButton = (ImageButton) findViewById(R.id.circleButton);

        searchingGroupText = (EditText) findViewById(R.id.searchingGroupText);

        tabhostInit();

        new Thread(sender, "Watek do wysyłania koordynatów").start();

        readNotifications = db.getAllNotifications();

        mainSpinner();
        notifications();
        messages();
        setupCircleButtonWithProfileImage();
        noticeAndMessageButtons();

        addListenerOnButton();
        addListenerOnSpinner();
        addListenerOnSpinner2();
        addListenerOnSpinner3();

        layoutSettings = (View) findViewById(R.id.settingsLayout);

        SettingButtons();

        layoutMarker = (View) findViewById(R.id.markerLayout);

        firstMarkerButton = (Button) findViewById(R.id.firstButton);
        secondMarkerButton = (Button) findViewById(R.id.secondButton);
        thirdMarkerButton = (Button) findViewById(R.id.thirdButton);
        fourthMarkerButton = (Button) findViewById(R.id.fourthButton);
        /*closeMarkerButton = (Button) findViewById(R.id.closeMarkerButton);

        closeMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMarker.setVisibility(View.GONE);
            }
        });*/

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setCancelable(false);

        POIScrollView = (ScrollView) findViewById(R.id.POIScroll);

        //Start-up markers list
        //markers = db.getAllMarkers();

        toolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipRelativeLayout);
        toolTipRelativeLayout.bringToFront();

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
    }

    private void addMyToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("Click here for settings")
                .withShadow()
                .withColor(Color.RED)
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);
        myToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.circleButton));
        myToolTipView.setOnToolTipViewClickedListener(MainActivity.this);
    }

    private void addFriendEmailToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("Type here your friend email")
                .withShadow()
                .withColor(Color.RED)
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);
        friendEmailToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.friendEmail));
        friendEmailToolTipView.setOnToolTipViewClickedListener(MainActivity.this);
    }

    private void tabhostInit() {
        tabhost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabhost.setup(context, getSupportFragmentManager(), android.R.id.tabcontent);

        tabhost.addTab(tabhost.newTabSpec("map").setIndicator("MAP"),
                Mapka.class, null);
        tabhost.addTab(tabhost.newTabSpec("friends").setIndicator("FRIENDS"),
                FriendsFragment.class, null);
        tabhost.addTab(tabhost.newTabSpec("group").setIndicator("GROUP"),
                GroupFragment.class, null);

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabhost.getCurrentTab() == 1){
                    Log.d("CHUJ","WESZLO W TABA");
                    if(session.getHintsLeft() > 0){
                        session.setHintsLeft(session.getHintsLeft() - 1);
                        final Handler myHandler1 = new Handler();

                        myHandler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new CountDownTimer(4000, 3999) {

                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        if (friendEmailToolTipView == null) {
                                            addFriendEmailToolTipView();
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        Log.d("GOWNO", "JEST");
                                        if (friendEmailToolTipView != null) {
                                            friendEmailToolTipView.remove();
                                            friendEmailToolTipView = null;
                                        }
                                    }
                                }.start();
                            }
                        }, 3000);
                    }
                }

                layoutMarker.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tabhost.getApplicationWindowToken(), 0);
            }
        });

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setupCircleButtonWithProfileImage() {
        Bitmap icon = null;

        String previouslyEncodedImage;
        String kindOfLoginn = shre.getString("kind_of_login", "");
        Drawable fromFTP = null;

        // pobierz zdjęcie z serwera i załaduj jako profilowe
        fromFTP =  getImageFromFTP(Integer.parseInt(session.getUserId()));
        if(fromFTP != null)
        {
            icon= drawableToBitmap(fromFTP);
            profilePictureRaw = icon;
            String enco = encodeBitmapTobase64(icon);
            edit.putString("image_data", enco);
            edit.commit();
        }

        else
        {
            //wyslij zdj z fejsa do ftp
            String image = shre.getString("facebook_image_data", "");
            Log.e("SRATATATA", image);
            Bitmap tmp = decodeBase64ToBitmap(image);
            //Drawable tmpp = new BitmapDrawable(getResources(), tmp);
            profilePictureRaw = tmp;
            icon = tmp;

            if(!image.isEmpty())
            {
                Log.d("ATLAS", "KLEJ OK");
                //upload zdjecia do ftp
                uploadProfileImageToFTP();
            }
        }

        //w razie gdyby nie bylo zadnego zdjecia, to dziwny domyslny ryj laduje na profilowym
        if (icon == null)
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.image3);


        //Bitmap bMapScaled = Bitmap.createScaledBitmap(icon, 150, 150, true);
        Bitmap bitmap_round = clipBitmap(icon, circleButton);
        circleButton.setImageBitmap(bitmap_round);

    }

    private void noticeAndMessageButtons() {
        noticeButton = (ImageButton) findViewById(R.id.noticeButton);
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

    private void mainSpinner() {
        spinner1 = (Spinner) findViewById(R.id.spinner);
        String[] spinnerOptions = {"", "Settings", "Log out"};
        ArrayAdapter<String> circleButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptions) {
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
        spinner1.setAdapter(circleButtonOptions);
    }

    private void notifications() {
        spinner2 = (Spinner) findViewById(R.id.spinner2);
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

    private void messages() {
        spinner3 = (Spinner) findViewById(R.id.spinner3);
        String[] spinner3Options = {"message 1", "message 2", "message 3", "message 4"};
        ArrayAdapter<String> messageButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinner3Options);
        spinner3.setAdapter(messageButtonOptions);
    }

    public void SettingButtons() {
        confirm = (Button) findViewById(R.id.confirmSettingsButton);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        changeImgFromFacebook=(Button) findViewById(R.id.buttonImageFromFacebook);

        changeImgFromFacebook.setOnClickListener(new View.OnClickListener()

                                                 {
                                                     @Override
                                                     public void onClick(View v) {

                                                         Bitmap toCrop = null;
                                                         String previouslyEncodedImage = "";

                                                         if (shre.getString("facebook_image_data", "") != "") {
                                                             previouslyEncodedImage = shre.getString("facebook_image_data", "");
                                                             toCrop = decodeBase64ToBitmap(previouslyEncodedImage);
                                                         }


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
                        uploadProfileImageToFTP();
                    Bitmap bitmap_round = clipBitmap(photo, circleButton);
                    circleButton.setImageBitmap(bitmap_round);

                    FileOutputStream fos = context.openFileOutput(IMAGE_PHOTO_FILENAME, Context.MODE_PRIVATE);
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

                uploadProfileImageToFTP();

                Bitmap bitmap_round = clipBitmap(photo, circleButton);
                circleButton.setImageBitmap(bitmap_round);

                FileOutputStream fos = context.openFileOutput(IMAGE_PHOTO_FILENAME, Context.MODE_PRIVATE);
                bitmap_round.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();


            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
            //Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut(AdapterView.OnItemSelectedListener view) {

        //stopLocationUpdates();

        session.logOut();


        Intent closeIntent = new Intent(this, LoginActivity.class);
        startActivity(closeIntent);
        finish();
    }

    public void stayActive(final String id, final float c1, final float c2) {
        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String TAG = "Sending coordinates & checking for notifications";
                Log.d(TAG, response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    JSONArray markersArray = jObj.getJSONArray("markers");
                    JSONObject markerObj;
                    CustomMarker myMarker;
                    String mySqlId;
                    String userId;
                    Double latitude;
                    Double longitude;
                    String markerName;

                    JSONArray array = jObj.getJSONArray("notifications");
                    JSONObject notObj;
                    String senderId;
                    String senderName;
                    String senderEmail;
                    String receiverId;
                    String type;
                    String messageId;
                    String groupId;
                    //String markerId;
                    String createdAt;


                    //Tutaj jest doddawany do lokalnej bazy znacznik
                    //wspóldzielony przez przyleciela. Karol za to odpowiada.
                    for(int i=0; i < markersArray.length(); i++){
                        markerObj = markersArray.getJSONObject(i);
                        mySqlId = markerObj.getString("mysqlid");
                        userId = markerObj.getString("userid");
                        latitude = markerObj.getDouble("latitude");
                        longitude = markerObj.getDouble("longitude");
                        markerName = markerObj.getString("markername");

                        myMarker = new CustomMarker(mySqlId, userId, latitude, longitude, markerName);
                        db.addMarker(myMarker);
                        globalVariable.addNewMarker(myMarker);
                        Log.d("???","co tu się  wyprawia");

                    }

                    for (int i = 0; i < array.length(); i++) {
                        notObj = array.getJSONObject(i);
                        senderId = notObj.getString("senderid");
                        senderName = notObj.getString("senderName");
                        senderEmail = notObj.getString("senderEmail");
                        receiverId = notObj.getString("receiverid");
                        type = notObj.getString("type");
                        messageId = notObj.getString("messageid");
                        groupId = notObj.getString("groupid");
                        //markerId = notObj.getString("markerid");
                        createdAt = notObj.getString("created_at");
                        v.vibrate(500);
                        db.addNotification(senderId, senderName, senderEmail, receiverId, type, messageId, groupId, createdAt, 0);
                        readNotifications.add(1, (new Notification(senderId, senderName, senderEmail, receiverId, type, messageId, groupId, createdAt, 0)));
                        Toast.makeText(getApplicationContext(), "You have new notification", Toast.LENGTH_LONG).show();
                        if (type.equals("friendshipAgreed")) {

                            db.addFriend(senderId, senderName, senderEmail);
                            FriendsFragment.addFriend(new Friend(Integer.valueOf(senderId), senderName, senderEmail));
                        }
                        else if (type.equals("friendshipCanceled")) {
                            FriendsFragment.removeItem(senderEmail);
                        }
                        else if (type.equals("friendshipRequest")) {


                        }
                        else if (type.equals("shareMarker")) {

                        }
                    }

                } catch (Exception e) {
                }
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

                Log.e("OnLocationChanged", "Notification Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "userConnection");
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

    public void addListenerOnSpinner() {
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        layoutSettings.setVisibility(View.VISIBLE);
                        layoutMarker.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        spinner1.setSelection(0);
                        break;
                    case 2:
                        logOut(this);
                        //spinner1.setSelection(0);
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
                if (inhibit_spinner) {
                    inhibit_spinner = false;
                } else {

                    if (readNotifications.get(position).getType().equals("friendshipRequest") && !readNotifications.get(position).isChecked()) {
                        onFriendshipRequest(readNotifications.get(position));

                    } else {

                    }
                }
                spinner2.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    public void addListenerOnSpinner3() {
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    /**
     * Tutaj definiujemy jakie operacje mają się odbyć po połączeniu z google service
     *
     * @param bundle - obiekt zawieta ewentualne dane zwracane przez usługę
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(AppController.TAG, "Podłączony do api service");

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // setUpMap(true);


        //setMapListener();
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

        String whereClusere= Sender.makeStatementAboutFriendsList(db.getAllFriends());
       // Log.d("pobieranie znajomych",whereClusere);
        Sender.sendRequestAboutFriendsCoordinate(whereClusere, AppController.getInstance().getMyMap());

        stayActive(session.getUserId(), (float) latitude, (float) longitude);

    }

    public Bitmap clipBitmap(Bitmap bitmap, ImageButton x) {
        if (bitmap == null)
            return null;
        final int width = x.getLayoutParams().width;
        final int height = x.getLayoutParams().height;
        bitmap = bitmap.createScaledBitmap(bitmap,width,height,true);

        final int bWidth = bitmap.getWidth();
        final int bHeight = bitmap.getHeight();

        System.out.println("WYMIARY " + width + "," + height + " ; bitmapa " + bWidth + ", " + bHeight);

        DisplayMetrics metrics = new DisplayMetrics();

        //zakomentowałem to, bo w Sender.java nie mogłem użyć getWindowManager()
        // wygląda jakby działało bez tego w porządku /Johny
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Bitmap outputBitmap = Bitmap.createBitmap(metrics,width, height, Bitmap.Config.ARGB_8888);

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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
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
                        FriendsFragment.addFriend(new Friend(Integer.valueOf(myreceiverid), receiverName, receiverEmail));


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



    // method for bitmap to base64
    public static String encodeBitmapTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    // method for base64 to bitmap
    public static Bitmap decodeBase64ToBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private void uploadProfileImageToFTP()
    {

        //upload zdjecia do ftp
        FTPClient con = null;

        try {
            con = new FTPClient();
            con.connect("ftp.marcinta.webd.pl");

            if (con.login("grouploc@marcinta.webd.pl", "grouploc2015")) {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                //create a file to write bitmap data
                String ak = Time.SECOND+""+ Time.MINUTE;
                File f = new File(context.getCacheDir(), ak);
                boolean res = f.createNewFile();
                //Log.d("TAKCZYNIE", ""+res);

//Convert bitmap to byte array
                Bitmap bitmap = profilePictureRaw;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                //Log.d("ZDJECIE", bitmapdata.toString());

//write the bytes in file
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();


                FileInputStream in = new FileInputStream(new File(context.getCacheDir()+"/"+ak));

                try {
                    deleteFile(session.getUserId() + ".png");
                }
                catch(Exception e)
                {
                    //e.toString();
                }

                boolean result = con.storeFile("/" + session.getUserId() + ".png", in);

                in.close();
                if (result) Log.v("moj upload", "succeeded");
                con.logout();
                con.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR FTP", e.getMessage());
        }
    }

    public static Drawable getImageFromFTP(int userID) //może zwracać null - uwaga dla Szczurka
    {

        Bitmap icon = null;
        FTPClient con = null;
        Drawable phot =null;
        try
        {
            con = new FTPClient();
                    con.connect("ftp.marcinta.webd.pl");
            Log.e("przed getFriendPhoto", "wszedl");
            if (con.login("grouploc@marcinta.webd.pl", "grouploc2015"))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                Log.e("przed getFriendPhoto", "wszedl2" + userID);

               phot = Drawable.createFromStream(con.retrieveFileStream(userID+".png"), "userID");
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            Log.v("download result","failed");
            e.printStackTrace();
        }
        Log.d("PRAWDA", "" + (phot != null));

        //return phot==null?resources.getDrawable(R.drawable.image3):phot;
        return phot;


    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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
