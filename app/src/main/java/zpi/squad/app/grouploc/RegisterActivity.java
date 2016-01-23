package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {

    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        session = SessionManager.getInstance(getApplicationContext());
        if (session.isLoggedIn()) {
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString();
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                boolean registrationSuccessfully = false;

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    showDialog();
                    try {
                        Parse.initialize(getApplicationContext(), AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);

                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();

                    ParseUser user = new ParseUser();
                    user.setUsername(email);
                    user.setEmail(email);
                    user.setPassword(password);
                    user.put("name", name);

                    try {
                        user.signUp();
                        registrationSuccessfully = true;
                    } catch (Exception e) {

                        if (e.getMessage().contains("already been taken"))
                            Toast.makeText(getApplicationContext(), "Email already in use, please log in or use other email", Toast.LENGTH_LONG).show();
                        else if (e.getMessage().contains("invalid email address"))
                            Toast.makeText(getApplicationContext(), "Invalid email address!", Toast.LENGTH_LONG).show();
                        else
                            e.printStackTrace();
                    } finally {
                        hideDialog();
                        if (registrationSuccessfully)
                            Toast.makeText(getApplicationContext(), "Registration successfully!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);

                    }


                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });


        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}