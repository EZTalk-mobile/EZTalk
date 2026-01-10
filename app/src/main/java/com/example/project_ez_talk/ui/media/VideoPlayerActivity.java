package com.example.project_ez_talk.ui.media;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.cardview.widget.CardView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;

public class VideoPlayerActivity extends BaseActivity {

    public static final String EXTRA_VIDEO_URL = "video_url";
    public static final String EXTRA_SENDER_NAME = "sender_name";
    public static final String EXTRA_TIMESTAMP = "timestamp";
    public static final String EXTRA_CAPTION = "caption";

    private VideoView videoView;
    private LinearLayout topBar;
    private ImageView btnBack, btnDownload, btnShare;
    private TextView tvSenderName, tvTimestamp, tvCaption;
    private CardView cvCaption;
    private ProgressBar progressBar;

    private String videoUrl;
    private boolean isUiVisible = true;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Hide system UI for immersive experience
        hideSystemUI();

        initViews();
        setupListeners();
        loadVideo();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void initViews() {
        videoView = findViewById(R.id.videoView);
        topBar = findViewById(R.id.topBar);
        btnBack = findViewById(R.id.btnBack);
        btnDownload = findViewById(R.id.btnDownload);
        btnShare = findViewById(R.id.btnShare);
        tvSenderName = findViewById(R.id.tvSenderName);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvCaption = findViewById(R.id.tvCaption);
        cvCaption = findViewById(R.id.cvCaption);
        progressBar = findViewById(R.id.progressBar);

        // Setup media controller
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDownload.setOnClickListener(v -> downloadVideo());

        btnShare.setOnClickListener(v -> shareVideo());

        // Toggle UI visibility on video click
        videoView.setOnClickListener(v -> toggleUI());

        videoView.setOnPreparedListener(mp -> {
            progressBar.setVisibility(View.GONE);
            mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
                videoView.setMediaController(mediaController);
                mediaController.setAnchorView(videoView);
            });
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(VideoPlayerActivity.this, "Error playing video", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void loadVideo() {
        videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        String senderName = getIntent().getStringExtra(EXTRA_SENDER_NAME);
        String timestamp = getIntent().getStringExtra(EXTRA_TIMESTAMP);
        String caption = getIntent().getStringExtra(EXTRA_CAPTION);

        // Set sender info
        if (senderName != null && !senderName.isEmpty()) {
            tvSenderName.setText(senderName);
        } else {
            tvSenderName.setText("Unknown");
        }

        if (timestamp != null && !timestamp.isEmpty()) {
            tvTimestamp.setText(timestamp);
        } else {
            tvTimestamp.setVisibility(View.GONE);
        }

        // Set caption if exists
        if (caption != null && !caption.isEmpty()) {
            tvCaption.setText(caption);
            cvCaption.setVisibility(View.VISIBLE);
        } else {
            cvCaption.setVisibility(View.GONE);
        }

        // Load and play video
        if (videoUrl != null && !videoUrl.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);

            Uri videoUri = Uri.parse(videoUrl);
            videoView.setVideoURI(videoUri);
            videoView.requestFocus();
            videoView.start();
        } else {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void toggleUI() {
        if (isUiVisible) {
            // Hide UI
            topBar.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> topBar.setVisibility(View.GONE));

            cvCaption.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> cvCaption.setVisibility(View.GONE));

            isUiVisible = false;
        } else {
            // Show UI
            topBar.setVisibility(View.VISIBLE);
            topBar.animate()
                    .alpha(1f)
                    .setDuration(300);

            if (tvCaption.getText().length() > 0) {
                cvCaption.setVisibility(View.VISIBLE);
                cvCaption.animate()
                        .alpha(1f)
                        .setDuration(300);
            }

            isUiVisible = true;
        }
    }

    private void downloadVideo() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
            request.setTitle("Downloading Video");
            request.setDescription("Downloading video from EZ Talk");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "EZTalk_" + System.currentTimeMillis() + ".mp4");

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareVideo() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this video: " + videoUrl);
            startActivity(Intent.createChooser(shareIntent, "Share video via"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}