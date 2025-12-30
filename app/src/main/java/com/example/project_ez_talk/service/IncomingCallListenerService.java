package com.example.project_ez_talk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project_ez_talk.model.CallData;
import com.example.project_ez_talk.ui.call.incoming.IncomingCallActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Service that listens for incoming calls and shows the incoming call activity
 *
 * ⚠️ CRITICAL: This service MUST run in the background to receive calls
 * Even when the main app is closed/backgrounded!
 */
public class IncomingCallListenerService extends Service {

    private static final String TAG = "IncomingCallListener";
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

    private DatabaseReference incomingCallsRef;
    private ChildEventListener childEventListener;
    private String currentUserId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🔔 ═══════════════════════════════════════");
        Log.d(TAG, "🔔 IncomingCallListenerService STARTED");
        Log.d(TAG, "🔔 ═══════════════════════════════════════");

        // Get current user
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "✅ User authenticated: " + currentUserId);
            startListeningForIncomingCalls();
        } else {
            Log.w(TAG, "⚠️ User not authenticated - Service cannot listen for calls");
            stopSelf();
        }

        // START_STICKY ensures service restarts if killed
        return START_STICKY;
    }

    /**
     * Start listening for incoming calls
     */
    private void startListeningForIncomingCalls() {
        try {
            incomingCallsRef = FirebaseDatabase.getInstance(DATABASE_URL)
                    .getReference("incoming_calls/" + currentUserId + "/calls");

            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // ✅ NEW CALL RECEIVED
                    Log.d(TAG, "🔔 ═══════════════════════════════════════");
                    Log.d(TAG, "🔔 INCOMING CALL DETECTED!");
                    Log.d(TAG, "🔔 Call ID: " + snapshot.getKey());
                    Log.d(TAG, "🔔 ═══════════════════════════════════════");

                    try {
                        // Extract call data
                        String callerId = snapshot.child("callerId").getValue(String.class);
                        String callerName = snapshot.child("callerName").getValue(String.class);
                        String callerAvatar = snapshot.child("callerAvatar").getValue(String.class);
                        String callType = snapshot.child("callType").getValue(String.class);
                        String status = snapshot.child("status").getValue(String.class);

                        Log.d(TAG, "Caller: " + callerName + " (" + callerId + ")");
                        Log.d(TAG, "Call Type: " + callType);
                        Log.d(TAG, "Status: " + status);

                        // Show incoming call IMMEDIATELY
                        // Status might not be set yet, so show for any new call
                        if (callerId != null && callerName != null) {
                            showIncomingCallActivity(callerId, callerName, callerAvatar, callType);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing call data: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // Call status changed (accepted/rejected/ended)
                    String status = snapshot.child("status").getValue(String.class);
                    Log.d(TAG, "📞 Call status changed: " + status);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    // Call ended or was removed
                    Log.d(TAG, "📞 Call removed/ended: " + snapshot.getKey());
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "❌ Failed to listen for incoming calls: " + error.getMessage());
                }
            };

            incomingCallsRef.addChildEventListener(childEventListener);
            Log.d(TAG, "✅ Started listening for incoming calls");
            Log.d(TAG, "✅ Path: incoming_calls/" + currentUserId + "/calls");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting listener: " + e.getMessage(), e);
        }
    }

    /**
     * Show incoming call activity - THIS IS CRITICAL!
     */
    private void showIncomingCallActivity(String callerId, String callerName, String callerAvatar, String callType) {
        Log.d(TAG, "🔔 ═══════════════════════════════════════");
        Log.d(TAG, "🔔 SHOWING INCOMING CALL ALERT");
        Log.d(TAG, "🔔 Caller: " + callerName);
        Log.d(TAG, "🔔 ═══════════════════════════════════════");

        try {
            Intent intent = new Intent(this, IncomingCallActivity.class);

            // CRITICAL FLAGS:
            // - NEW_TASK: Start in new task (required for Service)
            // - TOP_TASK: Bring to top
            // - BROUGHT_TO_FRONT: Actually show the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            intent.putExtra(IncomingCallActivity.EXTRA_CALLER_ID, callerId);
            intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, callerName);
            intent.putExtra(IncomingCallActivity.EXTRA_CALLER_AVATAR, callerAvatar);
            intent.putExtra(IncomingCallActivity.EXTRA_CALL_TYPE, callType);

            startActivity(intent);
            Log.d(TAG, "✅ Incoming call activity STARTED - User should see alert!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to start incoming call activity: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "⚠️ Service destroyed, removing listeners");

        if (incomingCallsRef != null && childEventListener != null) {
            incomingCallsRef.removeEventListener(childEventListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}