package com.example.chatd;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Sirena extends AppCompatActivity {

    public TextView textView;
    private ImageView imgView;

    BottomNavigationView bottomNavigationView;
    TextView txtJson;

    SharedPreferences sp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sirena);

        txtJson = findViewById(R.id.siren_status);
        imgView = findViewById(R.id.status_image);
//        new JsonTask().execute(url);
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.sirenActivity);

        sp= getApplicationContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        //String sStatus = sp.getString("ServiceStatus", "");
        //txtJson.setText(sStatus);
        int warning = sp.getInt("warning",0);
        if (warning==1){
            txtJson.setText("Повітряна тривога!");
            imgView.setImageResource(R.drawable.ic_warning);
        }
        else{
            txtJson.setText("Тривоги немає");
            imgView.setImageResource(R.drawable.ico_allright);
        }



        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected( MenuItem item) {
                switch (item.getItemId()){
                    case R.id.sirenActivity:
                        return true;
                    case R.id.mainActivity:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settingsActivity:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }


}

