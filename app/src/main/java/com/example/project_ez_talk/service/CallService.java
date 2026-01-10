package com.example.project_ez_talk.service;

<<<<<<< HEAD
import android.app.Service;
import android.content.Intent;
=======
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
<<<<<<< HEAD

/**
 * CallService - Background service for handling calls
 *
 * Keeps calls running even when app is minimized
 * Shows persistent notification during calls
 * Manages audio focus and call lifecycle
 *
 * For now: Empty stub - works fine for testing
 * Later: Add persistent notification, audio focus, etc.
 */
public class CallService extends Service {

    private static final String TAG = "CallService";
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    @Override
    public void onCreate() {
        super.onCreate();
<<<<<<< HEAD
        Log.d(TAG, "✅ CallService created");
=======
        Log.d(TAG, "CallService created");
        createNotificationChannel();
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
<<<<<<< HEAD
        Log.d(TAG, "🎤 CallService started");

        // Keep service running even if killed by system
        return START_STICKY;
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
<<<<<<< HEAD
        // Not a bound service
        return null;
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
<<<<<<< HEAD
        Log.d(TAG, "📞 CallService destroyed");
    }

    /**
     * TODO: For production, implement:
     *
     * 1. Create persistent notification:
     *    - Channel: "ongoing_call"
     *    - Title: "You're in a call"
     *    - Action: Quick return to call
     *
     * 2. Start foreground service:
     *    startForeground(NOTIFICATION_ID, notification)
     *
     * 3. Handle audio focus:
     *    - Request audio focus
     *    - Handle focus loss
     *
     * 4. Manage call state:
     *    - Listen to call events
     *    - Show/hide notification
     *
     * 5. Prevent system kill:
     *    - Use WakeLock
     *    - Return START_STICKY
     *
     * Example for later:
     *
     * private Notification createCallNotification() {
     *     return new NotificationCompat.Builder(this, "ongoing_call")
     *         .setContentTitle("You're in a call")
     *         .setSmallIcon(R.drawable.ic_call)
     *         .setCategory(Notification.CATEGORY_CALL)
     *         .setOngoing(true)
     *         .build();
     * }
     */
}
=======
        Log.d(TAG, "CallService destroyed");
        isCallActive = false;
    }
}
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
