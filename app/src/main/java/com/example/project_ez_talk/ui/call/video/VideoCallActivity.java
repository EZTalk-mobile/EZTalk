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
import com.example.project_ez_talk.service.CallService;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.webTRC.MainRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import org.webrtc.SurfaceViewRenderer;

public class VideoCallActivity extends BaseActivity implements MainRepository.Listener {

    private static final String TAG = "VideoCallActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

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
        mainRepository.listener = this;

        // Get current user info
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUsername = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews();
        setupClickListeners();
        loadUserInfo();

        // Check and request permissions
        if (checkPermissions()) {
            permissionsGranted = true;
            // Don't setup WebRTC here - wait until after login
            initiateCall();
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
                // Don't setup WebRTC here - wait until after login
                initiateCall();
            } else {
                Toast.makeText(this, "Camera and microphone permissions are required for video calls", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initiateCall() {
        Log.d(TAG, "=== initiateCall() started ===");
        Log.d(TAG, "Current username: " + currentUsername);
        Log.d(TAG, "Target userId: " + userId);
        Log.d(TAG, "Is incoming: " + isIncoming);

        // First, login to MainRepository with WebRTC setup
        if (currentUsername == null || currentUsername.isEmpty()) {
            Log.e(TAG, "ERROR: User not authenticated");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Login to WebRTC signaling first
        Log.d(TAG, "Attempting to login to WebRTC signaling...");
        mainRepository.login(currentUsername, this, () -> {
            Log.d(TAG, "✅ Logged in to WebRTC signaling successfully");

            // Setup WebRTC views after login (when webRTCClient is created)
            Log.d(TAG, "Setting up WebRTC views...");
            setupWebRTC();
            Log.d(TAG, "✅ WebRTC views setup complete");

            // Start foreground service for call
            Log.d(TAG, "Starting CallService...");
            Intent serviceIntent = new Intent(VideoCallActivity.this, CallService.class);
            serviceIntent.setAction("START_CALL");
            serviceIntent.putExtra("caller_name", userName);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d(TAG, "✅ CallService started");

            // Now subscribe to incoming events (after login)
            Log.d(TAG, "Subscribing to latest events...");
            mainRepository.subscribeForLatestEvent(model -> {
                Log.d(TAG, "📩 Incoming call event: " + model.getType() + " from: " + model.getSender());

                // For outgoing calls, when we receive ANY WebRTC signal from receiver,
                // it means they accepted - start our WebRTC connection
                if (!isIncoming && model.getSender() != null && model.getSender().equals(userId)) {
                    Log.d(TAG, "🔍 Checking signal from receiver...");
                    Log.d(TAG, "Signal type: " + model.getType());
                    Log.d(TAG, "Is ringing: " + isRinging);

                    // If we receive Offer, Answer, or IceCandidate from the receiver, they accepted
                    if (model.getType() == com.example.project_ez_talk.webTRC.DataModelType.Offer ||
                            model.getType() == com.example.project_ez_talk.webTRC.DataModelType.Answer ||
                            model.getType() == com.example.project_ez_talk.webTRC.DataModelType.IceCandidate) {
                        Log.d(TAG, "🎉 Receiver sent WebRTC signal! Starting connection on caller side...");

                        // Only start once
                        if (isRinging) {
                            isRinging = false;
                            tvCallStatus.setText("Connecting...");
                            Log.d(TAG, "Calling mainRepository.startCall() for user: " + userId);
                            mainRepository.startCall(userId);
                            Log.d(TAG, "✅ WebRTC connection started on caller side");
                        } else {
                            Log.d(TAG, "⚠️ Already started WebRTC, skipping...");
                        }
                    }
                }

                // If this is a new incoming StartCall event and we're in an outgoing call
                // Don't handle it here - this should go to HomeActivity's listener
                if (model.getType() == com.example.project_ez_talk.webTRC.DataModelType.StartCall) {
                    Log.d(TAG, "⚠️ StartCall event received while VideoCallActivity is active - ignoring");
                }
            });
            Log.d(TAG, "✅ Subscribed to events");

            // If not incoming call, initiate the call
            if (!isIncoming) {
                Log.d(TAG, "📞 Initiating outgoing call to: " + userId);
                isRinging = true;
                tvCallStatus.setText("Calling...");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d(TAG, "Sending call request to userId: " + userId);
                    mainRepository.sendCallRequest(userId, () -> {
                        Log.e(TAG, "❌ ERROR: Failed to send call request to " + userId);
                        Toast.makeText(VideoCallActivity.this, "Failed to send call request", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    Log.d(TAG, "✅ Call request sent, waiting for receiver to accept...");
                    // DON'T start WebRTC yet - wait for receiver to accept
                    // The receiver will accept and send back WebRTC signals
                    // Our event listener will detect those signals and establish connection
                }, 1000);
            } else {
                Log.d(TAG, "📲 Answering incoming call from: " + userId);
                // Answer incoming call - receiver starts the WebRTC connection
                tvCallStatus.setText("Connecting...");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d(TAG, "===== RECEIVER STARTING WEBRTC =====");
                    Log.d(TAG, "Receiver user: " + currentUsername);
                    Log.d(TAG, "Calling user (target): " + userId);
                    Log.d(TAG, "This will create an OFFER and send to: " + userId);
                    mainRepository.startCall(userId);
                    Log.d(TAG, "✅ WebRTC answer call started - offer should be sent now");
                }, 500);
            }
        });
    }

    private void setupWebRTC() {
        // Note: localView and remoteView should be SurfaceViewRenderer from your layout
        // If they don't exist in your layout, you'll need to add them
        // For now, we'll try to find them by ID (you may need to add these to your XML)
        try {
            localView = findViewById(R.id.localView);
            remoteView = findViewById(R.id.remoteView);

            if (localView != null && remoteView != null) {
                mainRepository.initLocalView(localView);
                mainRepository.initRemoteView(remoteView);
            } else {
                Log.e(TAG, "SurfaceViewRenderers not found in layout. Please add them to activity_video_call.xml");
                Toast.makeText(this, "Video views not properly configured", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up WebRTC views: " + e.getMessage());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);

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