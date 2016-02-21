package zpi.squad.app.grouploc.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
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
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import zpi.squad.app.grouploc.R;

public class ResetPasswordActivity extends Activity implements AppCompatCallback {

    EditText emailInput;
    Button resetPasswordButton, backToLoginScreenButton;
    private AppCompatDelegate delegate;
    private boolean positiveValidate;
    private TextInputLayout inputLayoutEmail;
    private ProgressDialog progress;
    private boolean successReset = false;
    private boolean resetFacebookEmail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_reset_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reset_password);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        delegate.getSupportActionBar().setCustomView(R.layout.actionbar_reset_password);

        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_reset_email);
        emailInput = (EditText) findViewById(R.id.email_reset_password);
        emailInput.addTextChangedListener(new MyTextWatcher(emailInput));
        resetPasswordButton = (Button) findViewById(R.id.btnResetSend);
        backToLoginScreenButton = (Button) findViewById(R.id.btnBackToLoginScreen);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            /*Parse pozwala wysłać maila z resetem hasła jak ktoś zarejestrował się przez fejsa, ale ta zmiana,
                * pomimo tego, że piszą że zakończyła się sukcesem, nie pozwala zalogować się przy użyciu maila z fejsa,
                * ani nie przeszkadza znowu w logowaniu się fejsem, jak wcześniej
                * Dlatego sprawdzam tutaj, czy ktoś podaje naprawdę adres mailowy - nazwa usera z fejsa nie może mieć małpy,
                * więc takie sprawdzenie powinno wystarczyć*/

                final String enteredEmail = emailInput.getText().toString().trim();

                submitForm();
                if (positiveValidate) {
                    progress = ProgressDialog.show(ResetPasswordActivity.this, getString(R.string.pleaseWait), "Sending reset password request", true);

                    new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            // do the thing that takes a long time
                            ParseQuery.clearAllCachedResults();
                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("email", enteredEmail);

                            Object[] queryResult;

                            try {
                                queryResult = query.find().toArray().clone();

                                if (queryResult.length == 0) {
                                    successReset = false;
                                    Log.e("RESET", " " + queryResult.length);
                                } else if ((boolean) ((ParseUser) queryResult[0]).get("isFacebookAccount")) {
                                    successReset = false;
                                    resetFacebookEmail = true;
                                } else {
                                    ParseUser.requestPasswordResetInBackground(enteredEmail);
                                    successReset = true;

                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.e("PASS REMINDER", e.getLocalizedMessage());
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    if (successReset) {
                                        Toast.makeText(getApplicationContext(),
                                                "Password reset request sent to " + enteredEmail + ". Check your mailbox", Toast.LENGTH_LONG).show();
                                    } else if (resetFacebookEmail) {
                                        Toast.makeText(getApplicationContext(), "You should log in with 'Log in with facebook' button", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "No user registered with email: " + enteredEmail, Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        backToLoginScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    private void submitForm() {
        positiveValidate = false;

        if (!validateEmail()) {
            return;
        }
        else
            positiveValidate = true;
    }

    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            inputLayoutEmail.setError(getString(R.string.emailValidEmpty));
            requestFocus(emailInput);
            return false;
        } else if (!isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.emailValid));
            requestFocus(emailInput);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
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
                case R.id.email_reset_password:
                    validateEmail();
                    break;
            }
        }
    }
}
