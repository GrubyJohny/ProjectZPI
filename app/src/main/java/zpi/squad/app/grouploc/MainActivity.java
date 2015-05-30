package zpi.squad.app.grouploc;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.MediaStore;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,MarkerDialog.NoticeDialogListener, ActionBar.TabListener {

    private SessionManager session;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    Vibrator v;
    private View tabLayout;
    protected boolean inhibit_spinner = true;
    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;
    private ImageButton circleButton;
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
    private LocationManager locationManager;
    private Button mainPoiButton;
    private Button clearPoiButton;
    private Button confirm;
    private Button cancel;
    private Button changeImgFromGallery;
    private Button changeImgFromCamera;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private static final int CROP_IMAGE = 3;
    String IMAGE_PHOTO_FILENAME = "profile_photo.png";
    private Button firstMarkerButton;
    private Button secondMarkerButton;
    private Button thirdMarkerButton;
    private Button fourthMarkerButton;
    private Button closeMarkerButton;
    private Marker ostatniMarker;
    private FragmentTabHost tabhost;
    public final String FACEBOOK_PROFILE_IMAGE = "facebook_profile_image.png";

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

    //Lista aktywnych znaczników użtkownika
    private List<CustomMarker> markers;

    //OstatniKliknietyNaMapi
    private LatLng lastClikOnMap;

    AppController globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        context = getApplicationContext();
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

        tabhost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabhost.setup(context, getSupportFragmentManager(), android.R.id.tabcontent);

        tabhost.addTab(tabhost.newTabSpec("map").setIndicator("MAP"),
                Mapka.class, null);
        tabhost.addTab(tabhost.newTabSpec("friends").setIndicator("FRIENDS"),
                FriendsFragment.class, null);
        tabhost.addTab(tabhost.newTabSpec("group").setIndicator("GROUP"),
                GroupFragment.class, null);

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
        closeMarkerButton = (Button) findViewById(R.id.closeMarkerButton);

        closeMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMarker.setVisibility(View.GONE);
            }
        });

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setCancelable(false);

        POIScrollView = (ScrollView) findViewById(R.id.POIScroll);

        //Start-up markers list
        markers=db.getAllMarkers();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void setupCircleButtonWithProfileImage() {
        Bitmap icon = null;
        circleButton = (ImageButton) findViewById(R.id.circleButton);
        try {
            File filePath = context.getFileStreamPath(FACEBOOK_PROFILE_IMAGE);
            FileInputStream fi = new FileInputStream(filePath);
            icon = BitmapFactory.decodeStream(fi);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            File filePath = context.getFileStreamPath(IMAGE_PHOTO_FILENAME);
            FileInputStream fi = null;
            try {
                fi = new FileInputStream(filePath);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            icon = BitmapFactory.decodeStream(fi);
        }

        //w razie gdyby nie byĹ‚o jeszcze ĹĽadnego naszego zdjÄ™cia, to johny lÄ…duje na profilowym
        if(icon==null)
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.coffee);

        Bitmap bMapScaled = Bitmap.createScaledBitmap(icon, 150, 150, true);
        Bitmap bitmap_round = clipBitmap(bMapScaled);
        circleButton.setImageBitmap(bitmap_round);

    }

    private void noticeAndMessageButtons() {
        noticeButton = (ImageButton) findViewById(R.id.noticeButton);
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.notificon);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 100, 100, true);
        Bitmap bitmap_round1 = clipBitmap(bMapScaled);

        noticeButton.setImageBitmap(bitmap_round1);

        messageButton = (ImageButton) findViewById(R.id.messageButton);
        Bitmap bMap1 = BitmapFactory.decodeResource(getResources(), R.drawable.messageicon);
        Bitmap bitmap_round2 = clipBitmap(bMap1);
        Bitmap bMapScaled1 = Bitmap.createScaledBitmap(bitmap_round2, 100, 100, true);

        messageButton.setImageBitmap(bMapScaled1);
    }

    private void mainSpinner() {
        spinner1 = (Spinner) findViewById(R.id.spinner);
        String[] spinnerOptions = {"", "Settings", "Log out"};
        ArrayAdapter<String> circleButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptions){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else {
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
        readNotifications.add(0,new Notification("","","","","","","","",0));
        NotificationAdapter noticeButtonOptions = new NotificationAdapter(this, readNotifications){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }

                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };;
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
                //layoutFlipper.setVisibility(View.VISIBLE);
                layoutSettings.setVisibility(View.INVISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
            }
        });

        cancel = (Button) findViewById(R.id.cancelSettingsButton);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //layoutFlipper.setVisibility(View.VISIBLE);
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

                //SZCZUREK LAYOUT DESIGNER - tutaj możesz zmienić rozmiary zdjęcia które, zwraca galeria
                intent.putExtra("outputX", 250);
                intent.putExtra("outputY", 250);

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
                intent.putExtra("outputX", 250);
                intent.putExtra("outputY", 250);

                try {

                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, PICK_FROM_CAMERA);

                } catch (ActivityNotFoundException e) {

                }
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
                    Bitmap bitmap_round = clipBitmap(photo);
                    circleButton.setImageBitmap(bitmap_round);

                    FileOutputStream fos = context.openFileOutput(IMAGE_PHOTO_FILENAME, Context.MODE_PRIVATE);
                    bitmap_round.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();

                    Toast.makeText(this, "Profile image changed successfully!",
                            Toast.LENGTH_SHORT).show();

                }}
            else if (requestCode == PICK_FROM_CAMERA) {
                Bundle extras2 = data.getExtras();
                if (extras2 != null) {
                    Uri uri = data.getData();
                    Intent cropIntent = new Intent("com.android.camera.action.CROP");
                    //indicate image type and Uri
                    cropIntent.setDataAndType(uri, "image/*");
                    //set crop properties
                    cropIntent.putExtra("crop", "true");
                    //indicate aspect of desired crop
                    cropIntent.putExtra("aspectX", 1);
                    cropIntent.putExtra("aspectY", 1);
                    //indicate output X and Y
                    cropIntent.putExtra("outputX", 250);
                    cropIntent.putExtra("outputY", 250);
                    //retrieve data on return
                    cropIntent.putExtra("return-data", true);
                    //start the activity - we handle returning in onActivityResult
                    startActivityForResult(cropIntent, CROP_IMAGE);
                }
            }
            else if(requestCode == CROP_IMAGE)
            {
                Bundle extras2 = data.getExtras();
                Bitmap photo = extras2.getParcelable("data");

                Bitmap bitmap_round = clipBitmap(photo);
                circleButton.setImageBitmap(bitmap_round);

                FileOutputStream fos = context.openFileOutput(IMAGE_PHOTO_FILENAME, Context.MODE_PRIVATE);
                bitmap_round.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                Toast.makeText(this, "Profile image changed successfully!",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
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

        stopLocationUpdates();

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
                //Log.d(TAG, response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    JSONArray array = jObj.getJSONArray("notifications");
                    JSONObject notObj;
                    String senderId;
                    String senderName;
                    String senderEmail;
                    String receiverId;
                    String type;
                    String messageId;
                    String groupId;
                    String createdAt;

                    for (int i = 0; i < array.length(); i++) {
                        notObj = array.getJSONObject(i);
                        senderId = notObj.getString("senderid");
                        senderName = notObj.getString("senderName");
                        senderEmail = notObj.getString("senderEmail");
                        receiverId = notObj.getString("receiverid");
                        type = notObj.getString("type");
                        messageId = notObj.getString("messageid");
                        groupId = notObj.getString("groupid");
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
                            // final int id = Integer.valueOf(senderId);
                            // final String n = senderName;
                            // final String e = senderEmail;
                            // Log.d("MOJLOG", "weszlo");
                        }
                    }

                }catch(Exception e){
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
            }
        });
        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner2.performClick();
            }
        });
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner3.performClick();
            }
        });
    }

    public void addListenerOnSpinner() {
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ( position) {
                    case 0:
                        break;
                    case 1:
                        layoutSettings.setVisibility(View.VISIBLE);
                        tabLayout.setVisibility(View.INVISIBLE);
                        layoutMarker.setVisibility(View.GONE);
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

        mCurrentLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
        float latitude=(float)location.getLatitude();
        float longitude=(float)location.getLongitude();
        //Toast.makeText(getApplicationContext(), "Szerokość + " + latitude + " Długość: " + longitude, Toast.LENGTH_SHORT).show();
        stayActive(session.getUserId(), (float) latitude, (float) longitude);

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

    @Override
    protected void onDestroy() {
        db.deleteMarkers();
        saveToSQLiteDataBase();

        super.onDestroy();
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

    //Rozpoczynam pisanie kodu odpowiedzialnego za pokazanie dokładnej trasy


    //no to pobierzmy tego jsona

   /* @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        MarkerDialog md=(MarkerDialog) dialog;
        String name=md.getName();
        Log.d("Marker Dialog",name);
        myMap.addMarker(new MarkerOptions().position(lastClikOnMap).draggable(true).title(name));
        CustomMarker mark=new CustomMarker(null,session+"",lastClikOnMap.latitude,lastClikOnMap.longitude,name);
        markers.add(mark);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(md.getInput().getWindowToken(), 0);

    }*/

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

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
                .setNegativeButton(android.R.string.no,null)
                .setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {
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
                .setNegativeButton(android.R.string.no,null)
                .setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(not.getType().equals("friendshipRequest")){
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


    public void saveToSQLiteDataBase(){
        for(CustomMarker m:markers)
        {
            db.addMarker(m);
            System.out.println("MARKER ZAPISYWANY: " + m);
        }
    }

    @Override
    public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog) {

        MarkerDialog md=(MarkerDialog) dialog;
        String name=md.getName();
        Log.d("Marker Dialog", name);
        globalVariable.getMyMap().addMarker(new MarkerOptions().position(globalVariable.getLastClikOnMap()).draggable(true).title(name));
        CustomMarker mark=new CustomMarker(null,session+"",globalVariable.getLastClikOnMap().latitude,globalVariable.getLastClikOnMap().longitude,name);
        markers.add(mark);
        for(CustomMarker m: markers)
            System.out.println("MARKER: " + m);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(md.getInput().getWindowToken(), 0);
    }
}
