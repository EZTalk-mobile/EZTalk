<<<<<<< HEAD
package com.example.project_ez_talk.ui.call.voice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.CallData;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.utils.PermissionHelper;
import com.example.project_ez_talk.webrtc.DataModelType;
import com.example.project_ez_talk.webrtc.FirebaseSignaling;
import com.example.project_ez_talk.webrtc.MainRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ COMPLETE VOICE CALL ACTIVITY - FULLY WORKING
 * Uses MainRepository for WebRTC audio streaming
 *
 * Features:
 * - Real microphone audio capture & transmission
 * - Echo cancellation & noise suppression
 * - NAT traversal (STUN/TURN servers)
 * - Mute/Unmute microphone
 * - Speaker on/off
 * - Call duration timer
 * - Firebase call logging
 * - Incoming/Outgoing call support
 */
public class VoiceCallActivity extends BaseActivity {

    public static final String EXTRA_CALL_TYPE = "call_type";
    private static final String TAG = "VoiceCall";
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";
    private static final long CALL_TIMEOUT_MS = 30000; // 30 seconds
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Intent extras
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_AVATAR = "user_avatar";
    public static final String EXTRA_CURRENT_USER_ID = "current_user_id";
    public static final String EXTRA_IS_INCOMING = "is_incoming";

    // UI Components
    private TextView tvCallDuration;
    private TextView tvCallerName;
    private TextView tvCallStatus;
    private ImageView ivCallerAvatar;
    private LinearLayout btnMuteLayout;
    private LinearLayout btnEndCallLayout;
    private LinearLayout btnSpeakerLayout;

    // User Information
    private String userId;              // Remote user ID
    private String userName;            // Remote user name
    private String userAvatar;          // Remote user avatar
    private String currentUserId;       // Current user ID
    private String currentUserName;     // Current user name
    private boolean isIncoming;         // Is this an incoming call?

    // Call State
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private int seconds = 0;
    private boolean isMicrophoneMuted = false;
    private boolean isSpeakerOn = false;
    private boolean isCallConnected = false;
    private boolean timerStarted = false;
    private boolean isEndingCall = false;
    private long callStartTime = 0;

    // ✅ WebRTC & Signaling
    private MainRepository mainRepository;
    private FirebaseSignaling firebaseSignaling;

    // Timer Runnables
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            if (tvCallDuration != null) {
                tvCallDuration.setText(formatTime(seconds));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCallConnected) {
                Log.w(TAG, "⏱️ Call timeout - no answer");
                runOnUiThread(() -> {
                    Toast.makeText(VoiceCallActivity.this, "No answer", Toast.LENGTH_SHORT).show();
                    endCall();
                });
            }
=======
package com.example.project_ez_talk.ui.call.voice;import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_ez_talk.databinding.ActivityVoiceCallBinding;
import com.permissionx.guolindev.PermissionX;

import java.util.Locale;

public class VoiceCallActivity extends AppCompatActivity {

    private ActivityVoiceCallBinding binding;
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;

    // Call timer variables
    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            binding.tvCallTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "   🎤 VOICE CALL ACTIVITY STARTED");
        Log.d(TAG, "════════════════════════════════════════");

        try {
            setContentView(R.layout.activity_voice_call);

            // Get intent extras
            userId = getIntent().getStringExtra(EXTRA_USER_ID);
            userName = getIntent().getStringExtra(EXTRA_USER_NAME);
            userAvatar = getIntent().getStringExtra(EXTRA_USER_AVATAR);
            currentUserId = getIntent().getStringExtra(EXTRA_CURRENT_USER_ID);
            isIncoming = getIntent().getBooleanExtra(EXTRA_IS_INCOMING, false);

            // Get current user from Firebase Auth if not provided
            if (currentUserId == null || currentUserId.isEmpty()) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    currentUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    if (currentUserName == null || currentUserName.isEmpty()) {
                        currentUserName = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    }
                } else {
                    Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }

            Log.d(TAG, "Remote User: " + userName + " (" + userId + ")");
            Log.d(TAG, "Current User: " + currentUserName + " (" + currentUserId + ")");
            Log.d(TAG, "Call Direction: " + (isIncoming ? "INCOMING" : "OUTGOING"));

            // Initialize UI
            initViews();
            setupClickListeners();
            loadCallerInfo();

            // ✅ Handle microphone permissions safely
            if (PermissionHelper.hasAudioPermission(this)) {
                Log.d(TAG, "✅ Microphone permission already granted");
                initializeWebRTC();
            } else {
                Log.d(TAG, "🎤 Requesting microphone permission...");
                PermissionHelper.requestAudioPermission(this);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ CRASH in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting call", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * ✅ Initialize UI views
     */
    private void initViews() {
        tvCallDuration = findViewById(R.id.tvCallDuration);
        tvCallerName = findViewById(R.id.tvUserName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        ivCallerAvatar = findViewById(R.id.ivUserAvatar);
        btnMuteLayout = findViewById(R.id.btnMute);
        btnEndCallLayout = findViewById(R.id.btnEndCall);
        btnSpeakerLayout = findViewById(R.id.btnSpeaker);

        Log.d(TAG, "✅ Views initialized");
    }

    /**
     * ✅ Setup click listeners for buttons
     */
    private void setupClickListeners() {
        if (btnMuteLayout != null) {
            btnMuteLayout.setOnClickListener(v -> {
                Log.d(TAG, "🔇 MUTE BUTTON CLICKED");
                toggleMute();
            });
        }

        if (btnEndCallLayout != null) {
            btnEndCallLayout.setOnClickListener(v -> {
                Log.d(TAG, "╔════════════════════════════╗");
                Log.d(TAG, "║   END CALL CLICKED 🔴     ║");
                Log.d(TAG, "╚════════════════════════════╝");
                endCall();
            });
        }

        if (btnSpeakerLayout != null) {
            btnSpeakerLayout.setOnClickListener(v -> {
                Log.d(TAG, "🔊 SPEAKER BUTTON CLICKED");
                toggleSpeaker();
            });
        }

        Log.d(TAG, "✅ Click listeners attached");
    }

    /**
     * ✅ Load and display caller information
     */
    private void loadCallerInfo() {
        // Set caller/user name
        if (tvCallerName != null) {
            tvCallerName.setText(userName != null && !userName.isEmpty() ? userName : "Unknown User");
        }

        // Load avatar
        if (ivCallerAvatar != null) {
            if (userAvatar != null && !userAvatar.isEmpty()) {
                Glide.with(this)
                        .load(userAvatar)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(ivCallerAvatar);
            } else {
                ivCallerAvatar.setImageResource(R.drawable.ic_profile);
            }
        }

        // Set initial status
        if (tvCallStatus != null) {
            tvCallStatus.setText(isIncoming ? "Connecting..." : "Calling...");
        }

        Log.d(TAG, "✅ Caller info loaded");
    }

    /**
     * ✅ Check microphone permission
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "🎤 Requesting microphone permission...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "✅ Microphone permission already granted");
            initializeWebRTC();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE_AUDIO) {
            if (PermissionHelper.isPermissionGranted(grantResults)) {
                Log.d(TAG, "✅ Microphone permission granted");
                initializeWebRTC(); // Initialize only after permission is granted
            } else {
                Log.e(TAG, "❌ Microphone permission denied");
                Toast.makeText(this, "Microphone permission required for voice calls", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    /**
     * ✅ Initialize WebRTC using MainRepository
     */
    /**
     * ✅ Initialize WebRTC using MainRepository
     */
    private void initializeWebRTC() {
        Log.d(TAG, "🎤 Initializing WebRTC via MainRepository...");

        try {
            // Get MainRepository singleton
            mainRepository = MainRepository.getInstance();

            // Login to WebRTC system
            mainRepository.login(
                    currentUserId,
                    currentUserName != null ? currentUserName : "User",
                    this,
                    () -> {
                        Log.d(TAG, "✅ MainRepository logged in successfully");

                        // Set repository listener for call events
                        mainRepository.repositoryListener = new MainRepository.RepositoryListener() {
                            @Override
                            public void onCallConnected() {
                                Log.d(TAG, "✅ CALL CONNECTED - Audio flowing!");
                                runOnUiThread(() -> onCallConnected());
                            }

                            @Override
                            public void onCallEnded() {
                                Log.d(TAG, "📞 Call ended by remote peer");
                                runOnUiThread(() -> {
                                    Toast.makeText(VoiceCallActivity.this,
                                            "Call ended", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }

                            @Override
                            public void onRemoteStreamAdded(org.webrtc.MediaStream mediaStream) {
                                Log.d(TAG, "📊 Remote audio stream received!");
                                Log.d(TAG, "   Audio tracks: " + mediaStream.audioTracks.size());
                                runOnUiThread(() -> onRemoteAudioReceived());
                            }
                        };

                        // Subscribe to WebRTC signaling events
                        mainRepository.subscribeForLatestEvent(model -> {
                            Log.d(TAG, "📨 WebRTC signaling event: " + model.getType());

                            // Nothing else needed here - MainRepository handles it
                        });

                        // Initialize Firebase Signaling for call notifications
                        initializeFirebaseSignaling();

                        // ✅ CRITICAL FIX: Handle incoming vs outgoing differently
                        if (isIncoming) {
                            Log.d(TAG, "📞 INCOMING CALL - Waiting for offer...");
                            updateCallStatus("Connecting...");
                            // Don't do anything else - wait for offer to arrive via signaling
                            // MainRepository will automatically create and send answer when offer arrives
                        } else {
                            Log.d(TAG, "📞 OUTGOING CALL - Creating offer...");
                            updateCallStatus("Calling...");
                            mainRepository.startCall(userId);
                            startCallTimeout();
                        }
                    }
            );

        } catch (Exception e) {
            Log.e(TAG, "❌ WebRTC initialization error: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to initialize audio", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    // ✅ ADD this helper method
    private void updateCallStatus(String status) {
        runOnUiThread(() -> {
            if (tvCallStatus != null) {
                tvCallStatus.setText(status);
            }
        });
    }
    /**
     * ✅ Initialize Firebase Signaling for call control (accept/reject/end)
     */
    private void initializeFirebaseSignaling() {
        Log.d(TAG, "🔔 Initializing Firebase Signaling...");

        firebaseSignaling = new FirebaseSignaling();
        firebaseSignaling.init(currentUserId, new FirebaseSignaling.OnSuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Firebase Signaling initialized");

                // Listen for call control signals (ACCEPT, REJECT, END)
                firebaseSignaling.observeIncomingCalls(new FirebaseSignaling.OnCallDataListener() {
                    @Override
                    public void onCallDataReceived(com.example.project_ez_talk.model.CallData callData) {
                        // Only process signals from the user we're calling
                        if (!callData.getSenderId().equals(userId)) {
                            return;
                        }

                        Log.d(TAG, "📨 Call signal: " + callData.getType());

                        switch (callData.getType()) {
                            case ACCEPT:
                                Log.d(TAG, "✅ Call accepted by remote peer");
                                // Call will connect via WebRTC signaling
                                break;

                            case REJECT:
                                Log.d(TAG, "❌ Call rejected by remote peer");
                                runOnUiThread(() -> {
                                    Toast.makeText(VoiceCallActivity.this,
                                            "Call declined", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                                break;

                            case END:
                                Log.d(TAG, "📞 Call ended by remote peer");
                                runOnUiThread(() -> {
                                    Toast.makeText(VoiceCallActivity.this,
                                            "Call ended", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                                break;
                        }
                    }

                    /**
                     * @param callData
                     */
                    @Override
                    public void onOffer(CallData callData) {

                    }

                    /**
                     * @param callData
                     */
                    @Override
                    public void onAnswer(CallData callData) {

                    }

                    /**
                     * @param callData
                     */
                    @Override
                    public void onIceCandidate(CallData callData) {

                    }

                    /**
                     * @param callData
                     */
                    @Override
                    public void onAccept(CallData callData) {

                    }

                    /**
                     * @param callData
                     */
                    @Override
                    public void onReject(CallData callData) {

                    }

                    @Override
                    public void onError() {
                        Log.e(TAG, "❌ Error receiving call signals");
                    }
                });
            }

            @Override
            public void onError() {
                Log.e(TAG, "❌ Firebase Signaling initialization failed");
            }
        });
    }

    /**
     * ✅ Called when remote audio stream is received
     */
    private void onRemoteAudioReceived() {
        Log.d(TAG, "🔊 Remote audio received - updating UI");

        if (tvCallStatus != null) {
            tvCallStatus.setText("Connected");
        }

        Toast.makeText(this, "Audio connected", Toast.LENGTH_SHORT).show();
    }

    /**
     * ✅ Called when call is fully connected
     */
    private void onCallConnected() {
        Log.d(TAG, "✅ CALL CONNECTED - Starting timer");

        // Cancel timeout
        timeoutHandler.removeCallbacks(timeoutRunnable);

        isCallConnected = true;
        callStartTime = System.currentTimeMillis();

        // Hide status text
        if (tvCallStatus != null) {
            tvCallStatus.setVisibility(View.GONE);
        }

        // Start call duration timer
        if (!timerStarted) {
            startCallTimer();
            timerStarted = true;
        }

        // Enable speaker for better audio
        if (!isSpeakerOn) {
            toggleSpeaker();
        }
    }

    /**
     * ✅ Start call timeout timer (30 seconds)
     */
    private void startCallTimeout() {
        Log.d(TAG, "⏱️ Starting 30-second call timeout");
        timeoutHandler.postDelayed(timeoutRunnable, CALL_TIMEOUT_MS);
    }

    /**
     * ✅ Start call duration timer
     */
    private void startCallTimer() {
        seconds = 0;
        timerHandler.post(timerRunnable);
        Log.d(TAG, "⏱️ Call timer started");
    }

    /**
     * ✅ Toggle microphone mute/unmute
     */
    private void toggleMute() {
        isMicrophoneMuted = !isMicrophoneMuted;

        if (mainRepository != null) {
            mainRepository.toggleAudio(!isMicrophoneMuted);
        }

        Log.d(TAG, "🎤 Microphone " + (isMicrophoneMuted ? "MUTED" : "UNMUTED"));
        Toast.makeText(this, isMicrophoneMuted ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
    }

    /**
     * ✅ Toggle speaker on/off
     */
    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;

        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setSpeakerphoneOn(isSpeakerOn);
                Log.d(TAG, "🔊 Speaker " + (isSpeakerOn ? "ON" : "OFF"));
                Toast.makeText(this, "Speaker " + (isSpeakerOn ? "On" : "Off"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Speaker toggle error: " + e.getMessage());
        }
    }

    /**
     * ✅ End call and cleanup all resources
     */
    private void endCall() {
        Log.d(TAG, "🔴 ENDING CALL...");

        if (isEndingCall) {
            Log.d(TAG, "Already ending call, ignoring duplicate request");
            return;
        }
        isEndingCall = true;

        // Stop all timers
        timerHandler.removeCallbacks(timerRunnable);
        timeoutHandler.removeCallbacks(timeoutRunnable);

        // Close WebRTC connection
        if (mainRepository != null) {
            mainRepository.endCall();
            Log.d(TAG, "✅ WebRTC connection closed");
        }

        // Send END signal to remote peer
        if (firebaseSignaling != null && userId != null) {
            firebaseSignaling.endCall(userId, () -> {
                Log.e(TAG, "Failed to send END signal");
            });
        }

        // Restore audio settings
        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setSpeakerphoneOn(false);
                audioManager.setMicrophoneMute(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Audio restore error: " + e.getMessage());
        }

        // Log call to database if it was connected
        if (isCallConnected) {
            logCallToDatabase();
        }

        Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show();

        // Finish activity after short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "✅ Finishing activity");
            finish();
        }, 500);
    }

    /**
     * ✅ Log completed call to Firebase Realtime Database
     */
    private void logCallToDatabase() {
        try {
            DatabaseReference callLogsRef = FirebaseDatabase.getInstance(DATABASE_URL)
                    .getReference("call_logs");

            String callId = callLogsRef.push().getKey();
            if (callId == null) {
                Log.e(TAG, "Failed to generate call ID");
                return;
            }

            // Calculate call duration
            long durationSeconds = seconds;

            Map<String, Object> callLog = new HashMap<>();
            callLog.put("callId", callId);
            callLog.put("callerId", currentUserId);
            callLog.put("receiverId", userId);
            callLog.put("callerName", currentUserName != null ? currentUserName : "Unknown");
            callLog.put("receiverName", userName != null ? userName : "Unknown");
            callLog.put("callType", "voice");
            callLog.put("status", "completed");
            callLog.put("startTime", callStartTime);
            callLog.put("duration", durationSeconds);
            callLog.put("timestamp", System.currentTimeMillis());

            callLogsRef.child(callId).setValue(callLog)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "✅ Call logged to database (duration: " + durationSeconds + "s)"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "❌ Failed to log call: " + e.getMessage()));

        } catch (Exception e) {
            Log.e(TAG, "Database logging error: " + e.getMessage(), e);
        }
    }

    /**
     * Format seconds to MM:SS
     */
    @SuppressLint("DefaultLocale")
    private String formatTime(int secs) {
        int minutes = secs / 60;
        int seconds = secs % 60;
        return String.format("%02d:%02d", minutes, seconds);
=======

        // Initialize View Binding
        binding = ActivityVoiceCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Request Microphone Permission
        requestPermissions();

        // 2. Get intent data (who are we calling?)
        String userName = getIntent().getStringExtra("user_name");
        binding.tvCallerName.setText(userName != null ? userName : "Unknown User");

        // 3. Set up click listeners
        setupClickListeners();

        // 4. Start call logic (Placeholder for WebRTC/Mesibo logic)
        startCall();
    }

    private void requestPermissions() {
        PermissionX.init(this)
                .permissions(android.Manifest.permission.RECORD_AUDIO)
                .request((allGranted, grantedList, deniedList) -> {
                    if (!allGranted) {
                        Toast.makeText(this, "Microphone permission is required for voice calls", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setupClickListeners() {
        // End Call
        binding.fabEndCall.setOnClickListener(v -> endCall());

        // Mute/Unmute
        binding.fabMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            binding.fabMute.setSelected(isMuted);
            // logic to mute RTC stream
            Toast.makeText(this, isMuted ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
        });

        // Speaker Toggle
        binding.fabSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            binding.fabSpeaker.setSelected(isSpeakerOn);
            // logic to toggle audio output
            Toast.makeText(this, isSpeakerOn ? "Speaker On" : "Speaker Off", Toast.LENGTH_SHORT).show();
        });
    }

    private void startCall() {
        // Start the timer
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        binding.tvCallStatus.setText("Connected");

        // TODO: Initialize Mesibo WebRTC here
    }

    private void endCall() {
        timerHandler.removeCallbacks(timerRunnable);
        // TODO: Terminate WebRTC session
        finish();
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
<<<<<<< HEAD
        Log.d(TAG, "onDestroy - Cleaning up resources");

        // Stop timers
        timerHandler.removeCallbacks(timerRunnable);
        timeoutHandler.removeCallbacks(timeoutRunnable);

        // Cleanup WebRTC
        if (mainRepository != null) {
            mainRepository.endCall();
        }

        // Cleanup Firebase Signaling
        if (firebaseSignaling != null) {
            firebaseSignaling.removeListener();
            firebaseSignaling.cleanup();
        }

        // Restore audio settings
        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setMicrophoneMute(false);
                audioManager.setSpeakerphoneOn(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Cleanup error: " + e.getMessage());
        }

        Log.d(TAG, "✅ Cleanup complete");
    }
}
=======
        timerHandler.removeCallbacks(timerRunnable);
    }
}
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
