package com.example.project_ez_talk.ui.call.ongoing;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CallActivity extends BaseActivity {

    // Constants for intent extras
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_AVATAR = "user_avatar";
    public static final String EXTRA_IS_INCOMING = "is_incoming";

    private TextView tvCallDuration, tvCallerName, tvCallStatus;
    private ImageView ivCallerAvatar;
    private FloatingActionButton fabMute;
    private FloatingActionButton fabSpeaker;

    private String userName;
    private String userAvatar;
    private boolean isIncoming;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int seconds = 0;
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;

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
        setContentView(R.layout.activity_call);

        // Get extras from intent
        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        userAvatar = getIntent().getStringExtra(EXTRA_USER_AVATAR);
        isIncoming = getIntent().getBooleanExtra(EXTRA_IS_INCOMING, false);

        initViews();
        loadCallerInfo();
        setupClickListeners();

        // Start call timer after a short delay to simulate connection
        new Handler(Looper.getMainLooper()).postDelayed(this::startCallTimer, 1000);
    }

    private void initViews() {
        tvCallDuration = findViewById(R.id.tvCallDuration);
        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        ivCallerAvatar = findViewById(R.id.ivCallerAvatar);

        // Get the LinearLayouts
        LinearLayout btnMuteLayout = findViewById(R.id.btnMute);
        LinearLayout btnEndCallLayout = findViewById(R.id.btnEndCall);
        LinearLayout btnSpeakerLayout = findViewById(R.id.btnSpeaker);

        // Get the FABs which are the first child (index 0) of each LinearLayout
        fabMute = (FloatingActionButton) btnMuteLayout.getChildAt(0);
        FloatingActionButton fabEndCall = (FloatingActionButton) btnEndCallLayout.getChildAt(0);
        fabSpeaker = (FloatingActionButton) btnSpeakerLayout.getChildAt(0);
    }

    private void loadCallerInfo() {
        // Set caller name
        if (userName != null && !userName.isEmpty()) {
            tvCallerName.setText(userName);
        } else {
            tvCallerName.setText("Unknown Caller");
        }

        // Load caller avatar
        if (userAvatar != null && !userAvatar.isEmpty()) {
            Glide.with(this)
                    .load(userAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(ivCallerAvatar);
        }

        // Set initial call status
        tvCallStatus.setText(isIncoming ? "Incoming call..." : "Calling...");
    }

    private void setupClickListeners() {
        findViewById(R.id.btnMute).setOnClickListener(v -> toggleMute());
        findViewById(R.id.btnSpeaker).setOnClickListener(v -> toggleSpeaker());
        findViewById(R.id.btnEndCall).setOnClickListener(v -> endCall());
    }

    private void toggleMute() {
        isMuted = !isMuted;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMicrophoneMute(isMuted);
        }
        fabMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        Toast.makeText(this, isMuted ? "Microphone muted" : "Microphone unmuted", Toast.LENGTH_SHORT).show();
    }

    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(isSpeakerOn);
        }
        fabSpeaker.setImageResource(isSpeakerOn ? R.drawable.ic_speaker : R.drawable.ic_speaker);
        Toast.makeText(this, isSpeakerOn ? "Speaker on" : "Speaker off", Toast.LENGTH_SHORT).show();
    }

    private void endCall() {
        timerHandler.removeCallbacks(timerRunnable);
        Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startCallTimer() {
        seconds = 0;
        tvCallStatus.setText("Connected");
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

        // Turn off speaker if it was on
        if (isSpeakerOn) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setSpeakerphoneOn(false);
            }
        }
    }
}