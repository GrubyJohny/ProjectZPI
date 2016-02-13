package zpi.squad.app.grouploc.activity;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import zpi.squad.app.grouploc.R;

public class ResetPasswordActivity extends Activity implements AppCompatCallback {

    EditText email;
    Button resetPasswordButton, backToLoginScreenButton;
    private AppCompatDelegate delegate;

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

        email = (EditText) findViewById(R.id.email_reset_password);
        resetPasswordButton = (Button) findViewById(R.id.btnResetSend);
        backToLoginScreenButton = (Button) findViewById(R.id.btnBackToLoginScreen);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            /*Parse pozwala wysłać maila z resetem hasła jak ktoś zarejestrował się przez fejsa, ale ta zmiana,
                * pomimo tego, że piszą że zakończyła się sukcesem, nie pozwala zalogować się przy użyciu maila z fejsa,
                * ani nie przeszkadza znowu w logowaniu się fejsem, jak wcześniej
                * Dlatego sprawdzam tutaj, czy ktoś podaje naprawdę adres mailowy - nazwa usera z fejsa nie może mieć małpy,
                * więc takie sprawdzenie powinno wystarczyć*/

                String enteredEmail = email.getText().toString().trim();
                if(enteredEmail.length() > 0 && enteredEmail.contains("@"))
                {
                    ParseQuery.clearAllCachedResults();
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("email", enteredEmail);

                    Object[] queryResult;

                    try {
                        queryResult = query.find().toArray().clone();

                        if( queryResult.length == 0)
                        {
                            Toast.makeText(getApplicationContext(), "No user registered with email: " + enteredEmail, Toast.LENGTH_LONG).show();
                            Log.e("RESET", " " + queryResult.length);
                        }
                        else if((boolean) ((ParseUser) queryResult[0]).get("isFacebookAccount"))
                        {
                            Toast.makeText(getApplicationContext(), "You should log in with 'Log in with facebook' button", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            ParseUser.requestPasswordResetInBackground(enteredEmail);
                            Toast.makeText(getApplicationContext(),
                                    "Password reset request sent to " + enteredEmail + ". Check your mailbox", Toast.LENGTH_LONG).show();
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("PASS REMINDER", e.getLocalizedMessage());

                        if(e.getLocalizedMessage().contains("invalid email address"))
                            Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_LONG).show();
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "Please enter email first", Toast.LENGTH_LONG).show();
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
}
