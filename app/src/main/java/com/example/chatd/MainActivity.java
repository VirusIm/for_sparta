package com.example.chatd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.text.format.DateFormat;

import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_CODE = 1;
    private RelativeLayout activity_main;
    private DatabaseReference mDatabase;
    private FirebaseListAdapter<Message> adapter;

    SharedPreferences sp;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SIGN_IN_CODE){
            if (resultCode == RESULT_OK){
               // Snackbar.make(activity_main, "Ви авторизовані",Snackbar.LENGTH_SHORT).show();
                displayAllMessages();
            }
            else {
                Snackbar.make(activity_main, "Ви не авторизовані",Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sp = getApplicationContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isChecked = sp.getBoolean("theme_check",false);
        if (isChecked){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance("https://chatd-4a9d0-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        activity_main = findViewById(R.id.activity_main);
        FloatingActionButton sendBtn = findViewById(R.id.btnSend);

        if (!foregroundServiceRunning()){
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            startForegroundService(serviceIntent);
        }

        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.mainActivity);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected( MenuItem item) {
                switch (item.getItemId()){
                    case R.id.sirenActivity:
                        startActivity(new Intent(getApplicationContext(),Sirena.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.mainActivity:
                        return true;
                    case R.id.settingsActivity:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        sendBtn.setOnClickListener(view -> {
            EditText textField = findViewById(R.id.messageField);
            if (textField.getText().toString().equals(""))
                return;

            mDatabase.push().setValue(
                    new Message(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),textField.getText().toString())
            );
            textField.setText("");
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().build(),
                    SIGN_IN_CODE);
        }
        else{
          //  Snackbar.make(activity_main, "Ви авторизовані",Snackbar.LENGTH_SHORT).show();
            displayAllMessages();
        }
    }

    private void displayAllMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        FirebaseListOptions.Builder<Message> builder = new FirebaseListOptions.Builder<>();

        builder.setLayout(R.layout.list_item).setQuery(FirebaseDatabase.getInstance("https://chatd-4a9d0-default-rtdb.europe-west1.firebasedatabase.app/").getReference(), Message.class).setLifecycleOwner(this);
        adapter = new FirebaseListAdapter<Message>(builder.build()) {
            @Override
            protected void populateView(View v, Message model, int position) {

                TextView mess_user, mess_time;
                BubbleTextView mess_text;
                mess_user = v.findViewById(R.id.message_user);
                mess_time = v.findViewById(R.id.message_time);
                mess_text = v.findViewById(R.id.message_text);


                mess_user.setText(model.getUserName());
                mess_time.setText(DateFormat.format("dd-MM-yyyy HH:mm", model.getMessageTime()));

                mess_text.setText(model.getTextMessage());

            }
        };
        listOfMessages.setAdapter(adapter);
    }

    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(ForegroundService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
}