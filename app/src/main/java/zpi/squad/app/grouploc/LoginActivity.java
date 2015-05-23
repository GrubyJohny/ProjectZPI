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

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;


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
    public final String IMAGE_PHOTO_FILENAME = "profile_photo.png";
    private LoginButton loginButton;
    private String facebookUserId;




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

        //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));


        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setText("Register with Facebook");


        loginButton.setReadPermissions("public_profile");
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        facebookUserId = loginResult.getAccessToken().getUserId();


                        //Toast.makeText(LoginActivity.this.getBaseContext(),"facebook id: "+facebookUserId,Toast.LENGTH_SHORT).show();

                        URL imageURL = null;
                        try {
                            imageURL = new URL("https://graph.facebook.com/" + facebookUserId + "/picture?width=" + 200 + "&height=" + 200);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        InputStream inputStream = null;
                        try {
                            inputStream = (InputStream) imageURL.getContent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Bitmap photo = BitmapFactory.decodeStream(inputStream);
                        Bitmap bitmap_round = photo;

                        FileOutputStream fos = null;
                        try {
                            fos = context.openFileOutput(IMAGE_PHOTO_FILENAME, Context.MODE_PRIVATE);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bitmap_round.compress(Bitmap.CompressFormat.PNG, 100, fos);

                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });


    }

    /*
    public void getProfileInformation() {
        mAsyncRunner.request("me", new RequestListener() {
            @Override
            public void onComplete(String response, Object state) {
                Log.d("Profile", response);
                String json = response;
                try {
                    JSONObject profile = new JSONObject(json);
                    // getting name of the user
                    String name = profile.getString("name");
                    // getting email of the user
                    String email = profile.getString("email");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Name: " + name + "\nEmail: " + email, Toast.LENGTH_LONG).show();
                        }

                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e,
                                                Object state) {
            }

            @Override
            public void onMalformedURLException(MalformedURLException e,
                                                Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        });
    }
*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
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

                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
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
                Log.e(TAG, "Login Error: " + error.toString());

                Toast.makeText(getApplicationContext(),
                                                error.getMessage(), Toast.LENGTH_LONG).show();
                                hideDialog();;

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



}