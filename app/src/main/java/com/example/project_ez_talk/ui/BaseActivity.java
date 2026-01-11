package com.example.project_ez_talk.ui;

import android.content.Context;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_ez_talk.helper.LocaleHelper;
import com.example.project_ez_talk.helper.NotificationHelper;

/**
 * ✅ Base Activity class
 * Provides common functionality for all activities
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ==================== FCM INITIALIZATION ====================
        // Initialize Firebase Cloud Messaging
        // This will:
        // 1. Get device FCM token
        // 2. Save token to Firestore (for push notifications)
        // 3. Request notification permission (Android 13+)
        Log.d(TAG, "🔄 Initializing FCM for push notifications...");
        NotificationHelper.initializeFCM(this);
        NotificationHelper.requestNotificationPermission(this);
        // ==================== END FCM INITIALIZATION ====================

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Common resume logic

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: " + this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: " + this.getClass().getSimpleName());
        // Common pause logic

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Common cleanup

    }
}