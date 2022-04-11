package com.example.chatd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class Settings extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    SwitchMaterial theme_switch;
    SharedPreferences sp;
    Button signOutBtn;
    private static final int SIGN_IN_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.settingsActivity);
        theme_switch = findViewById(R.id.theme_switch);
        signOutBtn = findViewById(R.id.sign_out_btn);

        sp= getApplicationContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean checked = sp.getBoolean("theme_check", false);
        if (checked){
            theme_switch.setChecked(true);
        }




        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected( MenuItem item) {
                switch (item.getItemId()){
                    case R.id.sirenActivity:
                        startActivity(new Intent(getApplicationContext(), Sirena.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.mainActivity:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settingsActivity:

                        return true;
                }
                return false;
            }
        });



        theme_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences.Editor editor = sp.edit();

                if (isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                editor.putBoolean("theme_check",isChecked);
                editor.apply();
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder().build(),
                        SIGN_IN_CODE);
            }
        });
    }
}