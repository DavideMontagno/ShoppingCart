package com.example.davidemontagnob.myapplication;


import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.davidemontagnob.myapplication.R.layout.main_activity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public Database_helper myDB;
    private boolean isOpen = false;
    private FloatingActionButton main_f_b;
    private FloatingActionButton second_f_b;
    private FloatingActionButton third_f_b;
    private Animation FabOpen, FabClose, FabRotate, FabNRotate;

    private android.support.v7.widget.Toolbar toolbar;


    /* PREFERENCE*/
    private SharedPreferences prefs;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isOpened", isOpen);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isOpen = savedInstanceState.getBoolean("isOpened");
    }

    private boolean isOpened() {
        if (isOpen) return true;
        else return false;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(main_activity);
        myDB = new Database_helper(this);
        toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        main_f_b = (FloatingActionButton) findViewById(R.id.button_floating_a_1);
        second_f_b = (FloatingActionButton) findViewById(R.id.button_floating);
        third_f_b = (FloatingActionButton) findViewById(R.id.button_floating_a_2);
        main_f_b.setOnClickListener(this);
        second_f_b.setOnClickListener(this);
        third_f_b.setOnClickListener(this);
        second_f_b.setVisibility(View.INVISIBLE);
        third_f_b.setVisibility(View.INVISIBLE);

        FabOpen = AnimationUtils.loadAnimation((getApplicationContext()), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation((getApplicationContext()), R.anim.fab_close);
        FabRotate = AnimationUtils.loadAnimation((getApplicationContext()), R.anim.fab_rotate);
        FabNRotate = AnimationUtils.loadAnimation((getApplicationContext()), R.anim.fab_nrotate);
        /*Gestire le preferenze!*/
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        startService(new Intent(this, MyService.class));
    }


    //qui per impostare le preferenze
    public void onResume() {
        super.onResume();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.background_main);
        TextView txt = findViewById(R.id.m_t_2); //NOME NEL MENU
        String sSetting = prefs.getString("username", "").toString();
        txt.setText(sSetting);
        myDB.updateData();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }



    }

    private void functionCloseAnim() {
        second_f_b.startAnimation(FabClose);
        third_f_b.startAnimation(FabClose);
        main_f_b.startAnimation(FabNRotate);
        second_f_b.setVisibility(View.INVISIBLE);
        third_f_b.setVisibility(View.INVISIBLE);
        second_f_b.setClickable(false);
        third_f_b.setClickable(false);
        setOpen(false);

    }

    private void functionOpenAnim() {
        second_f_b.startAnimation(FabOpen);
        third_f_b.startAnimation(FabOpen);
        main_f_b.startAnimation(FabRotate);
        second_f_b.setVisibility(View.VISIBLE);
        third_f_b.setVisibility(View.VISIBLE);
        second_f_b.setClickable(true);
        third_f_b.setClickable(true);
        setOpen(true);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_floating_a_1:
                if (isOpened()) {
                    try {
                        // code runs in a thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                functionCloseAnim();

                            }
                        });
                    } catch (final Exception ex) {

                    }

                } else {
                    try {
                        // code runs in a thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                functionOpenAnim();

                            }
                        });
                    } catch (final Exception ex) {

                    }

                }
                break;

            case R.id.button_floating:
                Intent intent = new Intent(getApplicationContext(), Database_display_info.class);
                startActivity(intent);
                break;

            case R.id.button_floating_a_2:
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Bundle tmp = new Bundle();

                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Posizione disattivata");
                    alert.setMessage("Per poter utilizzare la mappa correttamente assicurati di aver attivato la posizione!");
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent2 = new Intent(getApplicationContext(), MapsActivity.class);

                            startActivity(intent2);
                        }
                    });
                    alert.create().show();
                } else {
                    final Intent intent2 = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent2);
                }
                break;

        }
    }


    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu2, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int res_id = item.getItemId();
        if (res_id == R.id.action_share) {
            if (isOpened()) functionCloseAnim();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Prova questas nuova app gratuita\nGrazie per averci donato 10 euro!");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Condividi usando"));
        } else if (res_id == R.id.action_about) {
            if (isOpened()) {
                try {
                    // code runs in a thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            functionCloseAnim();

                        }
                    });
                } catch (final Exception ex) {

                }
            }
            Toast.makeText(MainActivity.this, "Clicked: About us", Toast.LENGTH_SHORT).show();
        } else if (res_id == R.id.action_setting) {
            Intent intent3 = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent3);
        }
        return true;
    }






    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
