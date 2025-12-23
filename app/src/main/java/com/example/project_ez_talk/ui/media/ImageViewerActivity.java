package com.example.project_ez_talk.ui.media;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;

public class ImageViewerActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_SENDER_NAME = "sender_name";
    public static final String EXTRA_TIMESTAMP = "timestamp";
    public static final String EXTRA_CAPTION = "caption";

    private ImageView photoView;
    private LinearLayout topBar;
    private ImageView btnBack, btnShare, btnMore;
    private TextView tvSenderName, tvTimestamp, tvCaption;
    private CardView cvCaption;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        initViews();
        setupListeners();
        loadImage();
    }

    private void initViews() {
        photoView = findViewById(R.id.photoView);
        topBar = findViewById(R.id.topBar);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnMore = findViewById(R.id.btnMore);
        tvSenderName = findViewById(R.id.tvSenderName);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvCaption = findViewById(R.id.tvCaption);
        cvCaption = findViewById(R.id.cvCaption);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            // TODO: Share image
            Toast.makeText(this, "Share image", Toast.LENGTH_SHORT).show();
        });

        btnMore.setOnClickListener(v -> {
            // TODO: Show more options (download, delete, etc.)
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show();
        });

        // Hide/show top bar on image click
        photoView.setOnClickListener(v -> {
            if (topBar.getVisibility() == View.VISIBLE) {
                topBar.setVisibility(View.GONE);
                cvCaption.setVisibility(View.GONE);
            } else {
                topBar.setVisibility(View.VISIBLE);
                if (tvCaption.getText().length() > 0) {
                    cvCaption.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadImage() {
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        String senderName = getIntent().getStringExtra(EXTRA_SENDER_NAME);
        String timestamp = getIntent().getStringExtra(EXTRA_TIMESTAMP);
        String caption = getIntent().getStringExtra(EXTRA_CAPTION);

        // Set sender info
        if (senderName != null) tvSenderName.setText(senderName);
        if (timestamp != null) tvTimestamp.setText(timestamp);

        // Set caption if exists
        if (caption != null && !caption.isEmpty()) {
            tvCaption.setText(caption);
            cvCaption.setVisibility(View.VISIBLE);
        } else {
            cvCaption.setVisibility(View.GONE);
        }

        // Load image
        if (imageUrl != null) {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .into(photoView);
            progressBar.setVisibility(View.GONE);
        }
    }
}