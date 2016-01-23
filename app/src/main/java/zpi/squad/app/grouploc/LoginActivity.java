package zpi.squad.app.grouploc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin, btnLoginWithFacebook, btnRegister;
    //private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    //private ProgressDialog pDialog;
    //private SessionManager session;
    private SQLiteHandler db;
    public static Context context;
    public static List<String> permissions = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = getApplicationContext();
        if (SessionManager.getInstance(context).isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }

        //tę linijkę oczywiście trzeba później wyrzucić
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try{
            Parse.initialize(this, AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);
            //ParseFacebookUtils.initialize(context);
        }
        catch(Exception e)
        {
            // e.printStackTrace();
        }

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLoginWithFacebook = (Button) findViewById(R.id.login_button_facebook);
        btnRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        db = new SQLiteHandler(getApplicationContext());




        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString();

                if (email.trim().length() > 2 && password.trim().length() > 0 && email.contains("@") && email.contains(".")) {
                    if (AppController.checkConn(LoginActivity.this.getApplication())) {
                        checkLogin(email, password);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No connection to internet detected. Unfortunately it's is impossible to login", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
            }

        });



        permissions.add("public_profile");
        permissions.add("email");

        btnLoginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ParseFacebookUtils.initialize(context);
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback()
                {
                    @Override
                    public void done(final ParseUser user, ParseException err) {
                        if (user == null)
                        {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        }
                        else if (user.isNew())
                        {
                            try
                            {
                                Log.d("MyApp", "" + user.fetchIfNeeded().getEmail());
                                String[] tempInfo = getFacebookUserInfo(AccessToken.getCurrentAccessToken());
                                user.setEmail(tempInfo[0]);
                                user.put("name", tempInfo[1]);
                                user.save();
                            }
                            catch(Exception e)
                            {
                                e.getMessage();
                                e.printStackTrace();
                            }


                        }

                        ParseUser current = ParseUser.getCurrentUser();
                        SessionManager.getInstance(context).setUserEmail(current.getEmail());
                        SessionManager.getInstance().setUserName(current.get("name").toString());
                        SessionManager.getInstance().setUserPhoto(current.get("photo").toString());
                        SessionManager.getInstance().setUserId(current.getObjectId());

                        SessionManager.getInstance().setLogin(true);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent);

                    }
                });


            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {


                if (AppController.checkConn(LoginActivity.this.getApplication())) {
                    Intent i = new Intent(getApplicationContext(),
                            RegisterActivity.class);
                    startActivity(i);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "No connection to internet detected. Unfortunately it is impossible to login", Toast.LENGTH_LONG).show();
                }
            }
        });



    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    private void checkLogin(String email, String password) {

        try {
            ParseUser.logIn(email, password);
            Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();

            ParseUser current = ParseUser.getCurrentUser();
            if (current != null) {

                SessionManager.getInstance(context).setUserEmail(email);
                SessionManager.getInstance().setUserName(current.get("name").toString());
                //chwilowo wyłączone, pracuję nad tym
                //SessionManager.getInstance().setUserPhoto(current.get("photo").toString());
                SessionManager.getInstance().setUserId(current.getObjectId());
                SessionManager.getInstance().setLogin(true);
            }
        } catch (ParseException e) {
            if (e.getMessage().contains("invalid login parameters"))
                Toast.makeText(context, "Incorrect email or password", Toast.LENGTH_LONG).show();
            else
                e.printStackTrace();
        } finally {
            if (SessionManager.getInstance().isLoggedIn()) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        }

    }

    private String[] getFacebookUserInfo(AccessToken accessToken) throws IOException {


        final String result[] = new String[2];

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        try {
                            Log.e("EMAIL", object.getString("email"));
                            result[0] = object.getString("email");
                            result[1] = object.getString("name");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,email");
        request.setParameters(parameters);
        request.executeAndWait();

        return result;
    }

    /*
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

                        session.setUserId(uid);
                        session.setUserName(name);
                        session.setUserEmail(email);


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
*/
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