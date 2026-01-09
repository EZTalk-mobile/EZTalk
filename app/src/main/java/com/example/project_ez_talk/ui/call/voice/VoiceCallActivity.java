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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
