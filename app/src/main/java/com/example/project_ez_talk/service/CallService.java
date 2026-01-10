package com.example.project_ez_talk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "✅ CallService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🎤 CallService started");

        // Keep service running even if killed by system
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Not a bound service
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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