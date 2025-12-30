package com.example.project_ez_talk.ui.channel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateChannelActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private Toolbar toolbar;
    private MaterialCardView cvChannelIcon;
    private ImageView ivChannelIcon;
    private EditText etChannelName, etChannelDescription;
    private RadioGroup rgChannelType;
    private RadioButton radioPublic, radioPrivate;
    private TextView tvChannelTypeDesc;
    private Button btnCreateChannel;

    private Uri channelImageUri;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_channel);

        initFirebase();
        initViews();
        setupListeners();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("channel_images");
    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cvChannelIcon = findViewById(R.id.cvChannelIcon);
        ivChannelIcon = findViewById(R.id.ivChannelIcon);
        etChannelName = findViewById(R.id.etChannelName);
        etChannelDescription = findViewById(R.id.etChannelDescription);
        rgChannelType = findViewById(R.id.rgChannelType);
        radioPublic = findViewById(R.id.radioPublic);
        radioPrivate = findViewById(R.id.radioPrivate);
        tvChannelTypeDesc = findViewById(R.id.tvChannelTypeDesc);
        btnCreateChannel = findViewById(R.id.btnCreateChannel);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        radioPublic.setChecked(true);
        tvChannelTypeDesc.setText("Anyone can find and join this channel");
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        cvChannelIcon.setOnClickListener(v -> selectChannelImage());
        ivChannelIcon.setOnClickListener(v -> selectChannelImage());

        rgChannelType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioPublic) {
                tvChannelTypeDesc.setText("Anyone can find and join this channel");
            } else {
                tvChannelTypeDesc.setText("Only invited members can join this channel");
            }
        });

        btnCreateChannel.setOnClickListener(v -> createChannel());
    }

    private void selectChannelImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void createChannel() {
        String channelName = etChannelName.getText().toString().trim();
        String channelDesc = etChannelDescription.getText().toString().trim();
        boolean isPublic = radioPublic.isChecked();

        if (TextUtils.isEmpty(channelName)) {
            etChannelName.setError("Enter channel name");
            etChannelName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(channelDesc)) {
            etChannelDescription.setError("Enter channel description");
            etChannelDescription.requestFocus();
            return;
        }

        btnCreateChannel.setEnabled(false);
        btnCreateChannel.setText("Creating...");

        String channelId = UUID.randomUUID().toString();
        String currentUserId = auth.getCurrentUser().getUid();

        if (channelImageUri != null) {
            uploadChannelImageAndCreate(channelId, channelName, channelDesc, isPublic, currentUserId);
        } else {
            createChannelInDatabase(channelId, channelName, channelDesc, isPublic, null, currentUserId);
        }
    }

    private void uploadChannelImageAndCreate(String channelId, String channelName, String channelDesc, boolean isPublic, String currentUserId) {
        StorageReference imageRef = storageRef.child(channelId + "_" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(channelImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                createChannelInDatabase(channelId, channelName, channelDesc, isPublic, uri.toString(), currentUserId)
                        )
                )
                .addOnFailureListener(e -> {
                    resetCreateButton();
                    Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createChannelInDatabase(String channelId, String channelName, String channelDesc, boolean isPublic, String imageUrl, String currentUserId) {
        Map<String, Object> channelData = new HashMap<>();
        channelData.put("id", channelId);
        channelData.put("name", channelName);
        channelData.put("description", channelDesc);
        channelData.put("avatarUrl", imageUrl != null ? imageUrl : "");
        channelData.put("ownerId", currentUserId);
        channelData.put("createdAt", System.currentTimeMillis());
        channelData.put("isPublic", isPublic);
        channelData.put("subscriberCount", 1);
        channelData.put("lastMessage", "");
        channelData.put("lastMessageTimestamp", System.currentTimeMillis());

        // Admins and subscribers
        Map<String, Boolean> admins = new HashMap<>();
        admins.put(currentUserId, true);
        channelData.put("admins", admins);

        Map<String, Boolean> subscribers = new HashMap<>();
        subscribers.put(currentUserId, true);
        channelData.put("subscribers", subscribers);

        db.collection("channels")
                .document(channelId)
                .set(channelData)
                .addOnSuccessListener(unused -> addChannelToUserList(channelId, channelName, imageUrl, currentUserId))
                .addOnFailureListener(e -> {
                    resetCreateButton();
                    Toast.makeText(this, "Failed to create channel: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addChannelToUserList(String channelId, String channelName, String imageUrl, String currentUserId) {
        Map<String, Object> userChannelData = new HashMap<>();
        userChannelData.put("channelId", channelId);
        userChannelData.put("name", channelName);
        userChannelData.put("avatar", imageUrl != null ? imageUrl : "");
        userChannelData.put("role", "admin");
        userChannelData.put("addedAt", System.currentTimeMillis());

        db.collection("users")
                .document(currentUserId)
                .collection("channels")
                .document(channelId)
                .set(userChannelData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Channel created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    resetCreateButton();
                    Toast.makeText(this, "Channel created, but failed to save locally: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void resetCreateButton() {
        btnCreateChannel.setEnabled(true);
        btnCreateChannel.setText("Create Channel");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null && requestCode == PICK_IMAGE_REQUEST) {
            channelImageUri = data.getData();
            Glide.with(this).load(channelImageUri).centerCrop().into(ivChannelIcon);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}