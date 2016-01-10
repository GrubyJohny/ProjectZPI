package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    public static Context context;
    private CallbackManager callbackManager;
    SharedPreferences shre;
    SharedPreferences.Editor edit;
    private LoginButton loginButton;
    private String facebookUserId, facebookUserEmail, facebookUserName;
    List<String> permissions;
    ProgressDialog dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext());
        context = getApplicationContext();
        shre = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit=shre.edit();


        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                if (email.trim().length() > 0 && password.trim().length() > 0) {
                    dialog = ProgressDialog.show(LoginActivity.this, "Loading", "Please wait...", true);
                    dialog.create();
                    dialog.show();
                    if(AppController.checkConn(LoginActivity.this.getApplication()))
                    {
                        //Toast.makeText(context, "Please wait...", Toast.LENGTH_LONG).show();

                        checkLogin(email, password);
                        edit.putString("kind_of_login", "normal");
                    }
                    else{
                        Toast.makeText(getApplicationContext(),
                                "No connection to internet detected. Unfortunately it's is impossible to login", Toast.LENGTH_LONG).show();
                    }

                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
            }

        });

        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {


                if(AppController.checkConn(LoginActivity.this.getApplication())) {
                    Intent i = new Intent(getApplicationContext(),
                            RegisterActivity.class);
                    startActivity(i);
                    //finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "No connection to internet detected. Unfortunately it's is impossible to login", Toast.LENGTH_LONG).show();
                }
            }
        });

        //facebook
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setText("Register with Facebook");
        loginButton.setReadPermissions("public_profile");

        permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");
        permissions.add("user_birthday");
        loginButton.setReadPermissions(permissions);


        if (null == callbackManager)
            callbackManager = CallbackManager.Factory.create();


        /*loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(LoginActivity.this, "Loading", "Please wait...", true);
                loginButton.registerCallback(callbackManager, _mcallbackLogin);
            }
        });*/

        loginButton.registerCallback(callbackManager, _mcallbackLogin);


    }

    public final FacebookCallback<LoginResult> _mcallbackLogin = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(final LoginResult loginResult) {

            if (loginResult.getAccessToken() != null) {
               // Log.i("TAG", "LoginButton FacebookCallback onSuccess token : " + loginResult.getAccessToken().getToken());
                GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (null != object) {
                            //Log.e("TAG", object.optString("name").toString() + " " + object.optString("first_name").toString() + " " + object.optString("email").toString());
                            facebookUserName = object.optString("name").toString();
                            facebookUserEmail = object.optString("email").toString();
                            facebookUserId = loginResult.getAccessToken().getUserId();

                            registerFacebookUser(facebookUserName, facebookUserEmail, facebookUserId);

                            //zapis zdjecia w shared preferences
                            Bitmap realImage = null;
                            try {
                                realImage = getFacebookProfilePicture(facebookUserId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            if (realImage != null) {

                               String encodedImage = encodeBitmapTobase64(realImage);

                                edit.putString("facebook_image_data", encodedImage);
                                edit.commit();
                                edit.putString("kind_of_login", "facebook");

                            }
                            else
                            {
                                Log.e("FACEBOOK", "Profile image = null");
                            }

                        }

                    }

                }).executeAsync();

                //dialog.dismiss();
            }
        }

        @Override
        public void onCancel() {
            Log.e("TAG", "LoginButton FacebookCallback onCancel");
        }

        @Override
        public void onError(FacebookException exception) {
            Log.e("TAG", "Exception:: " + exception.getStackTrace());
        }
    };


    public static Bitmap getFacebookProfilePicture(String userID) throws SocketException, SocketTimeoutException, MalformedURLException, IOException, Exception {
        String imageURL;

        Bitmap bitmap = null;
        imageURL = "https://graph.facebook.com/" + userID + "/picture?width=300&length=300";
        InputStream in = (InputStream) new URL(imageURL).getContent();
        bitmap = BitmapFactory.decodeStream(in);

        return bitmap;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


    private void checkLogin(final String email, final String password) {

        String tag_string_req = "req_login";


        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());


                try {

                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        session.setLogin(true);


                        JSONObject user = jObj.getJSONObject("user");
                        String uid = user.getString("uid");
                        String name = user.getString("name");
                        String email = user.getString("email");

                        session.setKeyUid(uid);
                        session.setKeyName(name);
                        session.setKeyEmail(email);


                        getUserInfo(uid);
                        AppController globalVariable = AppController.getInstance();
                        //  Sender.sendRequestAboutMarkers(uid,globalVariable.getMarkers(),globalVariable.getMyMap());


                    } else {

                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.toString());

                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        hideDialog();
        /*if(dialog.isShowing())
            dialog.dismiss();*/
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void getUserInfo(final String id) {

        String tag_string_req = "req_friendships";
        pDialog.setMessage("Logging in ...");
        showDialog();
        final String TAG = "List of friends and notifications request";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                //hideDialog();
                try {

                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        JSONArray array = jObj.getJSONArray("friends");
                        JSONObject friendObj;
                        String uid;
                        String name;
                        String email;
                        for (int i = 0; i < array.length(); i++) {
                            friendObj = array.getJSONObject(i);
                            uid = friendObj.getString("uid");
                            name = friendObj.getString("name");
                            email = friendObj.getString("email");

                            db.addFriend(uid, name, email);
                        }

                        JSONArray array2 = jObj.getJSONArray("oldNotifications");
                        JSONObject oldNotObj;
                        String senderId;
                        String senderName;
                        String senderEmail;
                        String receiverId;
                        String type;
                        String messageId;
                        String groupId;
                        String createdAt;

                        for (int i = 0; i < array2.length(); i++) {
                            oldNotObj = array2.getJSONObject(i);
                            senderId = oldNotObj.getString("senderid");
                            senderName = oldNotObj.getString("senderName");
                            senderEmail = oldNotObj.getString("senderEmail");
                            receiverId = oldNotObj.getString("receiverid");
                            type = oldNotObj.getString("type");
                            messageId = oldNotObj.getString("messageid");
                            groupId = oldNotObj.getString("groupid");
                            createdAt = oldNotObj.getString("created_at");

                            db.addNotification(senderId, senderName, senderEmail, receiverId, type, messageId, groupId, createdAt, 1);
                        }

                        JSONArray array3 = jObj.getJSONArray("notifications");
                        JSONObject notObj;


                        for (int i = 0; i < array3.length(); i++) {
                            notObj = array3.getJSONObject(i);
                            senderId = notObj.getString("senderid");
                            senderName = notObj.getString("senderName");
                            senderEmail = notObj.getString("senderEmail");
                            receiverId = notObj.getString("receiverid");
                            type = notObj.getString("type");
                            messageId = notObj.getString("messageid");
                            groupId = notObj.getString("groupid");
                            createdAt = notObj.getString("created_at");

                            db.addNotification(senderId, senderName, senderEmail, receiverId, type, messageId, groupId, createdAt, 0);
                        }

                        JSONArray markersArray = jObj.getJSONArray("markers");
                        for (int i = 0; i < markersArray.length(); i++)

                        {
                            //Log.d(TAG, "a jjajajjajjjjajajaj");
                            JSONObject marker = markersArray.getJSONObject(i);
                            int markerid = marker.getInt("markerid");
                            uid = Integer.toString(marker.getInt("uid"));
                            double latitude = marker.getDouble("latitude");
                            double longitude = marker.getDouble("longitude");
                            name = marker.getString("name");
                            CustomMarker customMarker = new CustomMarker(markerid + "", uid + "", latitude, longitude, name);
                            customMarker.setSaveOnServer(true);
                            long SQLiteID = db.addMarker(customMarker);
                            customMarker.setMarkerIdSQLite(Long.toString(SQLiteID));
                        }

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                       // Toast.makeText(getApplicationContext(), "Pomyślnie odebrano listę znajomych", Toast.LENGTH_LONG).show();
                       // Toast.makeText(getApplicationContext(), "Pomyślnie odebrano listę powiadomień", Toast.LENGTH_LONG).show();

                    } else {

                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "List of friends request Error: " + error.toString());

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "startSession");
                params.put("id", id);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        hideDialog();
    }

    private void registerFacebookUser(final String name, final String email,
                                      final String password) {

        String tag_string_req = "req_register";
        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register on server Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                       // Toast.makeText(getApplicationContext(), "Pomyślnie zarejestrowano użytkownika", Toast.LENGTH_LONG).show();

                        session.setLogin(true);
                        JSONObject user = jObj.getJSONObject("user");
                        String uid = user.getString("uid");
                        String name = user.getString("name");
                        String email = user.getString("email");

                        session.setKeyUid(uid);
                        session.setKeyName(name);
                        session.setKeyEmail(email);


                        getUserInfo(uid);

                    } else {

                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "facebook");
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);

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

        return imageEncoded;
    }


}