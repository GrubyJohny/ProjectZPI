package zpi.squad.app.grouploc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
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
                                String[] tempInfo = getFacebookUserInfo(AccessToken.getCurrentAccessToken());

                                for(int i=0; i<tempInfo.length; i++)
                                    Log.e("Facebook row " + i, " is: "+tempInfo[i].toString());

                                user.setEmail(tempInfo[0]);
                                user.put("name", tempInfo[1]);
                                //tutaj trzeba wykombinować zdjęcie z fejsa...
                                //a jak się nie uda, to domyślne:
                                user.put("photo", SessionManager.getInstance(getApplicationContext()).encodeBitmapTobase64(BitmapFactory.decodeResource(getResources(), R.drawable.image5)) );
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
                SessionManager.getInstance().setUserPhoto(current.get("photo").toString());
                SessionManager.getInstance().setUserId(current.getObjectId());
                SessionManager.getInstance().setLogin(true);
            }
        } catch (ParseException e) {
            if (e.getMessage().contains("invalid login parameters"))
                Toast.makeText(context, "Incorrect email or password", Toast.LENGTH_LONG).show();
            else
            {
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

}