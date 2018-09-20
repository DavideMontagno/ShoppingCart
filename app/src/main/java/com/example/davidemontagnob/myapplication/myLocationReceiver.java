package com.example.davidemontagnob.myapplication;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class myLocationReceiver extends BroadcastReceiver  {


    public static final String CHANNEL_2 = "channel2";


    @Override
    public void onReceive(Context context, Intent intent2) {

        final String key = LocationManager.KEY_PROXIMITY_ENTERING;
        final Boolean entering = intent2.getBooleanExtra(key, false);

        if (entering) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel1 = new NotificationChannel(
                        CHANNEL_2,
                        "channel2",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel1.setDescription("App - Channel1");
                channel1.enableVibration(true);

                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel1);
            }
            Intent intent = new Intent(context, MapsActivity.class);
            intent.setAction(Long.toString(System.currentTimeMillis()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_2);
            notification.setSmallIcon(R.drawable.icon_app)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Ehilà!")
                    .setContentText("Quì vicino puoi trovare quello che ti manca")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            notification.setAutoCancel(true);
            notificationManager.notify(2, notification.build());
        }

    }



}