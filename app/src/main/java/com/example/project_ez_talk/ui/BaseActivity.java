package com.example.project_ez_talk.ui;

<<<<<<< HEAD
=======
import android.content.Context;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
/**
 * ✅ Base Activity class
 * Provides common functionality for all activities
 */
public class BaseActivity extends AppCompatActivity {

    protected static final String TAG = "BaseActivity";
=======
import com.example.project_ez_talk.helper.LocaleHelper;
import com.example.project_ez_talk.helper.NotificationHelper;

/**
 * BaseActivity - Base class for all activities
 * Automatically applies saved language preference
 * Automatically initializes FCM for push notifications
 *
 * All your activities should extend this instead of AppCompatActivity
 *
 * Example:
 * public class HomeActivity extends BaseActivity {
 *     // Your code here
 * }
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
        Log.d(TAG, "onCreate: " + this.getClass().getSimpleName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: " + this.getClass().getSimpleName());
=======

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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    @Override
    protected void onResume() {
        super.onResume();
<<<<<<< HEAD
        Log.d(TAG, "onResume: " + this.getClass().getSimpleName());
=======
        // Common resume logic
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    @Override
    protected void onPause() {
        super.onPause();
<<<<<<< HEAD
        Log.d(TAG, "onPause: " + this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: " + this.getClass().getSimpleName());
=======
        // Common pause logic
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
<<<<<<< HEAD
        Log.d(TAG, "onDestroy: " + this.getClass().getSimpleName());
=======
        // Common cleanup
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }
}