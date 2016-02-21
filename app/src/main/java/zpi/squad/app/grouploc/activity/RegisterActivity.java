package zpi.squad.app.grouploc.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.Date;

import zpi.squad.app.grouploc.MainActivity;
import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.config.AppConfig;

public class RegisterActivity extends Activity implements AppCompatCallback {

    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private SessionManager session;
    private AppCompatDelegate delegate;
    private TextInputLayout inputLayoutName;
    private TextInputLayout inputLayoutPassword;
    private TextInputLayout inputLayoutEmail;
    private boolean positiveValidate;
    private ProgressDialog progressDialog;
    boolean registrationSuccessfully = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_register);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        delegate.getSupportActionBar().setCustomView(R.layout.actionbar_register);

        session = SessionManager.getInstance(getApplicationContext());
        if (session.isLoggedIn()) {
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_register_fullname);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_register_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_register_password);

        inputFullName = (EditText) findViewById(R.id.registerName);
        inputEmail = (EditText) findViewById(R.id.registerEmail);
        inputPassword = (EditText) findViewById(R.id.registerPassword);

        inputFullName.addTextChangedListener(new MyTextWatcher(inputFullName));
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        setUpButtons();
    }

    private void setUpButtons() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final String name = inputFullName.getText().toString().trim();
                final String email = inputEmail.getText().toString().trim();
                final String password = inputPassword.getText().toString();

                submitForm();
                if (positiveValidate) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    progressDialog = ProgressDialog.show(RegisterActivity.this, getString(R.string.pleaseWait), "Try to register new user", true);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // do the thing that takes a long time
                            try {
                                Parse.initialize(getApplicationContext(), AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);
                            } catch (Exception e) {
                                e.getLocalizedMessage();
                                e.printStackTrace();
                            }
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();

                            ParseUser user = new ParseUser();
                            user.setUsername(email);
                            user.setEmail(email);
                            user.setPassword(password);
                            user.put("isFacebookAccount", false);
                            user.put("locationUpdateTime", new Date());
                            //TO DO:
                            //jeszcze muszę przemyśleć jak tą lokalizację ustawiać przy rejestracji1

                            user.put("location", new ParseGeoPoint(50, 18));
                            user.put("name", name);
                            user.put("name_lowercase", name.toLowerCase());
                            user.put("photo", session.encodeBitmapTobase64(BitmapFactory.decodeResource(getResources(), R.drawable.image5)));

                            try {
                                user.signUp();
                                registrationSuccessfully = true;
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                if (e.getMessage().contains("already been taken")) {
                                    registrationSuccessfully = false;
                                } else if (e.getMessage().contains("invalid email address")) {
                                    Toast.makeText(getApplicationContext(), "Invalid email address!", Toast.LENGTH_LONG).show();
                                } else {
                                    e.printStackTrace();
                                }
                            } finally {
                                if (registrationSuccessfully) {
                                    finish();
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (registrationSuccessfully) {
                                        Toast.makeText(getApplicationContext(), "Registration successfully!", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Email already in use, please log in or use other email", Toast.LENGTH_LONG).show();
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
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

        if (!validateName()) {
            return;
        } else if (!validateEmail()) {
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
        int minimumLength = 6;

        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.passwordValidEmpty));
            requestFocus(inputPassword);
            return false;
        } else if (inputPassword.getText().toString().trim().length() < minimumLength) {
            inputLayoutPassword.setError(getString(R.string.passwordValidMoreThan));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateName() {
        int minimumLength = 2;

        if (inputFullName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.nameValidEmpty));
            requestFocus(inputFullName);
            return false;
        } else if (inputFullName.getText().toString().trim().length() < minimumLength) {
            inputLayoutName.setError(getString(R.string.nameValidMoreThan));
            requestFocus(inputFullName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
                case R.id.registerName:
                    validateName();
                    break;
                case R.id.registerEmail:
                    validateEmail();
                    break;
                case R.id.registerPassword:
                    validatePassword();
                    break;
            }
        }
    }
}