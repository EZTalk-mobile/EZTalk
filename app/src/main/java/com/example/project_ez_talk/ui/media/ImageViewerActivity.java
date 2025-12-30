package com.example.project_ez_talk.ui.media;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageViewerActivity extends BaseActivity {

    private static final String TAG = "ImageViewerActivity";

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_SENDER_NAME = "sender_name";
    public static final String EXTRA_TIMESTAMP = "timestamp";
    public static final String EXTRA_CAPTION = "caption";

    private PhotoView photoView;
    private LinearLayout topBar;
    private ImageView btnBack, btnDownload, btnShare;
    private TextView tvSenderName, tvTimestamp, tvCaption;
    private CardView cvCaption;
    private ProgressBar progressBar;

    private String imageUrl;
    private boolean isUiVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // Hide system UI for immersive experience
        hideSystemUI();

        initViews();
        setupListeners();
        loadImage();
    }

    private void hideSystemUI() {
        try {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } catch (Exception e) {
            Log.e(TAG, "Error hiding system UI: " + e.getMessage());
        }
    }

    private void initViews() {
        photoView = findViewById(R.id.photoView);
        topBar = findViewById(R.id.topBar);
        btnBack = findViewById(R.id.btnBack);
        btnDownload = findViewById(R.id.btnDownload);
        btnShare = findViewById(R.id.btnShare);
        tvSenderName = findViewById(R.id.tvSenderName);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvCaption = findViewById(R.id.tvCaption);
        cvCaption = findViewById(R.id.cvCaption);
        progressBar = findViewById(R.id.progressBar);

        // Log which views are null for debugging
        if (photoView == null) Log.e(TAG, "❌ photoView is NULL");
        if (topBar == null) Log.e(TAG, "❌ topBar is NULL");
        if (btnBack == null) Log.e(TAG, "❌ btnBack is NULL");
        if (btnDownload == null) Log.e(TAG, "❌ btnDownload is NULL");
        if (btnShare == null) Log.e(TAG, "❌ btnShare is NULL");
        if (tvSenderName == null) Log.e(TAG, "❌ tvSenderName is NULL");
        if (tvTimestamp == null) Log.e(TAG, "❌ tvTimestamp is NULL");
        if (tvCaption == null) Log.e(TAG, "❌ tvCaption is NULL");
        if (cvCaption == null) Log.e(TAG, "❌ cvCaption is NULL");
        if (progressBar == null) Log.e(TAG, "❌ progressBar is NULL");
    }

    private void setupListeners() {
        // ✅ Add null checks for all buttons
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "❌ btnBack is NULL - cannot set click listener");
        }

        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> downloadImage());
        } else {
            Log.e(TAG, "❌ btnDownload is NULL - cannot set click listener");
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> shareImage());
        } else {
            Log.e(TAG, "❌ btnShare is NULL - cannot set click listener");
        }

        // Toggle UI visibility on photo click
        if (photoView != null) {
            photoView.setOnClickListener(v -> toggleUI());
        } else {
            Log.e(TAG, "❌ photoView is NULL - cannot set click listener");
        }
    }

    private void loadImage() {
        imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        String senderName = getIntent().getStringExtra(EXTRA_SENDER_NAME);
        String timestamp = getIntent().getStringExtra(EXTRA_TIMESTAMP);
        String caption = getIntent().getStringExtra(EXTRA_CAPTION);

        Log.d(TAG, "📷 Loading image: " + imageUrl);

        // Set sender info
        if (tvSenderName != null) {
            if (senderName != null && !senderName.isEmpty()) {
                tvSenderName.setText(senderName);
            } else {
                tvSenderName.setText("Unknown");
            }
        }

        if (tvTimestamp != null) {
            if (timestamp != null && !timestamp.isEmpty()) {
                tvTimestamp.setText(timestamp);
            } else {
                tvTimestamp.setVisibility(View.GONE);
            }
        }

        // Set caption if exists
        if (tvCaption != null && cvCaption != null) {
            if (caption != null && !caption.isEmpty()) {
                tvCaption.setText(caption);
                cvCaption.setVisibility(View.VISIBLE);
            } else {
                cvCaption.setVisibility(View.GONE);
            }
        }

        // Load image with PhotoView for zoom/pan
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            if (photoView != null) {
                Glide.with(this)
                        .load(imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(photoView);

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                Log.e(TAG, "❌ photoView is NULL - cannot load image");
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void toggleUI() {
        if (isUiVisible) {
            // Hide UI
            if (topBar != null) {
                topBar.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> topBar.setVisibility(View.GONE));
            }

            if (cvCaption != null) {
                cvCaption.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> cvCaption.setVisibility(View.GONE));
            }

            isUiVisible = false;
        } else {
            // Show UI
            if (topBar != null) {
                topBar.setVisibility(View.VISIBLE);
                topBar.animate()
                        .alpha(1f)
                        .setDuration(300);
            }

            if (cvCaption != null && tvCaption != null && tvCaption.getText().length() > 0) {
                cvCaption.setVisibility(View.VISIBLE);
                cvCaption.animate()
                        .alpha(1f)
                        .setDuration(300);
            }

            isUiVisible = true;
        }
    }

    private void downloadImage() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
            request.setTitle("Downloading Image");
            request.setDescription("Downloading image from EZ Talk");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "EZTalk_" + System.currentTimeMillis() + ".jpg");

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this image: " + imageUrl);
            startActivity(Intent.createChooser(shareIntent, "Share image via"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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