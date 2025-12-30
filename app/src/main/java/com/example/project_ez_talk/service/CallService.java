package com.example.project_ez_talk.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.call.video.VideoCallActivity;

public class CallService extends Service {

    private static final String TAG = "CallService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "call_service_channel";
    private static final String CHANNEL_NAME = "Call Service";

    private final IBinder binder = new LocalBinder();
    private boolean isCallActive = false;
    private String callerName = "Unknown";

    public class LocalBinder extends Binder {
        public CallService getService() {
            return CallService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CallService created");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CallService started");
        
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START_CALL":
                        callerName = intent.getStringExtra("caller_name");
                        startForeground(NOTIFICATION_ID, createNotification());
                        isCallActive = true;
                        break;
                    case "END_CALL":
                        endCall();
                        break;
                }
            }
        }
        
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, VideoCallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ongoing Call")
                .setContentText("Call with " + callerName)
                .setSmallIcon(R.drawable.ic_call)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void endCall() {
        isCallActive = false;
        stopForeground(true);
        stopSelf();
    }

    public boolean isCallActive() {
        return isCallActive;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CallService destroyed");
        isCallActive = false;
    }
}
