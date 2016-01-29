package zpi.squad.app.grouploc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64InputStream;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
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
    private Bitmap profileImageFromFacebook;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        context = getApplicationContext();
        final SessionManager session = SessionManager.getInstance(context);
        if (SessionManager.getInstance(context).isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }

        //tę linijkę oczywiście trzeba później wyrzucić
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Parse.initialize(this, AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);
        } catch (Exception e) {
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

                if(!email.isEmpty() && password.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please enter password!", Toast.LENGTH_LONG).show();
                }
                else if(email.isEmpty() && !password.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please enter your email address", Toast.LENGTH_LONG).show();
                }
                else if(email.isEmpty() && password.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG).show();
                }
                else {
                    if (AppController.checkConn(LoginActivity.this.getApplication())) {
                        checkLogin(email, password);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No connection to internet detected. Unfortunately it's is impossible to login", Toast.LENGTH_LONG).show();
                    }

                }
            }

        });


        permissions.add("public_profile");
        permissions.add("email");

        btnLoginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ParseFacebookUtils.initialize(context);
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(final ParseUser user, ParseException err) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        } else if (user.isNew()) {
                            try {
                                String[] tempInfo = getFacebookUserInfo(AccessToken.getCurrentAccessToken());
                                user.setEmail(tempInfo[0]);
                                user.put("name", tempInfo[1]);
                                try {
                                    user.put("photo", session.encodeBitmapTobase64(getFacebookProfilePicture(AccessToken.getCurrentAccessToken())));
                                } catch (Exception e) {
                                    e.getLocalizedMessage();
                                    e.printStackTrace();
                                    user.put("photo", session.encodeBitmapTobase64(BitmapFactory.decodeResource(getResources(), R.drawable.image5)));
                                }

                                user.save();
                            } catch (Exception e) {
                                e.getLocalizedMessage();
                                e.printStackTrace();
                            }
                        }

                        ParseUser current = ParseUser.getCurrentUser();


                        try {
                            session.setUserEmail(current.fetchIfNeeded().getEmail());
                            session.setUserName(current.fetchIfNeeded().get("name").toString());
                            session.setUserPhoto(current.fetchIfNeeded().get("photo").toString());
                            session.setUserId(current.fetchIfNeeded().getObjectId());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        session.setLogin(true);
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
                SessionManager.getInstance().setUserPhoto(current.get("photo").toString());
                SessionManager.getInstance().setUserId(current.getObjectId());
                SessionManager.getInstance().setLogin(true);
            }
        } catch (ParseException e) {
            if (e.getMessage().contains("invalid login parameters"))
                Toast.makeText(context, "Incorrect email or password", Toast.LENGTH_LONG).show();
            else {
                e.getLocalizedMessage();
                e.printStackTrace();
            }
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
                        try {
                            result[0] = object.getString("email");
                            result[1] = object.getString("name");
                        } catch (JSONException e) {
                            e.getLocalizedMessage();
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

    private Bitmap getFacebookProfilePicture(AccessToken accessToken) throws IOException {


        Bundle params = new Bundle();
        params.putBoolean("redirect", false);
        params.putInt("height", 100);
        params.putInt("width", 100);
        GraphResponse srequest = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + accessToken.getUserId() + "/picture",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        //String ara = response.getJSONObject().toString();
                        //Log.e("PROFILOWE: ", ara);
                        try {
                            JSONObject araa = response.getJSONObject();
                            JSONObject aray = araa.getJSONObject("data");

                            URL facebookProfilePictureUrl = new URL(aray.getString("url"));
                            profileImageFromFacebook = BitmapFactory.decodeStream(facebookProfilePictureUrl.openConnection().getInputStream());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
        ).executeAndWait();

        return profileImageFromFacebook;
    }
}