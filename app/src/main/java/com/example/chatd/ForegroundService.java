package com.example.chatd;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
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

public class ForegroundService extends Service {
    public ForegroundService() {
        super();
    }

    public int warning = 0;
    private static String url = "https://war-api.ukrzen.in.ua/alerts/api/alerts/active.json";
    ArrayList<HashMap<String,String>> cities;
    private ProgressDialog progressDialog;
    ProgressDialog pd ;
    public String[] location;
    private TextView textView;
    SharedPreferences sp;
    private NotificationManager notificationManager;
    private static final int NOTIFY_ID = 101;
    private static final String CHANNEL_ID = "CHANNEL_ID";



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = sp.edit();

                while (true){

                    new JsonTask().execute(url);
                    Log.e("MyLog", "warning " + warning);
                    editor.putInt("warning", warning);
                    editor.apply();

                    try {
                        Thread.sleep(Long.parseLong("500"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        final String CHANNELID = "Warning";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Service")
                    .setContentTitle("Is working")
                    .setSmallIcon(R.drawable.ic_launcher_foreground);


            startForeground(1001, notification.build());

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class JsonTask extends AsyncTask<String, String, String> {



        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            int warning_cheking = 0;
            try{
                JSONObject jsonObject= new JSONObject(result);
                JSONArray city = jsonObject.getJSONArray("alerts");
                location = new String[city.length()];
                for(int i=0;i<city.length();i++){
                    JSONObject jsonObject1=city.getJSONObject(i);
                    String addloc =jsonObject1.getString("location_title");

                    if((addloc.equals("м.Полтава")) || (addloc.equals("Полтавська область")) || (addloc.equals("Полтавський район"))){
                        warning_cheking =1;
                        Log.e("MyLog", "warning_checker " + warning_cheking);
                    }




                }if(warning == 0 && warning_cheking == 1){

                    Log.e("MyLog", "Notif Send ");

                    createNotification1();





                }else if(warning == 1 && warning_cheking ==0){

                    createNotification2();


                } ;}catch(Exception e){}
            warning=warning_cheking;
        }


    }
    private void createNotification1(){
        Uri uri = Uri.parse("/raw/after.mp3");
        String id = "my_channel_id_01";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = notificationManager.getNotificationChannel(id);
            if (channel ==null){
                channel = new NotificationChannel(id, "Channel Title", NotificationManager.IMPORTANCE_HIGH);

                channel.setDescription("Description");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100,1000,200,300});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                notificationManager.createNotificationChannel(channel);
            }
        }
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0 , notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("ТРИВОГА")
                .setContentText("Повітряна тривога, усі в укриття!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100,1000,200,300})
                .setAutoCancel(false)
                .setSound(uri)
                ;
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
        m.notify(1,builder.build());
        final MediaPlayer mediaPlayer = MediaPlayer.create(this,R.raw.before);
        mediaPlayer.start();
    }

    private void createNotification2(){
        Uri uri = Uri.parse("/raw/after.mp3");
        String id = "my_channel_id_01";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = notificationManager.getNotificationChannel(id);
            if (channel ==null){
                channel = new NotificationChannel(id, "Channel Title", NotificationManager.IMPORTANCE_HIGH);

                channel.setDescription("Description");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100,1000,200,300});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                notificationManager.createNotificationChannel(channel);
            }
        }
        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0 , notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Відбій повітряної тривоги")
                .setContentText("Можна виходити з укриття")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100,1000,200,300})
                .setAutoCancel(false)
                .setSound(uri)
                ;
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
        m.notify(1,builder.build());
        final MediaPlayer mediaPlayer = MediaPlayer.create(this,R.raw.after);
        mediaPlayer.start();
    }
}


