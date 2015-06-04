package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public final String FACEBOOK_PROFILE_IMAGE = "facebook_profile_image.png";
    private Bitmap bitmap;
    private LoginButton loginButton;
    private String facebookUserId, facebookUserEmail, facebookUserName;
    List<String> permissions;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext());
        context = getApplicationContext();

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
                    checkLogin(email, password);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
            }

        });

        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
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

        loginButton.registerCallback(this.callbackManager, _mcallbackLogin);
    }

    public final  FacebookCallback<LoginResult> _mcallbackLogin =    new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(final LoginResult loginResult) {

            if(loginResult.getAccessToken() != null){
                Log.i("TAG", "LoginButton FacebookCallback onSuccess token : "+ loginResult.getAccessToken().getToken());
                GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (null != object) {
                            Log.e("TAG", object.optString("name").toString() + " " + object.optString("first_name").toString()+" "+object.optString("email").toString());
                            facebookUserName = object.optString("name").toString();
                            facebookUserEmail = object.optString("email").toString();
                            facebookUserId = loginResult.getAccessToken().getUserId();

                            Log.d("FEJS", facebookUserName);
                            Log.d("FEJS", facebookUserEmail);
                            Log.d("FEJS", facebookUserId);

                            registerFacebookUser(facebookUserName, facebookUserEmail, facebookUserId);

                            //jeszcze zdjęcie

                            try {
                                bitmap = getFacebookProfilePicture(facebookUserId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Bitmap photo = bitmap;

                            FileOutputStream fos = null;
                            try {
                                fos = context.openFileOutput(FACEBOOK_PROFILE_IMAGE, Context.MODE_PRIVATE);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            photo.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(LoginActivity.this, "Profile image saved successfully!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).executeAsync();


            }
        }

        @Override
        public void onCancel() {
            Log.e("TAG", "LoginButton FacebookCallback onCancel");
        }

        @Override
        public void onError(FacebookException exception) {
            Log.e("TAG","Exception:: "+exception.getStackTrace());
        }
    };



    public static Bitmap getFacebookProfilePicture(String userID) throws SocketException, SocketTimeoutException, MalformedURLException, IOException, Exception
    {
        String imageURL;

        Bitmap bitmap = null;
        imageURL = "https://graph.facebook.com/"+userID+"/picture?width=200&length=200";
        InputStream in = (InputStream) new URL(imageURL).getContent();
        bitmap = BitmapFactory.decodeStream(in);

        return bitmap;
    }


    public static String fromStream(InputStream in) throws IOException
    {
        Log.d("STRIM", in.toString());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        return out.toString();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


    private void checkLogin(final String email, final String password) {

        String tag_string_req = "req_login";
        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

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
                        AppController global=AppController.getInstance();
                      //  Sender.sendRequestAboutMarkers(uid,global.getMarkers(),global.getMyMap());


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
        pDialog.setMessage("Sending Request for list of friends and notifications");
        showDialog();
        final String TAG = "List of friends and notifications request";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                hideDialog();
                try {

                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        JSONArray array = jObj.getJSONArray("friends");
                        JSONObject friendObj;
                        String uid;
                        String name;
                        String email;
                        for(int i=0; i<array.length(); i++){
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

                        for(int i=0; i<array2.length(); i++){
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


                        for(int i=0; i<array3.length(); i++){
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

                        JSONArray markersArray=jObj.getJSONArray("markers");
                        for(int i=0;i<markersArray.length();i++)

                        {
                            Log.d(TAG,"a jjajajjajjjjajajaj");
                            JSONObject marker=markersArray.getJSONObject(i);
                            int markerid=marker.getInt("markerid");
                            uid= Integer.toString(marker.getInt("uid"));
                            double latitude=marker.getDouble("latitude");
                            double longitude=marker.getDouble("longitude");
                            name=marker.getString("name");
                            CustomMarker customMarker =new CustomMarker(markerid+"",uid+"",latitude,longitude,name);
                            customMarker.setSaveOnServer(true);
                            long SQLiteID=db.addMarker(customMarker);
                            customMarker.setMarkerIdSQLite(Long.toString(SQLiteID));
                            AppController.getInstance().addToMarkers(customMarker);
                        }

                 

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(getApplicationContext(), "Pomyślnie odebrano listę znajomych", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Pomyślnie odebrano listę powiadomień", Toast.LENGTH_LONG).show();

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
                hideDialog();

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
                        Toast.makeText(getApplicationContext(), "Pomyślnie zarejestrowano użytkownika", Toast.LENGTH_LONG).show();

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

                }else if (error instanceof TimeoutError) {
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


}