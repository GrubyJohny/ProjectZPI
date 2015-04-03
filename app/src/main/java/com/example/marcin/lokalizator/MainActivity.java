package com.example.marcin.lokalizator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.location.LocationManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    SessionManager session;
    ViewFlipper vieFli;
    private float lastX;
    private Spinner spinner1;
    private Button circleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        session = new SessionManager(this);


        vieFli = (ViewFlipper) findViewById(R.id.viewFlipper);
        vieFli.setHorizontalScrollBarEnabled(true);


        View vie = View.inflate(this, R.layout.activity_my_map, null);
        //View vie = View.inflate(this, R.layout.activity_login, null);
        vieFli.addView(vie);

        View vie2 = View.inflate(this, R.layout.activity_friends, null);
        vieFli.addView(vie2);

        //View vie3 = View.inflate(this, R.layout.activity_login, null);
        //View vie = View.inflate(this, R.layout.activity_login, null);
        //vieFli.addView(vie3);

        spinner1 = (Spinner) findViewById(R.id.spinner);
        String[] spinnerOptions = {"Settings", "Log out"};
        ArrayAdapter<String> circleButtonOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptions);
        spinner1.setAdapter(circleButtonOptions);

        circleButton = (Button) findViewById(R.id.circleButton);

        addListenerOnButton();
        addListenerOnSpinner();

    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut(AdapterView.OnItemSelectedListener view) {
        session.setLogin(false);
        Intent closeIntent = new Intent(this, LoginActivity.class);
        startActivity(closeIntent);
    }

    // Method to handle touch event like left to right swap and right to left swap
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN: {
                lastX = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = touchevent.getX();

                // if left to right swipe on screen
                if (lastX < currentX) {
                    // If no more View/Child to flip
                    if (vieFli.getDisplayedChild() == 0)
                        break;

                    vieFli.setInAnimation(this, R.anim.slide_in_from_left);
                    vieFli.setOutAnimation(this, R.anim.slide_out_to_right);

                    vieFli.showNext();
                }

                // if right to left swipe on screen
                if (lastX > currentX) {
                    if (vieFli.getDisplayedChild() == 1)
                        break;

                    vieFli.setInAnimation(this, R.anim.slide_in_from_right);
                    vieFli.setOutAnimation(this, R.anim.slide_out_to_left);

                    vieFli.showPrevious();
                }
                break;
            }
        }
        return false;
    }

    public void openMap(View v) {
        Intent i = new Intent(getApplicationContext(),
                MyMapActivity.class);
        startActivity(i);
        finish();
    }

    public void addListenerOnButton() {

        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner1.performClick();
            }
        });

    }

    public void addListenerOnSpinner(){
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch((int)position)
                {
                    case 0:
                        //USTAWIENIA TO DO
                        break;
                    case 1:
                        logOut(this);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
