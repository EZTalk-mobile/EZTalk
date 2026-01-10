    package com.example.project_ez_talk.webrtc;

    import android.util.Log;

    import com.example.project_ez_talk.model.CallData;
    import com.google.firebase.database.*;
    import com.google.gson.Gson;

    /**
     * ✅ COMPLETE FirebaseSignaling with SINGLETON PATTERN
     * Ensures only ONE listener instance is active at any time
     * Handles incoming calls, call control signals (ACCEPT, REJECT, END)
     */
    public class FirebaseSignaling {

        private static final String TAG = "FirebaseSignaling";
        private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";
        private static final String SIGNALING_NODE = "signaling";
        private static final String LATEST_EVENT_FIELD = "latest_event";

        // ✅ SINGLETON INSTANCE
        private static FirebaseSignaling instance;

        private final DatabaseReference dbRef;
        private final Gson gson = new Gson();
        private String currentUserId;
        private ValueEventListener callListener;
        private boolean isListening = false;

        // ============================================================
        // SINGLETON PATTERN
        // ============================================================

        /**
         * ✅ Get singleton instance of FirebaseSignaling
         * Ensures only ONE listener is created and active
         */
        public static synchronized FirebaseSignaling getInstance() {
            if (instance == null) {
                instance = new FirebaseSignaling();
                Log.d(TAG, "✅ FirebaseSignaling SINGLETON created");
            }
            return instance;
        }

        /**
         * ✅ Private constructor - use getInstance() instead
         */
        public FirebaseSignaling() {
            dbRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference();
            Log.d(TAG, "✅ FirebaseSignaling initialized with correct database URL");
        }

        // ============================================================
        // INTERFACES
        // ============================================================

        public interface OnSuccessListener {
            void onSuccess();
            void onError();
        }

        public interface OnCallDataListener {
            void onCallDataReceived(CallData callData);

            void onOffer(CallData callData);

            void onAnswer(CallData callData);

            void onIceCandidate(CallData callData);

            void onAccept(CallData callData);

            void onReject(CallData callData);

            void onError();
        }

        // ============================================================
        // INITIALIZATION
        // ============================================================

        /**
         * ✅ Initialize FirebaseSignaling for a user
         * Safe to call multiple times - won't reinitialize if already done
         */
        public void init(String userId, OnSuccessListener listener) {
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "❌ Invalid user ID");
                if (listener != null) listener.onError();
                return;
            }

            // ✅ DON'T RE-INITIALIZE if already done for this user
            if (currentUserId != null && currentUserId.equals(userId)) {
                Log.d(TAG, "⚠️ Already initialized for user: " + userId);
                if (listener != null) listener.onSuccess();
                return;
            }

            currentUserId = userId;

            // Create user node in Firebase
            dbRef.child(SIGNALING_NODE)
                    .child(currentUserId)
                    .child(LATEST_EVENT_FIELD)
                    .setValue("")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "✅ User node initialized: " + userId);
                            if (listener != null) listener.onSuccess();
                        } else {
                            Log.e(TAG, "❌ Failed to initialize user node: " + task.getException());
                            if (listener != null) listener.onError();
                        }
                    });
        }

        // ============================================================
        // SENDING CALLS
        // ============================================================

        /**
         * ✅ Send call data to another user
         */
        public void sendCallData(CallData callData, Runnable onError) {
            if (callData == null || callData.getTargetId() == null || callData.getTargetId().isEmpty()) {
                Log.e(TAG, "❌ Invalid call data or target ID");
                if (onError != null) onError.run();
                return;
            }

            // Set sender if not already set
            if (callData.getSenderId() == null || callData.getSenderId().isEmpty()) {
                callData.setSenderId(currentUserId);
            }

            // Add timestamp if not set
            if (callData.getTimestamp() == 0) {
                callData.setTimestamp(System.currentTimeMillis());
            }

            String json = gson.toJson(callData);

            dbRef.child(SIGNALING_NODE)
                    .child(callData.getTargetId())
                    .child(LATEST_EVENT_FIELD)
                    .setValue(json)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Call data sent successfully");
                        Log.d(TAG, "   Type: " + callData.getType());
                        Log.d(TAG, "   To: " + callData.getTargetId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to send call data: " + e.getMessage());
                        if (onError != null) onError.run();
                    });
        }

        // ============================================================
        // RECEIVING CALLS
        // ============================================================

        /**
         * ✅ Listen for incoming calls and control signals
         * Only activate ONE listener per user
         */
        public void observeIncomingCalls(OnCallDataListener listener) {
            if (currentUserId == null || currentUserId.isEmpty()) {
                Log.e(TAG, "❌ User not initialized");
                if (listener != null) listener.onError();
                return;
            }

            // ✅ IMPORTANT: Only one listener at a time
            if (isListening) {
                Log.w(TAG, "⚠️ Already listening for calls - ignoring duplicate request");
                return;
            }

            isListening = true;

            DatabaseReference ref = dbRef.child(SIGNALING_NODE)
                    .child(currentUserId)
                    .child(LATEST_EVENT_FIELD);

            callListener = new ValueEventListener() {
                private long lastTimestamp = 0;

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "⚠️ No data at user node (yet)");
                        return;
                    }

                    String data = snapshot.getValue(String.class);
                    if (data == null || data.isEmpty()) {
                        Log.d(TAG, "⚠️ Empty data received");
                        return;
                    }

                    try {
                        CallData callData = gson.fromJson(data, CallData.class);

                        // Validate call data
                        if (callData == null || !callData.isValid()) {
                            Log.w(TAG, "⚠️ Invalid CallData received");
                            return;
                        }

                        // Check if this is a new event (avoid processing same event twice)
                        long timestamp = callData.getTimestamp();
                        if (timestamp <= lastTimestamp) {
                            Log.d(TAG, "⚠️ Ignoring old event (timestamp: " + timestamp + ")");
                            return;
                        }

                        lastTimestamp = timestamp;

                        // Log received call
                        Log.d(TAG, "════════════════════════════════════════");
                        Log.d(TAG, "📨 Received: " + callData.getType() + " from " + callData.getSenderId());
                        Log.d(TAG, "   Call Type: " + callData.getCallType());
                        Log.d(TAG, "   Data: " + callData.getData());
                        Log.d(TAG, "════════════════════════════════════════");

                        // Notify listener
                        if (listener != null) {
                            listener.onCallDataReceived(callData);
                        }

                        // Clear after processing to avoid duplicates
                        snapshot.getRef().setValue("");

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing call data: " + e.getMessage(), e);
                        if (listener != null) listener.onError();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "❌ Listener cancelled: " + error.getMessage());
                    isListening = false;
                    if (listener != null) listener.onError();
                }
            };

            // Start listening
            ref.addValueEventListener(callListener);
            Log.d(TAG, "👂 Listening started for user: " + currentUserId);
        }

        // ============================================================
        // CALL CONTROL SIGNALS
        // ============================================================

        /**
         * ✅ Send ACCEPT signal to caller
         */
        public void acceptCall(String callerId, String callType, Runnable onError) {
            Log.d(TAG, "📤 Sending ACCEPT signal to: " + callerId);
            sendSimpleResponse(callerId, callType, CallData.Type.ACCEPT, "Call accepted", onError);
        }

        /**
         * ✅ Send REJECT signal to caller
         */
        public void rejectCall(String callerId, String callType, Runnable onError) {
            Log.d(TAG, "📤 Sending REJECT signal to: " + callerId);
            sendSimpleResponse(callerId, callType, CallData.Type.REJECT, "Call rejected", onError);
        }

        /**
         * ✅ Send END signal to end the call
         */
        public void endCall(String targetId, Runnable onError) {
            Log.d(TAG, "📤 Sending END signal to: " + targetId);
            sendSimpleResponse(targetId, null, CallData.Type.END, "Call ended", onError);
        }

        /**
         * ✅ Helper method to send simple call control responses
         */
        private void sendSimpleResponse(String targetId, String callType, CallData.Type type, String message, Runnable onError) {
            if (targetId == null || targetId.isEmpty() || currentUserId == null) {
                Log.e(TAG, "❌ Cannot send response - missing target or current user");
                if (onError != null) onError.run();
                return;
            }

            CallData data = new CallData();
            data.setTargetId(targetId);
            data.setSenderId(currentUserId);
            data.setType(type);
            data.setCallType(callType != null ? callType : "voice");
            data.setData(message);
            data.setTimestamp(System.currentTimeMillis());

            sendCallData(data, onError);
        }

        // ============================================================
        // CLEANUP
        // ============================================================

        /**
         * ✅ Remove listener without destroying singleton
         */
        public void removeListener() {
            if (callListener != null && currentUserId != null) {
                dbRef.child(SIGNALING_NODE)
                        .child(currentUserId)
                        .child(LATEST_EVENT_FIELD)
                        .removeEventListener(callListener);
                callListener = null;
                isListening = false;
                Log.d(TAG, "👂 Listener removed");
            }
        }

        /**
         * ✅ Cleanup Firebase Signaling
         * Note: Doesn't destroy singleton - it will persist
         */
        public void cleanup() {
            Log.d(TAG, "🧹 FirebaseSignaling cleanup called");
            removeListener();
            // ✅ IMPORTANT: Don't set currentUserId to null
            // Singleton should persist across activity changes
        }

        // ============================================================
        // GETTERS
        // ============================================================

        /**
         * Get current user ID
         */
        public String getCurrentUserId() {
            return currentUserId;
        }

        /**
         * Check if listener is active
         */
        public boolean isListening() {
            return isListening;
        }

        /**
         * Check if initialized
         */
        public boolean isInitialized() {
            return currentUserId != null && !currentUserId.isEmpty();
        }

        /**
         * Get database reference (for advanced usage)
         */
        public DatabaseReference getDatabaseReference() {
            return dbRef;
        }

        // ============================================================
        // DEBUGGING
        // ============================================================

        /**
         * Get status information
         */
        public String getStatus() {
            return "FirebaseSignaling{" +
                    "userId=" + currentUserId +
                    ", isListening=" + isListening +
                    ", isInitialized=" + isInitialized() +
                    "}";
        }
    }