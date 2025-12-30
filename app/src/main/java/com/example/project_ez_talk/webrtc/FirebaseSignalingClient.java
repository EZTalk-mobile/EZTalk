package com.example.project_ez_talk.webrtc;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class FirebaseSignalingClient {

    private static final String TAG = "FirebaseSignalingClient";

    // ⚠️ MUST MATCH YOUR DATABASE REGION
    private static final String DATABASE_URL =
            "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

    private static final String SIGNALING_NODE = "signaling";
    private static final String EVENTS_NODE = "events";

    private final Gson gson = new Gson();
    private final DatabaseReference rootRef;

    private String currentUserId;

    // ================= LISTENER =================
    public interface SignalingListener {
        void onNewEvent(DataModel model);
    }

    // ================= CONSTRUCTOR =================
    public FirebaseSignalingClient() {
        rootRef = FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference();
        Log.d(TAG, "✅ FirebaseSignalingClient initialized");
    }

    // ================= LOGIN =================
    public void login(String userId, Runnable onSuccess) {
        currentUserId = userId;

        Log.d(TAG, "🔐 Login signaling user: " + userId);

        // Ensure signaling node exists
        rootRef.child(SIGNALING_NODE)
                .child(userId)
                .child(EVENTS_NODE)
                .setValue(true)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "✅ Signaling ready for user: " + userId);
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "❌ Login failed", e)
                );
    }

    // ================= SEND MESSAGE =================
    public void sendMessageToOtherUser(DataModel model) {
        if (model == null || model.getTarget() == null) {
            Log.e(TAG, "❌ Invalid DataModel or target");
            return;
        }

        Log.d(TAG, "📤 Sending: " + model.getType()
                + " → " + model.getTarget());

        rootRef.child(SIGNALING_NODE)
                .child(model.getTarget())
                .child(EVENTS_NODE)
                .push() // 🔥 IMPORTANT: DO NOT OVERWRITE
                .setValue(gson.toJson(model))
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "✅ Signal sent")
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "❌ Send failed", e)
                );
    }

    // ================= LISTEN EVENTS =================
    public void observeIncomingEvents(SignalingListener listener) {
        if (currentUserId == null) {
            Log.e(TAG, "❌ observeIncomingEvents: user not logged in");
            return;
        }

        Log.d(TAG, "👂 Listening for signaling events: " + currentUserId);

        rootRef.child(SIGNALING_NODE)
                .child(currentUserId)
                .child(EVENTS_NODE)
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot,
                                             String previousChildName) {

                        String json = snapshot.getValue(String.class);
                        if (json == null) return;

                        try {
                            DataModel model =
                                    gson.fromJson(json, DataModel.class);

                            if (model == null || !model.isValid()) {
                                Log.w(TAG, "⚠️ Invalid DataModel");
                                snapshot.getRef().removeValue();
                                return;
                            }

                            Log.d(TAG, "📨 Received: " + model.getType()
                                    + " from " + model.getSender());

                            if (listener != null) {
                                listener.onNewEvent(model);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Parsing error", e);
                        }

                        // 🧹 DELETE after consume
                        snapshot.getRef().removeValue();
                    }

                    @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "❌ Listener cancelled", error.toException());
                    }
                });
    }
}
