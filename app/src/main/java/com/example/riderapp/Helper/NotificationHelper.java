package com.example.riderapp.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.riderapp.R;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URI;

public class NotificationHelper extends ContextWrapper {

    private static final String name = "Courier";
    private static final String id = "com.example.riderapp";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel(id,name,NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.GRAY);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager(){
        if(manager == null){
            manager= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String title, String content, PendingIntent pcontent, Uri muri){

        return new Notification.Builder(getApplicationContext(),id).setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(muri)
                .setContentIntent(pcontent)
                .setSmallIcon(R.drawable.ic_directions_car);
    }
}
