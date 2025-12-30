package com.example.project_ez_talk.ui.chat.detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.MessageAdapter;
import com.example.project_ez_talk.model.Message;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.video.VideoCallActivity;
import com.example.project_ez_talk.webTRC.FirebaseClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class ChatDetailActivity extends BaseActivity {

    private static final String TAG = "ChatDetailActivity";

    // UI Views
    private Toolbar toolbar;
    private ImageView ivUserAvatar;
    private TextView tvUserName;
    private TextView tvUserStatus;
    private ImageView btnBack;
    private ImageView btnVoiceCall;
    private ImageView btnVideoCall;
    private ImageView btnMore;

    private RecyclerView rvMessages;
    private EditText etMessage;
    private FloatingActionButton fabSend;
    private FloatingActionButton fabVoice;
    private ImageView btnEmoji;
    private ImageView btnAttach;
    private CardView voiceRecordingOverlay;
    private TextView tvRecordingTime;

    // Adapters and Data
    private MessageAdapter messageAdapter;

    // Firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String chatId;
    private CollectionReference messagesRef;
    private ListenerRegistration listener;

    // Other user info
    private String receiverId;
    private String receiverName;
    private String receiverAvatar;

    // Current user info
    private String currentUserName;
    private String currentUserAvatar;

    // Media handling
    private Uri imageUri;
    FirebaseClient firebaseClient = new FirebaseClient();


    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadImage();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && imageUri != null) {
                    uploadImage();
                }
            });

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean cameraGranted = Boolean.TRUE.equals(permissions.get(Manifest.permission.CAMERA));
                boolean storageGranted = Boolean.TRUE.equals(permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE));

                if (cameraGranted && storageGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get receiver info from intent
        receiverId = getIntent().getStringExtra("user_id");
        receiverName = getIntent().getStringExtra("user_name");
        receiverAvatar = getIntent().getStringExtra("user_avatar");

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();
        setupToolbar();
        setupRecyclerView();
        generateChatId();

        // Fetch current user info and start loading messages
        fetchCurrentUserInfo();
        setupMessageInput();
        setupClickListeners();

    }
public void onClickVideoCall (){
    PermissionX.init(this)
            .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
            .request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    // Navigate to VideoCallActivity (it will handle the login)
                    Intent intent = new Intent(ChatDetailActivity.this, VideoCallActivity.class);
                    intent.putExtra(VideoCallActivity.EXTRA_USER_ID, receiverId);
                    intent.putExtra(VideoCallActivity.EXTRA_USER_NAME, receiverName);
                    intent.putExtra(VideoCallActivity.EXTRA_USER_AVATAR, receiverAvatar);
                    intent.putExtra(VideoCallActivity.EXTRA_IS_INCOMING, false);
                    startActivity(intent);
                    Toast.makeText(this, "Starting video call...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Camera and microphone permissions are required for video calls", Toast.LENGTH_LONG).show();
                }
            });
}


    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        btnBack = findViewById(R.id.btnBack);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);
        btnVideoCall = findViewById(R.id.btnVideoCall);
        btnVideoCall.setOnClickListener(v -> onClickVideoCall());
        btnMore = findViewById(R.id.btnMore);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        fabSend = findViewById(R.id.fabSend);
        fabVoice = findViewById(R.id.fabVoice);
        btnEmoji = findViewById(R.id.btnEmoji);
        btnAttach = findViewById(R.id.btnAttach);
        voiceRecordingOverlay = findViewById(R.id.voiceRecordingOverlay);
        tvRecordingTime = findViewById(R.id.tvRecordingTime);

        // Hide send button initially
        fabSend.setVisibility(View.GONE);
        fabVoice.setVisibility(View.VISIBLE);
    }

    /**
     * ✅ Fetch current user's name and avatar from Firestore
     */
    private void fetchCurrentUserInfo() {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("name");
                        currentUserAvatar = documentSnapshot.getString("avatarUrl");
                        Log.d(TAG, "Current user info loaded: " + currentUserName);
                    } else {
                        currentUserName = currentUser.getEmail() != null ? currentUser.getEmail() : "User";
                        currentUserAvatar = "";
                        Log.d(TAG, "User document doesn't exist, using email");
                    }

                    // Create chat document and start loading messages
                    createInitialChatDocument();
                    loadRealTimeMessages();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user info: " + e.getMessage());
                    currentUserName = currentUser.getEmail() != null ? currentUser.getEmail() : "User";
                    currentUserAvatar = "";

                    // Continue anyway
                    createInitialChatDocument();
                    loadRealTimeMessages();
                });
    }

    @SuppressLint("SetTextI18n")
    private void setupToolbar() {
        if (tvUserName != null) {
            tvUserName.setText(receiverName != null && !receiverName.isEmpty() ? receiverName : "User");
        }

        if (ivUserAvatar != null && receiverAvatar != null && !receiverAvatar.isEmpty()) {
            Glide.with(this)
                    .load(receiverAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivUserAvatar);
        } else if (ivUserAvatar != null) {
            ivUserAvatar.setImageResource(R.drawable.ic_profile);
        }

        if (tvUserStatus != null) {
            tvUserStatus.setText("Online");
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(new ArrayList<>(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    /**
     * ✅ Generate unique chat ID from both user IDs
     */
    private void generateChatId() {
        String id1 = currentUser.getUid();
        String id2 = receiverId;
        chatId = id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
        Log.d(TAG, "Generated Chat ID: " + chatId);
    }

    /**
     * ✅ Create initial chat document with participants array
     */
    private void createInitialChatDocument() {
        long timestamp = System.currentTimeMillis();

        // Shared chat document with participants
        Map<String, Object> sharedChatData = new HashMap<>();
        sharedChatData.put("id", chatId);
        sharedChatData.put("participants", Arrays.asList(currentUser.getUid(), receiverId));
        sharedChatData.put("createdAt", timestamp);

        db.collection("chats")
                .document(chatId)
                .set(sharedChatData, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Error creating shared chat: " + e.getMessage()));

        // Current user's chat list entry (shows receiver's info)
        Map<String, Object> chatDataCurrent = new HashMap<>();
        chatDataCurrent.put("id", chatId);
        chatDataCurrent.put("name", receiverName != null ? receiverName : "Unknown");
        chatDataCurrent.put("avatarUrl", receiverAvatar != null ? receiverAvatar : "");
        chatDataCurrent.put("lastMessage", "");
        chatDataCurrent.put("lastMessageTimestamp", timestamp);
        chatDataCurrent.put("unreadCount", 0);

        // Receiver's chat list entry (shows current user's info)
        Map<String, Object> chatDataReceiver = new HashMap<>();
        chatDataReceiver.put("id", chatId);
        chatDataReceiver.put("name", currentUserName != null ? currentUserName : "Unknown");
        chatDataReceiver.put("avatarUrl", currentUserAvatar != null ? currentUserAvatar : "");
        chatDataReceiver.put("lastMessage", "");
        chatDataReceiver.put("lastMessageTimestamp", timestamp);
        chatDataReceiver.put("unreadCount", 0);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("chats")
                .document(chatId)
                .set(chatDataCurrent)
                .addOnFailureListener(e -> Log.e(TAG, "Error creating current user's chat: " + e.getMessage()));

        db.collection("users")
                .document(receiverId)
                .collection("chats")
                .document(chatId)
                .set(chatDataReceiver)
                .addOnFailureListener(e -> Log.e(TAG, "Error creating receiver's chat: " + e.getMessage()));

        Log.d(TAG, "Chat document created");
    }

    /**
     * ✅ Load real-time messages
     */
    private void loadRealTimeMessages() {
        Log.d(TAG, "Loading messages for chatId: " + chatId);

        messagesRef = db.collection("chats")
                .document(chatId)
                .collection("messages");

        listener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore Error: " + e.getMessage());
                        Toast.makeText(ChatDetailActivity.this,
                                "Error loading messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null) {
                        Log.d(TAG, "No messages yet");
                        return;
                    }

                    Log.d(TAG, "Messages loaded: " + snapshots.size());

                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                message.setMessageId(doc.getId());
                                messages.add(message);
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing message: " + ex.getMessage());
                        }
                    }

                    messageAdapter.setMessages(messages);

                    // Scroll to latest message
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void setupMessageInput() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;
                fabSend.setVisibility(hasText ? View.VISIBLE : View.GONE);
                fabVoice.setVisibility(hasText ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        fabSend.setOnClickListener(v -> sendTextMessage());
        fabVoice.setOnClickListener(v -> Toast.makeText(this, "Voice recording coming soon", Toast.LENGTH_SHORT).show());
        btnAttach.setOnClickListener(v -> showAttachmentBottomSheet());
        btnEmoji.setOnClickListener(v -> Toast.makeText(this, "Emoji picker coming soon", Toast.LENGTH_SHORT).show());
        btnVoiceCall.setOnClickListener(v -> Toast.makeText(this, "Voice call coming soon", Toast.LENGTH_SHORT).show());

        btnMore.setOnClickListener(v -> Toast.makeText(this, "More options coming soon", Toast.LENGTH_SHORT).show());
    }

    /**
     * ✅ Send text message with sender info
     */
    private void sendTextMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                messageText,
                Message.MessageType.TEXT
        );

        // Add sender info
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "Message sent successfully");
                    etMessage.setText("");
                    updateChatList(messageText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send message: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ✅ Update chat list for both users
     */
    private void updateChatList(String messagePreview) {
        long timestamp = System.currentTimeMillis();

        // Current user's entry
        Map<String, Object> chatDataCurrent = new HashMap<>();
        chatDataCurrent.put("lastMessage", messagePreview);
        chatDataCurrent.put("lastMessageTimestamp", timestamp);

        // Receiver's entry
        Map<String, Object> chatDataReceiver = new HashMap<>();
        chatDataReceiver.put("lastMessage", messagePreview);
        chatDataReceiver.put("lastMessageTimestamp", timestamp);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("chats")
                .document(chatId)
                .update(chatDataCurrent);

        db.collection("users")
                .document(receiverId)
                .collection("chats")
                .document(chatId)
                .update(chatDataReceiver);
    }

    private void showAttachmentBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_attachment);

        View llCamera = dialog.findViewById(R.id.llCamera);
        View llGallery = dialog.findViewById(R.id.llGallery);

        if (llCamera != null) {
            llCamera.setOnClickListener(v -> {
                openCameraWithPermission();
                dialog.dismiss();
            });
        }

        if (llGallery != null) {
            llGallery.setOnClickListener(v -> {
                openGallery();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);

        if (imageUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        if (imageUri == null) return;

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("chat_images")
                .child(chatId)
                .child(System.currentTimeMillis() + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        sendImageMessage(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upload failed: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ✅ Send image message with sender info
     */
    private void sendImageMessage(String imageUrl) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                "",
                Message.MessageType.IMAGE
        );
        message.setFileUrl(imageUrl);
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "Image sent successfully");
                    updateChatList("[Image]");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send image: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
            Log.d(TAG, "Listener removed");
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }
}