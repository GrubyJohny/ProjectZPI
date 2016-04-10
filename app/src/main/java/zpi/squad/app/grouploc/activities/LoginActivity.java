package zpi.squad.app.grouploc.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import zpi.squad.app.grouploc.AppController;
import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class LoginActivity extends Activity implements AppCompatCallback {
    public static Context context;
    public static List<String> permissions = new ArrayList<>();
    private Button btnLogin, btnLoginWithFacebook, btnRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private TextView btnRemind;
    private Bitmap profileImageFromFacebook;
    private SessionManager session;
    private AppCompatDelegate delegate;
    private boolean positiveValidate;
    private ProgressDialog progressLogin;
    private ProgressDialog progressFacebookLogin;
    private boolean successLogin = false;
    private CommonMethods commonMethods;
    private ParseUser current;
    private LogInCallback facebookLoginCallback;

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_login);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        delegate.getSupportActionBar().setCustomView(R.layout.actionbar_login);

        context = getApplicationContext();
        session = SessionManager.getInstance(context);
        commonMethods = CommonMethods.getInstance(context);

        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            session.requestLocationUpdate = true;
            startActivity(intent);
        }

        //tę linijkę oczywiście trzeba później wyrzucić
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_login_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_login_password);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLoginWithFacebook = (Button) findViewById(R.id.login_button_facebook);
        btnRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnRemind = (TextView) findViewById(R.id.btnRemindPassword);



        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString();

                submitForm();
                if (positiveValidate) {
                    if (AppController.checkConn(LoginActivity.this.getApplication())) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

        facebookLoginCallback = new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                boolean rollbackIntentStarted = false;

                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                    rollbackIntentStarted = true;
                    finish();
                    startActivity(i);
                } else if (user.isNew()) {
                    try {
                        String[] tempInfo = getFacebookUserInfo(AccessToken.getCurrentAccessToken());
                        user.setEmail(tempInfo[0]);

                        user.put("name", tempInfo[1]);
                        user.put("name_lowercase", tempInfo[1].toLowerCase());
                        user.put("location", new ParseGeoPoint(55, 55));
                        user.put("isFacebookAccount", true);
                        try {
                            user.put("photo", commonMethods.encodeBitmapTobase64(getFacebookProfilePicture(AccessToken.getCurrentAccessToken())));
                        } catch (Exception e) {
                            e.getLocalizedMessage();
                            e.printStackTrace();
                            user.put("photo", commonMethods.encodeBitmapTobase64(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar)));
                        }

                        user.save();

                    } catch (Exception e) {
                        e.getLocalizedMessage();
                        e.printStackTrace();
                    }
                }


                if (user == null) {
                    if (!rollbackIntentStarted) {
                        Log.e("A MOŻE", " JEDNAK");
                        i = new Intent(getApplicationContext(),
                                LoginActivity.class);
                        finish();
                        startActivity(i);
                    }
                } else {

                    try {

                        //jedna kurwa, jebana linijka, pezez którą pieprzyłem się z tym 3 dni
                        try {
                            user.save();
                        } catch (Exception e) {

                            e.getLocalizedMessage();
                            e.printStackTrace();
                            if (e.getLocalizedMessage().contains("has already been taken")) {
                                /*
                                *
                                * TUTAJ BĘDZIE OBSŁUGA SYTUACJI, KIEDY NAJPIERW ZAREJESTRUJESZ SIĘ NORMALNIE, A PÓŹNIEJ
                                * CHCESZ ZALOGOWAĆ SIĘ PRZEZ FEJSA. NIE WIEM JEDNAK CO POWINNO SIĘ WYDARZYĆ W TAKIEJ SYTUACJI
                                * #TAKASYTUACJA
                                *
                                * */
                                //user.pin();
                            }
                        }
                        //jak nie ma internetu, to tu wypieprza

                        current = ParseUser.getCurrentUser().fetchIfNeeded();

                        session.setUserEmail(current.getEmail());
                        session.setUserName(current.get("name").toString());
                        session.setUserPhoto(current.get("photo").toString());
                        session.setUserId(current.getObjectId());
                        session.setUserCurrentLocation(current.getParseGeoPoint("location").getLatitude(), current.getParseGeoPoint("location").getLongitude());
                        session.setUserIsLoggedByFacebook(true);

                        session.setLoggedIn(true);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    } catch (ParseException e) {
                        e.getLocalizedMessage();
                        e.printStackTrace();

                    }
                }

            }

        };

        btnLoginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    if (AppController.checkConn(LoginActivity.this.getApplication())) {

                        progressFacebookLogin = ProgressDialog.show(LoginActivity.this, getString(R.string.pleaseWait), "Logging with Facebook", true);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // do the thing that takes a long time

                                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, facebookLoginCallback);
                            }
                        }).start();

                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No connection to internet detected. Unfortunately it's is impossible to login", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception pe) {
                    pe.getLocalizedMessage();
                    pe.printStackTrace();

                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppController.checkConn(LoginActivity.this.getApplication())) {
                    Intent i = new Intent(getApplicationContext(),
                            RegisterActivity.class);
                    startActivity(i);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "No connection to internet detected. It is impossible to register", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppController.checkConn(LoginActivity.this.getApplication())) {
                    Intent i = new Intent(getApplicationContext(),
                            ResetPasswordActivity.class);
                    startActivity(i);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "No connection to internet detected. It is impossible to reset password", Toast.LENGTH_LONG).show();
                }
            }
        });

        ParseInstallation.getCurrentInstallation().deleteInBackground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressFacebookLogin != null && progressFacebookLogin.isShowing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressFacebookLogin.dismiss();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void checkLogin(final String email, final String password) {
        progressLogin = ProgressDialog.show(this, getString(R.string.pleaseWait), "Logging in", true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    try {
                        Parse.initialize(context, "rMAJUrbPT4fIVGk8ePC7gavmnY8NmmaxWv8Lf8y4", "NOKLzlyq0v5nj5js1ZoQbXPewym3MCSUCIlRudMy");
                    } catch (Exception e) {
                        e.getLocalizedMessage();
                    }
                    ParseUser.logIn(email, password);
                    successLogin = true;

                    ParseUser current = ParseUser.getCurrentUser().fetchIfNeeded();
                    if (current != null) {

                        session.setUserEmail(email);
                        session.setUserName(current.get("name").toString());
                        session.setUserPhoto(current.get("photo").toString());
                        session.setUserId(current.getObjectId());
                        //w następnym commicie będę to pobierał już na bieżąco
                        session.setUserCurrentLocation(current.getParseGeoPoint("location").getLatitude(), current.getParseGeoPoint("location").getLongitude());
                        session.setLoggedIn(true);
                    }
                } catch (ParseException e) {
                    if (e.getMessage().contains("invalid login parameters")) {
                        successLogin = false;
                    } else {
                        e.getLocalizedMessage();
                        e.printStackTrace();
                    }
                } finally {
                    if (session.isLoggedIn()) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (successLogin) {
                            Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "Incorrect email or password", Toast.LENGTH_LONG).show();
                        }
                        progressLogin.dismiss();
                    }
                });
            }
        }).start();
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

    @Override
    public void onSupportActionModeStarted(android.support.v7.view.ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(android.support.v7.view.ActionMode mode) {

    }

    @Nullable
    @Override
    public android.support.v7.view.ActionMode onWindowStartingSupportActionMode(android.support.v7.view.ActionMode.Callback callback) {
        return null;
    }

    private void submitForm() {
        positiveValidate = false;

        if (!validateEmail()) {
            return;
        } else if (!validatePassword()) {
            return;
        } else
            positiveValidate = true;
    }

    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty()) {
            inputLayoutEmail.setError(getString(R.string.emailValidEmpty));
            requestFocus(inputEmail);
            return false;
        } else if (!isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.emailValid));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.passwordValidEmpty));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.email:
                    validateEmail();
                    break;
                case R.id.password:
                    validatePassword();
                    break;
            }
        }
    }
}