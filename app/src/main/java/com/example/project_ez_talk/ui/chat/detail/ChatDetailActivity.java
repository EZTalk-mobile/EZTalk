package com.example.project_ez_talk.ui.chat.detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.example.project_ez_talk.helper.SupabaseStorageManager;
import com.example.project_ez_talk.model.Message;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.video.VideoCallActivity;
import com.example.project_ez_talk.webTRC.FirebaseClient;

import java.io.IOException;
import java.io.InputStream;
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
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.recyclerview.widget.ItemTouchHelper;
import com.example.project_ez_talk.adapter.SwipeToDeleteCallback;
@SuppressWarnings("ALL")
public class ChatDetailActivity extends BaseActivity {

    private static final String TAG = "ChatDetailActivity";

    // Supabase Configuration
    private static final String SUPABASE_URL = "https://ijcfvpodwmshmdecmxmk.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlqY2Z2cG9kd21zaG1kZWNteG1rIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjYwNTM5NzQsImV4cCI6MjA4MTYyOTk3NH0.35y9_9TMIMEltfYRFs06oOPJwpIGEUHZQasXYkch3IQ";
    private static final String BUCKET_IMAGES = "chat-images";
    private static final String BUCKET_DOCUMENTS = "chat-documents";
    private static final String BUCKET_AUDIO = "chat-audio";

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
    private LocationManager locationManager;
    FirebaseClient firebaseClient = new FirebaseClient();


    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "📸 Gallery result received: resultCode=" + result.getResultCode());
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Log.d(TAG, "✅ Image selected from gallery: " + imageUri.toString());
                        uploadImage();
                    } else {
                        Log.e(TAG, "❌ imageUri is null even though result was OK");
                        Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "❌ Gallery selection cancelled or failed");
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "📷 Camera result received: resultCode=" + result.getResultCode());
                if (result.getResultCode() == RESULT_OK && imageUri != null) {
                    Log.d(TAG, "✅ Photo taken: " + imageUri.toString());
                    uploadImage();
                } else {
                    Log.d(TAG, "❌ Camera cancelled or imageUri is null");
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

    private final ActivityResultLauncher<Intent> documentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadDocumentToSupabase(fileUri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> audioLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
                        uploadAudioToSupabase(audioUri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> contactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    if (contactUri != null) {
                        handleContactSelection(contactUri);
                    }
                }
            });

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean fineLocationGranted = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
                    sendLocationMessage();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> galleryPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchGalleryPicker();
                } else {
                    Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Initialize Supabase Storage
        SupabaseStorageManager.init(this);

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
                        currentUserAvatar = documentSnapshot.getString("profilePicture");
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

        // for swipe to deletet

        //set the chat id
        messageAdapter.setCurrentChatId(chatId);
        SwipeToDeleteCallback swipeCallBack = new SwipeToDeleteCallback(this, messageAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallBack);
        itemTouchHelper.attachToRecyclerView(rvMessages);
        messageAdapter.setDeleteListener(message -> {
            Log.d(TAG, "lub jaol"+ message.getMessageId());
        });
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
        // ==================== SET CHAT TYPE AND ID FOR SWIPE DELETE ====================
        messageAdapter.setCurrentChatId(chatId);
        messageAdapter.setChatType("private");  // ✅ Important: Set chat type to "private" for 1-on-1 chats
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
        View llDocument = dialog.findViewById(R.id.llDocument);
        View llAudio = dialog.findViewById(R.id.llAudio);
        View llLocation = dialog.findViewById(R.id.llLocation);
        View llContact = dialog.findViewById(R.id.llContact);

        if (llCamera != null) llCamera.setOnClickListener(v -> { openCameraWithPermission(); dialog.dismiss(); });
        if (llGallery != null) llGallery.setOnClickListener(v -> { openGallery(); dialog.dismiss(); });
        if (llDocument != null) llDocument.setOnClickListener(v -> { openDocumentPicker(); dialog.dismiss(); });
        if (llAudio != null) llAudio.setOnClickListener(v -> { openAudioPicker(); dialog.dismiss(); });
        if (llLocation != null) llLocation.setOnClickListener(v -> { requestLocationPermission(); dialog.dismiss(); });
        if (llContact != null) llContact.setOnClickListener(v -> { openContactPicker(); dialog.dismiss(); });

        dialog.show();
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                launchGalleryPicker();
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                launchGalleryPicker();
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void launchGalleryPicker() {
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
        try {
            Log.d(TAG, "📷 Opening camera...");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new android.content.ContentValues()
            );

            if (imageUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Camera error: " + e.getMessage());
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== NETWORK CHECK ====================

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Network check error: " + e.getMessage());
            return true;
        }
    }

    // ==================== IMAGE HANDLING ====================

    private void uploadImage() {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) return;

                byte[] imageData = readBytes(inputStream);
                String fileName = "chat_" + chatId + "_" + System.currentTimeMillis() + ".jpg";
                String filePath = "chat_images/" + fileName;
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_IMAGES + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(imageData, MediaType.parse("image/jpeg"));

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(body)
                        .addHeader("Content-Type", "image/jpeg")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String imageUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_IMAGES + "/" + filePath;
                    runOnUiThread(() -> sendImageMessage(imageUrl));
                } else {
                    runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
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
                    Toast.makeText(this, "Image sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send image: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== DOCUMENT HANDLING ====================

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        documentLauncher.launch(intent);
    }

    private void uploadDocumentToSupabase(Uri documentUri) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading document...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(documentUri);
                if (inputStream == null) return;

                byte[] fileData = readBytes(inputStream);
                String fileName = getFileName(documentUri);
                String filePath = "documents/" + chatId + "_" + System.currentTimeMillis() + "_" + fileName;
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_DOCUMENTS + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(fileData, MediaType.parse("application/octet-stream"));

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(body)
                        .addHeader("Content-Type", "application/octet-stream")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String documentUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_DOCUMENTS + "/" + filePath;
                    runOnUiThread(() -> sendDocumentMessage(documentUrl, fileName));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendDocumentMessage(String documentUrl, String fileName) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                fileName,
                Message.MessageType.FILE
        );
        message.setFileUrl(documentUrl);
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "✅ Document sent");
                    updateChatList("[Document]");
                    Toast.makeText(this, "Document sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send document: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, "Failed to send document", Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== AUDIO HANDLING ====================

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        audioLauncher.launch(intent);
    }

    private void uploadAudioToSupabase(Uri audioUri) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading audio...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(audioUri);
                if (inputStream == null) return;

                byte[] audioData = readBytes(inputStream);
                String fileName = "audio_" + chatId + "_" + System.currentTimeMillis() + ".m4a";
                String filePath = "audio/" + fileName;
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_AUDIO + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(audioData, MediaType.parse("audio/mp4"));

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(body)
                        .addHeader("Content-Type", "audio/mp4")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String audioUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_AUDIO + "/" + filePath;
                    runOnUiThread(() -> sendAudioMessage(audioUrl));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendAudioMessage(String audioUrl) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                "",
                Message.MessageType.AUDIO
        );
        message.setFileUrl(audioUrl);
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "✅ Audio sent");
                    updateChatList("[Audio]");
                    Toast.makeText(this, "Audio sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send audio: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, "Failed to send audio", Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== LOCATION HANDLING ====================

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            sendLocationMessage();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void sendLocationMessage() {
        try {
            Location location = null;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (location == null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();

                Message message = new Message(
                        currentUser.getUid(),
                        receiverId,
                        latitude + "," + longitude,
                        Message.MessageType.LOCATION
                );
                message.setSenderName(currentUserName);
                message.setSenderAvatarUrl(currentUserAvatar);
                message.setTimestamp(System.currentTimeMillis());

                messagesRef.add(message)
                        .addOnSuccessListener(ref -> {
                            Log.d(TAG, "✅ Location sent");
                            updateChatList("[Location]");
                            Toast.makeText(this, "Location sent", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to send location: " + e.getMessage());
                            Toast.makeText(ChatDetailActivity.this, "Failed to send location", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Location error: " + e.getMessage());
            Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== CONTACT HANDLING ====================

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactLauncher.launch(intent);
    }

    @SuppressLint("Range")
    private void handleContactSelection(Uri contactUri) {
        try {
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null
                );

                String phoneNumber = "";
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneCursor.close();
                }

                sendContactMessage(contactName, phoneNumber);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading contact: " + e.getMessage());
            Toast.makeText(this, "Error reading contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendContactMessage(String contactName, String phoneNumber) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                contactName + "|" + phoneNumber,
                Message.MessageType.FILE
        );
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "✅ Contact sent");
                    updateChatList("[Contact]");
                    Toast.makeText(this, "Contact sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send contact: " + e.getMessage());
                    Toast.makeText(ChatDetailActivity.this, "Failed to send contact", Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== UTILITY METHODS ====================

    private byte[] readBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        byte[] allBytes = new byte[0];

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] newBytes = new byte[allBytes.length + bytesRead];
            System.arraycopy(allBytes, 0, newBytes, 0, allBytes.length);
            System.arraycopy(buffer, 0, newBytes, allBytes.length, bytesRead);
            allBytes = newBytes;
        }

        inputStream.close();
        return allBytes;
    }

    private String getFileName(Uri uri) {
        String fileName = "file";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("_display_name");
                if (columnIndex >= 0) {
                    fileName = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name: " + e.getMessage());
        }
        return fileName;
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