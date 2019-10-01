package com.example.riderapp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Helper.NotificationHelper;
import com.example.riderapp.Model.Token;
import com.example.riderapp.R;
import com.example.riderapp.UserInterface;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import static android.support.constraint.Constraints.TAG;

public class FirebaseServices extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {

        super.onNewToken(s);
        Log.d("Courier", "Refreshed token: " + s);
        updateTokenToServer(s);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

       if(remoteMessage.getNotification().getTitle().equals("Cancel")){
           Handler handler = new Handler(Looper.getMainLooper());
           handler.post(new Runnable() {
               @Override
               public void run() {
                   Toast.makeText(FirebaseServices.this,""+remoteMessage.getNotification().getBody(),Toast.LENGTH_SHORT).show();
               }
           });
       }else if(remoteMessage.getNotification().getTitle().equals("Arrived")){
           if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
               showArrivedNotificationApi26(remoteMessage.getNotification().getBody());
           else
               showArrivedNotification(remoteMessage.getNotification().getBody());
       }

    }

    private void showArrivedNotification(String body) {
        PendingIntent content = PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_roun)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(content);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationApi26(String body) {
        PendingIntent content = PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper helper = new NotificationHelper(getBaseContext());
        Notification.Builder bbuilder = helper.getNotification("Arrived",body,content,defaultSound);

        helper.getManager().notify(1,bbuilder.build());
    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db=  FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_table);

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
    }
}
