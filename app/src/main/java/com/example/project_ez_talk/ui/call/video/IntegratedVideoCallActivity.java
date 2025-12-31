package com.example.project_ez_talk.ui.call.video;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.databinding.ActivityVideoCallBinding;
import com.example.project_ez_talk.model.CallData;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.utils.PermissionHelper;
import com.example.project_ez_talk.webrtc.FirebaseSignaling;
import com.example.project_ez_talk.webrtc.MainRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * IntegratedVideoCallActivity - Handles video call UI and WebRTC integration
 * Displays local and remote video streams with call controls
 */
public class IntegratedVideoCallActivity extends BaseActivity {
    private static final String TAG = "VideoCall";
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

    // Intent extra keys
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_AVATAR = "user_avatar";
    public static final String EXTRA_CURRENT_USER_ID = "current_user_id";
    public static final String EXTRA_IS_INCOMING = "is_incoming";

    private ActivityVideoCallBinding binding;
    private MainRepository repository;
    private FirebaseSignaling firebaseSignaling;

    // ✅ WebRTC View References
    private SurfaceViewRenderer localView;
    private SurfaceViewRenderer remoteView;

    private String remoteUserId;
    private String remoteUserName;
    private String currentUserId;
    private boolean isIncoming;
    private boolean isMicOn = true;
    private boolean isVideoOn = true;
    private long callStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "Video Call Activity started");

        // Get intent extras
        remoteUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        remoteUserName = getIntent().getStringExtra(EXTRA_USER_NAME);
        currentUserId = getIntent().getStringExtra(EXTRA_CURRENT_USER_ID);
        isIncoming = getIntent().getBooleanExtra(EXTRA_IS_INCOMING, false);

        // Validate current user
        if (currentUserId == null || currentUserId.isEmpty()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } else {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "User not authenticated");
                finish();
                return;
            }
        }

        Log.d(TAG, "Remote User: " + remoteUserName + " (" + remoteUserId + ")");
        Log.d(TAG, "Current User: " + currentUserId);
        Log.d(TAG, "Is Incoming: " + isIncoming);

        // ✅ Initialize WebRTC Views
        initializeWebRTCViews();

        // Initialize UI
        setupUI();

        // ✅ Handle camera and microphone permissions
        if (PermissionHelper.hasCameraPermission(this) && PermissionHelper.hasAudioPermission(this)) {
            Log.d(TAG, "✅ All permissions granted");
            initializeFirebaseSignaling();
            initWebRTC();
        } else {
            Log.d(TAG, "🎥 Requesting camera & microphone permissions...");
            PermissionHelper.requestCameraAndAudioPermission(this);
        }
    }

    /**
     * ✅ Initialize WebRTC SurfaceViewRenderer references
     */
    private void initializeWebRTCViews() {
        try {
            // Get remote view from remote container
            localView = binding.localViewCard.findViewById(R.id.localView);
            remoteView = binding.remoteVideoContainer.findViewById(R.id.remoteView);

            if (localView == null || remoteView == null) {
                Log.e(TAG, "❌ Failed to initialize WebRTC views - views are null");
                Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "✅ WebRTC views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing WebRTC views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing camera views", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE_CAMERA_AUDIO) {
            if (PermissionHelper.isPermissionGranted(grantResults)) {
                Log.d(TAG, "✅ Camera & microphone permissions granted");
                initializeFirebaseSignaling();
                initWebRTC();
            } else {
                Log.e(TAG, "❌ Camera or microphone permission denied");
                Toast.makeText(this, "Camera and microphone permissions are required for video calls", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * ✅ Initialize Firebase Signaling for call notifications
     */
    private void initializeFirebaseSignaling() {
        firebaseSignaling = new FirebaseSignaling();
        firebaseSignaling.init(currentUserId, new FirebaseSignaling.OnSuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Firebase Signaling initialized");

                // Listen for call events (ACCEPT/REJECT/END)
                firebaseSignaling.observeIncomingCalls(new FirebaseSignaling.OnCallDataListener() {
                    @Override
                    public void onCallDataReceived(CallData callData) {
                        handleCallSignal(callData);
                    }

                    @Override
                    public void onOffer(CallData callData) {
                    }

                    @Override
                    public void onAnswer(CallData callData) {
                    }

                    @Override
                    public void onIceCandidate(CallData callData) {
                    }

                    @Override
                    public void onAccept(CallData callData) {
                    }

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
                Log.e(TAG, "❌ Failed to initialize Firebase Signaling");
            }
        });
    }

    /**
     * ✅ Handle incoming call signals (ACCEPT/REJECT/END)
     */
    private void handleCallSignal(CallData callData) {
        Log.d(TAG, "📨 Call signal received: " + callData.getType());

        switch (callData.getType()) {
            case ACCEPT:
                Log.d(TAG, "✅ Call accepted by peer");
                // Peer accepted - WebRTC will handle connection
                break;

            case REJECT:
                Log.d(TAG, "❌ Call rejected by peer");
                runOnUiThread(() -> {
                    Toast.makeText(IntegratedVideoCallActivity.this, "Call declined", Toast.LENGTH_SHORT).show();
                    finish();
                });
                break;

            case END:
                Log.d(TAG, "📞 Call ended by peer");
                runOnUiThread(() -> {
                    Toast.makeText(IntegratedVideoCallActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
                    finish();
                });
                break;
        }
    }

    /**
     * ✅ Initialize WebRTC for actual video streaming
     */
    private void initWebRTC() {
        repository = MainRepository.getInstance();

        // Login to WebRTC system
        repository.login(
                currentUserId,
                remoteUserName != null ? remoteUserName : "User",
                this,
                () -> {
                    Log.d(TAG, "✅ WebRTC logged in");

                    try {
                        // ✅ Initialize video surfaces with proper views
                        repository.initLocalView(localView);
                        repository.initRemoteView(remoteView);
                        Log.d(TAG, "✅ Video surfaces initialized");
                    } catch (Exception e) {
                        Log.e(TAG, "Error initializing video surfaces", e);
                        Toast.makeText(this, "Error initializing camera", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Setup connection listener
                    repository.repositoryListener = new MainRepository.RepositoryListener() {
                        @Override
                        public void onCallConnected() {
                            Log.d(TAG, "✅ WebRTC call connected!");
                            callStartTime = System.currentTimeMillis();
                            runOnUiThread(() -> {
                                binding.tvCallStatus.setVisibility(View.GONE);
                                binding.cvCallDuration.setVisibility(View.VISIBLE);
                                Toast.makeText(IntegratedVideoCallActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onCallEnded() {
                            Log.d(TAG, "📞 WebRTC call ended");
                            runOnUiThread(() -> {
                                logCallToDatabase();
                                IntegratedVideoCallActivity.this.finish();
                            });
                        }

                        @Override
                        public void onRemoteStreamAdded(org.webrtc.MediaStream mediaStream) {
                            Log.d(TAG, "📹 Remote stream added, tracks: " + mediaStream.videoTracks.size());
                        }
                    };

                    // Subscribe to WebRTC signaling events
                    repository.subscribeForLatestEvent(model -> {
                        Log.d(TAG, "WebRTC signaling event: " + model.getType());
                    });

                    // Start the call based on direction
                    if (!isIncoming) {
                        // Outgoing call: send call request
                        repository.sendCallRequest(remoteUserId, () -> {
                            runOnUiThread(() -> {
                                Toast.makeText(IntegratedVideoCallActivity.this,
                                        "User not available", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        });
                    } else {
                        // Incoming call: start WebRTC connection immediately
                        repository.startCall(remoteUserId);
                    }
                }
        );
    }

    /**
     * Setup UI controls
     */
    private void setupUI() {
        // Set remote user name
        binding.tvRemoteName.setText(remoteUserName != null ? remoteUserName : "Unknown User");
        binding.tvCallStatus.setText(isIncoming ? "Connecting..." : "Calling...");

        Log.d(TAG, "✅ UI setup complete");

        // Microphone toggle
        binding.fabMic.setOnClickListener(v -> toggleMicrophone());

        // Video toggle
        binding.fabVideo.setOnClickListener(v -> toggleVideo());

        // Switch camera
        binding.fabSwitchCamera.setOnClickListener(v -> switchCamera());

        // End call
        binding.fabEndCall.setOnClickListener(v -> endCall());
    }

    /**
     * Toggle microphone on/off
     */
    private void toggleMicrophone() {
        isMicOn = !isMicOn;

        if (repository != null) {
            repository.toggleAudio(isMicOn);
        }

        binding.fabMic.setImageResource(isMicOn ? R.drawable.ic_mic : R.drawable.ic_mic_off);
        Toast.makeText(this, isMicOn ? "Mic on" : "Mic off", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "🎤 Microphone toggled: " + isMicOn);
    }

    /**
     * Toggle video on/off
     */
    private void toggleVideo() {
        isVideoOn = !isVideoOn;

        if (repository != null) {
            repository.toggleVideo(isVideoOn);
        }

        binding.fabVideo.setImageResource(isVideoOn ? R.drawable.ic_video : R.drawable.ic_video_off);
        Toast.makeText(this, isVideoOn ? "Camera on" : "Camera off", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "📹 Video toggled: " + isVideoOn);
    }

    /**
     * Switch between front and back camera
     */
    private void switchCamera() {
        if (repository != null) {
            repository.switchCamera();
            Toast.makeText(this, "Camera switched", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "📷 Camera switched");
        }
    }

    /**
     * ✅ End call with proper cleanup
     */
    private void endCall() {
        Log.d(TAG, "User clicked end call");

        // ✅ Send END signal to peer
        if (firebaseSignaling != null && remoteUserId != null) {
            firebaseSignaling.endCall(remoteUserId, () -> {
                Log.e(TAG, "❌ Failed to send end signal");
            });
        }

        // ✅ End WebRTC call
        if (repository != null) {
            repository.endCall();
        }

        // ✅ Log call to database
        logCallToDatabase();

        finish();
    }

    /**
     * ✅ Log video call to Firebase Realtime Database
     */
    private void logCallToDatabase() {
        DatabaseReference callLogsRef = FirebaseDatabase.getInstance(DATABASE_URL)
                .getReference("call_logs");

        String callId = callLogsRef.push().getKey();
        if (callId == null) {
            Log.e(TAG, "Failed to generate call ID");
            return;
        }

        // Determine call status
        String callStatus = (callStartTime > 0) ? "completed" : "cancelled";
        long duration = (callStartTime > 0) ?
                (System.currentTimeMillis() - callStartTime) / 1000 : 0;

        // Get caller info
        String callerName = "Unknown";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            callerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (callerName == null || callerName.isEmpty()) {
                callerName = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }
        }

        Map<String, Object> callLog = new HashMap<>();
        callLog.put("callId", callId);
        callLog.put("callerId", currentUserId);
        callLog.put("receiverId", remoteUserId);
        callLog.put("callerName", callerName != null ? callerName : "Unknown");
        callLog.put("receiverName", remoteUserName != null ? remoteUserName : "Unknown");
        callLog.put("callType", "video");
        callLog.put("status", callStatus);
        callLog.put("startTime", callStartTime > 0 ? callStartTime : System.currentTimeMillis());
        callLog.put("duration", duration);
        callLog.put("timestamp", System.currentTimeMillis());

        callLogsRef.child(callId).setValue(callLog)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Call logged to database"))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to log call: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Cleaning up");

        // ✅ Cleanup Firebase Signaling
        if (firebaseSignaling != null) {
            firebaseSignaling.removeListener();
            firebaseSignaling.cleanup();
            firebaseSignaling = null;
        }

        // ✅ Cleanup WebRTC
        if (repository != null) {
            repository.endCall();
        }
    }
}