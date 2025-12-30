package com.example.project_ez_talk.ui.call.video;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class VideoCallActivity extends BaseActivity {

    // Constants matching CallActivity
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_AVATAR = "user_avatar";
    public static final String EXTRA_IS_INCOMING = "is_incoming";

    private TextView tvCallDuration, tvRemoteName, tvCallStatus;
    private ImageView ivRemoteAvatar, ivLocalAvatar;
    private FloatingActionButton fabMic, fabVideo, fabEndCall, fabSwitchCamera;
    private View cvCallDuration;

    private String userId;
    private String userName;
    private String userAvatar;
    private boolean isIncoming;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int seconds = 0;
    private boolean isMuted = false;
    private boolean isVideoOn = true;
    private boolean isCallConnected = false;

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

        initViews();
        setupClickListeners();
        loadUserInfo();

        // Simulate connection after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(this::onCallConnected, 2000);
    }

    @SuppressLint("WrongViewCast")
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
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMicrophoneMute(isMuted);
        }
        fabMic.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        Toast.makeText(this, isMuted ? "Microphone muted" : "Microphone unmuted", Toast.LENGTH_SHORT).show();
    }

    private void toggleVideo() {
        isVideoOn = !isVideoOn;
        fabVideo.setImageResource(isVideoOn ? R.drawable.ic_video : R.drawable.ic_video_off);

        // Show/hide local video preview
        ivLocalAvatar.setVisibility(isVideoOn ? View.VISIBLE : View.GONE);

        Toast.makeText(this, isVideoOn ? "Camera on" : "Camera off", Toast.LENGTH_SHORT).show();
        // TODO: Implement actual camera toggle logic
    }

    private void switchCamera() {
        Toast.makeText(this, "Camera switched", Toast.LENGTH_SHORT).show();
        // TODO: Implement camera switching logic (front/back)
    }

    private void endCall() {
        timerHandler.removeCallbacks(timerRunnable);
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

        // Unmute mic if it was muted
        if (isMuted) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setMicrophoneMute(false);
            }
        }
    }
}