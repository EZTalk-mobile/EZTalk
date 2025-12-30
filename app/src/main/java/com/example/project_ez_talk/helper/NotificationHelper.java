package com.example.project_ez_talk.helper;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    /**
     * Initialize FCM and get device token
     * Call this in BaseActivity.onCreate() or MainActivity.onCreate()
     */
    public static void initializeFCM(Context context) {
        Log.d(TAG, "🔄 Initializing FCM...");

        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();

            // Only initialize if user is logged in
            if (auth.getCurrentUser() == null) {
                Log.d(TAG, "⚠️ User not logged in, skipping FCM initialization");
                return;
            }

            // Get FCM token
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> {
                        Log.d(TAG, "✅ FCM Token obtained: " + token);
                        saveFCMTokenToFirestore(context, token);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to get FCM token: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "❌ FCM initialization error: " + e.getMessage());
        }
    }

    /**
     * Save FCM token to Firestore user document
     * This token will be used to send push notifications
     */
    public static void saveFCMTokenToFirestore(Context context, String token) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() == null) {
                Log.d(TAG, "⚠️ User not logged in, cannot save FCM token");
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Update user document with FCM token
            db.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ FCM token saved to Firestore: " + token.substring(0, 20) + "...");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to save FCM token: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving FCM token: " + e.getMessage());
        }
    }

    /**
     * Request notification permission for Android 13+
     * Call this in BaseActivity.onCreate()
     */
    public static void requestNotificationPermission(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            try {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        == android.content.pm.PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "⚠️ POST_NOTIFICATIONS permission not granted on Android 13+");
                    // In production, you may want to request this permission
                } else {
                    Log.d(TAG, "✅ POST_NOTIFICATIONS permission granted");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking notification permission: " + e.getMessage());
            }
        }
    }

    /**
     * Clear FCM token when user logs out
     */
    public static void clearFCMToken(Context context) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() == null) {
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Delete FCM token from Firestore
            db.collection("users")
                    .document(userId)
                    .update("fcmToken", com.google.firebase.firestore.FieldValue.delete())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ FCM token cleared from Firestore");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to clear FCM token: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error clearing FCM token: " + e.getMessage());
        }
    }

    /**
     * Get current device FCM token (for debugging)
     */
    public static void getAndLogFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d(TAG, "📱 Current FCM Token: " + token);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get token: " + e.getMessage());
                });
    }
}