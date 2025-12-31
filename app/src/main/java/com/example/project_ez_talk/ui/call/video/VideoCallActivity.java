package com.example.project_ez_talk.ui.call.video;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.CallLog;
import com.example.project_ez_talk.service.CallService;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.webTRC.MainRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.webrtc.SurfaceViewRenderer;

public class VideoCallActivity extends BaseActivity implements MainRepository.Listener {

    private static final String TAG = "VideoCallActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

    // Constants matching CallActivity
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_AVATAR = "user_avatar";
    public static final String EXTRA_IS_INCOMING = "is_incoming";

    private TextView tvCallDuration, tvRemoteName, tvCallStatus;
    private ImageView ivRemoteAvatar, ivLocalAvatar;
    private FloatingActionButton fabMic, fabVideo, fabEndCall, fabSwitchCamera;
    private View cvCallDuration;

    // WebRTC Views
    private SurfaceViewRenderer localView, remoteView;

    private String userId;
    private String userName;
    private String userAvatar;
    private boolean isIncoming;

    private MainRepository mainRepository;
    private String currentUsername;
    private MediaPlayer ringingPlayer;
    private boolean permissionsGranted = false;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int seconds = 0;
    private boolean isMuted = false;
    private boolean isVideoOn = true;
    private boolean isCallConnected = false;
    private boolean isRinging = false;
    private Handler connectionTimeoutHandler = new Handler(Looper.getMainLooper());

    // Call log tracking
    private long callStartTime = 0;
    private String currentUserName = "";
    private String currentUserAvatar = "";
    private FirebaseFirestore firestore;
    private DatabaseReference callLogsRef;
    private Runnable connectionTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCallConnected && !isFinishing()) {
                Log.e(TAG, "❌ Connection timeout - no response after 20 seconds");
                Toast.makeText(VideoCallActivity.this, "Connection failed - user may be offline", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            tvCallDuration.setText(formatTime(seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // Get extras from intent
        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        userAvatar = getIntent().getStringExtra(EXTRA_USER_AVATAR);
        isIncoming = getIntent().getBooleanExtra(EXTRA_IS_INCOMING, false);

        // Get MainRepository instance
        mainRepository = MainRepository.getInstance();

        // Get current user info
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUsername = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        callLogsRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("call_logs");

        // Load current user info for call log
        loadCurrentUserInfo();

        initViews();

        setupClickListeners();
        loadUserInfo();

        // Check and request permissions
        if (checkPermissions()) {
            permissionsGranted = true;
            // Login to create fresh WebRTCClient for this call
            mainRepository.login(currentUsername, this, () -> {
                // Initialize views after login
                mainRepository.initLocalView(findViewById(R.id.localView));
                mainRepository.initRemoteView(findViewById(R.id.remoteView));
                mainRepository.listener = this;
                initiateCall();
            });
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                permissionsGranted = true;
                // Login to create fresh WebRTCClient
                mainRepository.login(currentUsername, this, () -> {
                    mainRepository.initLocalView(findViewById(R.id.localView));
                    mainRepository.initRemoteView(findViewById(R.id.remoteView));
                    mainRepository.listener = this;
                    initiateCall();
                });
            } else {
                Toast.makeText(this, "Camera and microphone permissions are required for video calls", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initiateCall() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Start foreground service
        Intent serviceIntent = new Intent(VideoCallActivity.this, CallService.class);
        serviceIntent.setAction("START_CALL");
        serviceIntent.putExtra("caller_name", userName);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        if (!isIncoming) {
            // CALLER: Just send request (YouTube flow)
            tvCallStatus.setText("Calling...");
            mainRepository.sendCallRequest(userId, () -> {
                Toast.makeText(VideoCallActivity.this, "Failed to send call request", Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            // CALLEE: Create offer (YouTube flow)
            tvCallStatus.setText("Connecting...");
            mainRepository.startCall(userId);
        }
    }

    private void initViews() {
        tvCallDuration = findViewById(R.id.tvCallDuration);
        tvRemoteName = findViewById(R.id.tvRemoteName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        ivRemoteAvatar = findViewById(R.id.ivRemoteAvatar);
        ivLocalAvatar = findViewById(R.id.ivLocalAvatar);
        cvCallDuration = findViewById(R.id.cvCallDuration);

        fabMic = findViewById(R.id.fabMic);
        fabVideo = findViewById(R.id.fabVideo);
        fabEndCall = findViewById(R.id.fabEndCall);
        fabSwitchCamera = findViewById(R.id.fabSwitchCamera);
    }

    private void setupClickListeners() {
        fabMic.setOnClickListener(v -> toggleMute());
        fabVideo.setOnClickListener(v -> toggleVideo());
        fabEndCall.setOnClickListener(v -> endCall());
        fabSwitchCamera.setOnClickListener(v -> switchCamera());
    }

    private void loadUserInfo() {
        // Set remote user name
        if (userName != null && !userName.isEmpty()) {
            tvRemoteName.setText(userName);
        } else {
            tvRemoteName.setText("Unknown User");
        }

        // Load remote user avatar
        if (userAvatar != null && !userAvatar.isEmpty()) {
            Glide.with(this)
                    .load(userAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(ivRemoteAvatar);
        }

        // Set initial call status
        tvCallStatus.setText(isIncoming ? "Connecting..." : "Calling...");
    }

    private void onCallConnected() {
        isCallConnected = true;
        isRinging = false;

        // Record call start time
        callStartTime = System.currentTimeMillis();
        Log.d(TAG, "📞 Call started at: " + callStartTime);

        // Cancel connection timeout
        connectionTimeoutHandler.removeCallbacks(connectionTimeoutRunnable);
        Log.d(TAG, "✅ Call connected successfully!");

        // Stop ringing sound
        if (ringingPlayer != null) {
            ringingPlayer.stop();
            ringingPlayer.release();
            ringingPlayer = null;
        }

        tvCallStatus.setText("Connected");

        // Show call duration and hide status after 1 second
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvCallStatus.setVisibility(View.GONE);
            cvCallDuration.setVisibility(View.VISIBLE);
            startCallTimer();
        }, 1000);
    }

    private void toggleMute() {
        isMuted = !isMuted;
        mainRepository.toggleAudio(isMuted);
        fabMic.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        Toast.makeText(this, isMuted ? "Microphone muted" : "Microphone unmuted", Toast.LENGTH_SHORT).show();
    }

    private void toggleVideo() {
        isVideoOn = !isVideoOn;
        mainRepository.toggleVideo(isVideoOn);
        fabVideo.setImageResource(isVideoOn ? R.drawable.ic_video : R.drawable.ic_video_off);
        Toast.makeText(this, isVideoOn ? "Camera on" : "Camera off", Toast.LENGTH_SHORT).show();
    }

    private void switchCamera() {
        mainRepository.switchCamera();
        Toast.makeText(this, "Camera switched", Toast.LENGTH_SHORT).show();
    }

    private void endCall() {
        timerHandler.removeCallbacks(timerRunnable);

        // Stop ringing if active
        if (isRinging && ringingPlayer != null) {
            ringingPlayer.stop();
            ringingPlayer.release();
            ringingPlayer = null;
        }

        // Save call log before ending
        saveCallLog(isCallConnected ? "completed" : "missed");

        // End WebRTC call
        if (mainRepository != null) {
            mainRepository.endCall();
        }

        // Stop foreground service
        Intent serviceIntent = new Intent(this, CallService.class);
        serviceIntent.setAction("END_CALL");
        startService(serviceIntent);

        Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startCallTimer() {
        seconds = 0;
        timerHandler.post(timerRunnable);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int secs) {
        int minutes = secs / 60;
        int seconds = secs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Load current user info from Firestore for call log
     */
    private void loadCurrentUserInfo() {
        if (currentUsername == null) return;

        firestore.collection("users")
                .document(currentUsername)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentUserName = doc.getString("name");
                        currentUserAvatar = doc.getString("profilePicture");
                        Log.d(TAG, "✅ Current user info loaded: " + currentUserName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to load current user info: " + e.getMessage());
                });
    }

    /**
     * Save call log to Firebase Realtime Database
     * @param status Call status: "completed", "missed", "rejected"
     */
    private void saveCallLog(String status) {
        if (currentUsername == null || userId == null) {
            Log.e(TAG, "❌ Cannot save call log: missing user IDs");
            return;
        }

        // Calculate duration (make it final for lambda)
        final long duration = (callStartTime > 0)
                ? (System.currentTimeMillis() - callStartTime) / 1000
                : 0; // seconds

        // Create call log
        CallLog callLog = new CallLog();

        // Determine who is caller/receiver
        if (isIncoming) {
            // Current user is receiver, remote user is caller
            callLog.setCallerId(userId);
            callLog.setReceiverId(currentUsername);
            callLog.setCallerName(userName != null ? userName : "Unknown");
            callLog.setReceiverName(currentUserName != null ? currentUserName : "You");
            callLog.setCallerAvatar(userAvatar != null ? userAvatar : "");
            callLog.setReceiverAvatar(currentUserAvatar != null ? currentUserAvatar : "");
        } else {
            // Current user is caller, remote user is receiver
            callLog.setCallerId(currentUsername);
            callLog.setReceiverId(userId);
            callLog.setCallerName(currentUserName != null ? currentUserName : "You");
            callLog.setReceiverName(userName != null ? userName : "Unknown");
            callLog.setCallerAvatar(currentUserAvatar != null ? currentUserAvatar : "");
            callLog.setReceiverAvatar(userAvatar != null ? userAvatar : "");
        }

        callLog.setCallType("video");
        callLog.setStatus(status);
        callLog.setStartTime(callStartTime > 0 ? callStartTime : System.currentTimeMillis());
        callLog.setDuration(duration);
        callLog.setTimestamp(System.currentTimeMillis());

        // Save to Realtime Database
        String callLogId = callLogsRef.push().getKey();
        if (callLogId != null) {
            callLogsRef.child(callLogId)
                    .setValue(callLog)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Call log saved: " + callLogId + " (status: " + status + ", duration: " + duration + "s)");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to save call log: " + e.getMessage());
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        connectionTimeoutHandler.removeCallbacks(connectionTimeoutRunnable);

        // Clean up WebRTC resources
        try {
            if (mainRepository != null) {
                mainRepository.endCall();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up WebRTC: " + e.getMessage());
        }
    }

    // MainRepository.Listener implementation
    @Override
    public void webrtcConnected() {
        runOnUiThread(this::onCallConnected);
    }

    @Override
    public void webrtcClosed() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Call disconnected", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}