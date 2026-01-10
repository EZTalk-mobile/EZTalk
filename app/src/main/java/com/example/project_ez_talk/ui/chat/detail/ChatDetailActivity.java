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
<<<<<<< HEAD
import android.os.Handler;
import android.os.Looper;
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
<<<<<<< HEAD
import android.view.MotionEvent;
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
<<<<<<< HEAD
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
=======
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.MessageAdapter;
<<<<<<< HEAD
import com.example.project_ez_talk.adapter.SwipeToDeleteCallback;
import com.example.project_ez_talk.model.CallData;
import com.example.project_ez_talk.model.Message;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.incoming.IntegratedIncomingCallActivity;
import com.example.project_ez_talk.ui.call.video.IntegratedVideoCallActivity;
import com.example.project_ez_talk.ui.call.voice.VoiceCallActivity;
import com.example.project_ez_talk.utils.AudioRecorderManager;
import com.example.project_ez_talk.utils.MessageNotificationManager;
import com.example.project_ez_talk.webrtc.FirebaseSignaling;
import com.example.project_ez_talk.webrtc.MainRepository;
=======
import com.example.project_ez_talk.helper.SupabaseStorageManager;
import com.example.project_ez_talk.model.Message;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.video.VideoCallActivity;
import com.example.project_ez_talk.webTRC.FirebaseClient;

import java.io.IOException;
import java.io.InputStream;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
<<<<<<< HEAD
import com.google.firebase.database.FirebaseDatabase;
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
<<<<<<< HEAD

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
=======
import com.permissionx.guolindev.PermissionX;

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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

<<<<<<< HEAD
/**
 * ✅ COMPLETE ChatDetailActivity with VIDEO + AUDIO UPLOAD
 */
=======
import androidx.recyclerview.widget.ItemTouchHelper;
import com.example.project_ez_talk.adapter.SwipeToDeleteCallback;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
@SuppressWarnings("ALL")
public class ChatDetailActivity extends BaseActivity {

    private static final String TAG = "ChatDetailActivity";

<<<<<<< HEAD
    // ✅ Firebase Realtime Database URL
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    // Supabase Configuration
    private static final String SUPABASE_URL = "https://ijcfvpodwmshmdecmxmk.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlqY2Z2cG9kd21zaG1kZWNteG1rIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjYwNTM5NzQsImV4cCI6MjA4MTYyOTk3NH0.35y9_9TMIMEltfYRFs06oOPJwpIGEUHZQasXYkch3IQ";
    private static final String BUCKET_IMAGES = "chat-images";
    private static final String BUCKET_DOCUMENTS = "chat-documents";
    private static final String BUCKET_AUDIO = "chat-audio";
<<<<<<< HEAD
    private static final String BUCKET_VIDEO = "chat-video";
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    // UI Views
    private Toolbar toolbar;
    private ImageView ivUserAvatar;
    private TextView tvUserName;
    private TextView tvUserStatus;
    private ImageView btnBack;
    private ImageView btnVoiceCall;
    private ImageView btnVideoCall;
    private ImageView btnMore;
<<<<<<< HEAD
=======

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    private RecyclerView rvMessages;
    private EditText etMessage;
    private FloatingActionButton fabSend;
    private FloatingActionButton fabVoice;
    private ImageView btnEmoji;
    private ImageView btnAttach;
<<<<<<< HEAD

    // Voice Recording Views
    private View voiceRecordingOverlay;
    private TextView tvRecordingTime;

    // Voice Recording
    private AudioRecorderManager audioRecorder;
    private Handler recordingHandler;
    private long recordingStartTime;
    private boolean isRecording = false;
    private AudioRecorderManager.RecordingCallback currentRecordingCallback;

=======
    private CardView voiceRecordingOverlay;
    private TextView tvRecordingTime;

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    // Adapters and Data
    private MessageAdapter messageAdapter;

    // Firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
<<<<<<< HEAD
    private FirebaseDatabase rtdb;
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    private String chatId;
    private CollectionReference messagesRef;
    private ListenerRegistration listener;

<<<<<<< HEAD
    // Firebase Signaling for calls
    private FirebaseSignaling firebaseSignaling;

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    // Other user info
    private String receiverId;
    private String receiverName;
    private String receiverAvatar;

    // Current user info
<<<<<<< HEAD
    private String currentUserName = "";
    private String currentUserAvatar = "";
    private String currentUserId = "";

    // Media handling
    private Uri imageUri;

    // Location
    private LocationManager locationManager;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> documentLauncher;
    private ActivityResultLauncher<Intent> audioLauncher;
    private ActivityResultLauncher<Intent> videoLauncher;
    private ActivityResultLauncher<Intent> contactLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ActivityResultLauncher<String> galleryPermissionLauncher;
=======
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


>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

<<<<<<< HEAD
        setupActivityResultLaunchers();
        initializeFirebase();
=======
        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Initialize Supabase Storage
        SupabaseStorageManager.init(this);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

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

<<<<<<< HEAD
        // Initialize Firebase Signaling for incoming calls
        initializeFirebaseSignaling();
    }

    private void initializeFirebase() {
        Log.d(TAG, "🔄 Initializing Firebase...");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        db = FirebaseFirestore.getInstance();
        rtdb = FirebaseDatabase.getInstance(DATABASE_URL);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d(TAG, "✅ Firebase fully initialized");
    }

    private void initializeFirebaseSignaling() {
        Log.d(TAG, "🔔 Initializing Firebase Signaling...");

        if (currentUser == null) {
            Log.e(TAG, "❌ Current user is null");
            return;
        }

        firebaseSignaling = FirebaseSignaling.getInstance();

        if (firebaseSignaling.isInitialized() &&
                firebaseSignaling.getCurrentUserId().equals(currentUser.getUid())) {
            Log.d(TAG, "✅ Firebase Signaling already initialized");
            setupIncomingCallListener();
            return;
        }

        firebaseSignaling.init(currentUser.getUid(), new FirebaseSignaling.OnSuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Firebase Signaling initialized");
                setupIncomingCallListener();
            }

            @Override
            public void onError() {
                Log.e(TAG, "❌ Failed to initialize Firebase Signaling");
            }
        });
    }

    private void setupIncomingCallListener() {
        Log.d(TAG, "👂 Setting up incoming call listener...");

        firebaseSignaling.observeIncomingCalls(new FirebaseSignaling.OnCallDataListener() {
            @Override
            public void onCallDataReceived(CallData callData) {
                if (callData.getType() == CallData.Type.OFFER) {
                    Log.d(TAG, "📞 INCOMING CALL!");
                    showIncomingCallScreen(callData);
                }
            }

            @Override
            public void onOffer(CallData callData) {}

            @Override
            public void onAnswer(CallData callData) {}

            @Override
            public void onIceCandidate(CallData callData) {}

            @Override
            public void onAccept(CallData callData) {}

            @Override
            public void onReject(CallData callData) {}

            @Override
            public void onError() {
                Log.e(TAG, "❌ Error listening for incoming calls");
            }
        });
    }

    private void showIncomingCallScreen(CallData callData) {
        String callerId = callData.getSenderId();
        String callerName = callData.getData() != null ? callData.getData() : "Unknown";
        String callType = callData.getCallType() != null ? callData.getCallType() : "voice";

        Intent intent = new Intent(this, IntegratedIncomingCallActivity.class);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALLER_ID, callerId);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALLER_NAME, callerName);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALLER_AVATAR, "");
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALL_TYPE, callType);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CURRENT_USER_ID, currentUser.getUid());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    // ============================================================
    // ACTIVITY RESULT LAUNCHERS
    // ============================================================

    private void setupActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToSupabase(imageUri);
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && imageUri != null) {
                        uploadImageToSupabase(imageUri);
                    }
                });

        documentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            uploadDocumentToSupabase(fileUri);
                        }
                    }
                });

        // ✅ AUDIO LAUNCHER
        audioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri audioUri = result.getData().getData();
                        if (audioUri != null) {
                            uploadAudioToSupabase(audioUri);
                        }
                    }
                });

        // ✅ VIDEO LAUNCHER
        videoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        if (videoUri != null) {
                            uploadVideoToSupabase(videoUri);
                        }
                    }
                });

        contactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        if (contactUri != null) {
                            handleContactSelection(contactUri);
                        }
                    }
                });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    Boolean cameraGranted = permissions.get(Manifest.permission.CAMERA);
                    Boolean storageGranted = permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE);

                    if (Boolean.TRUE.equals(cameraGranted) && Boolean.TRUE.equals(storageGranted)) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        locationPermissionLauncher = registerForActivityResult(
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

        galleryPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchGalleryPicker();
                    } else {
                        Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                    }
                });
    }

<<<<<<< HEAD
    // ============================================================
    // SETUP METHODS
    // ============================================================
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        btnBack = findViewById(R.id.btnBack);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);
        btnVideoCall = findViewById(R.id.btnVideoCall);
<<<<<<< HEAD
=======
        btnVideoCall.setOnClickListener(v -> onClickVideoCall());
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        btnMore = findViewById(R.id.btnMore);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        fabSend = findViewById(R.id.fabSend);
        fabVoice = findViewById(R.id.fabVoice);
        btnEmoji = findViewById(R.id.btnEmoji);
        btnAttach = findViewById(R.id.btnAttach);
<<<<<<< HEAD

        // Voice recording views
        voiceRecordingOverlay = findViewById(R.id.voiceRecordingOverlay);
        tvRecordingTime = findViewById(R.id.tvRecordingTime);

        // Initialize voice recorder
        audioRecorder = new AudioRecorderManager();
        recordingHandler = new Handler(Looper.getMainLooper());

=======
        voiceRecordingOverlay = findViewById(R.id.voiceRecordingOverlay);
        tvRecordingTime = findViewById(R.id.tvRecordingTime);

        // Hide send button initially
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        fabSend.setVisibility(View.GONE);
        fabVoice.setVisibility(View.VISIBLE);
    }

<<<<<<< HEAD
=======
    /**
     * ✅ Fetch current user's name and avatar from Firestore
     */
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    private void fetchCurrentUserInfo() {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
<<<<<<< HEAD
                        String name = documentSnapshot.getString("name");
                        String avatar = documentSnapshot.getString("avatarUrl");
                        currentUserName = name != null ? name : "User";
                        currentUserAvatar = avatar != null ? avatar : "";
                    } else {
                        currentUserName = currentUser.getEmail() != null ? currentUser.getEmail() : "User";
                        currentUserAvatar = "";
                    }
=======
                        currentUserName = documentSnapshot.getString("name");
                        currentUserAvatar = documentSnapshot.getString("profilePicture");
                        Log.d(TAG, "Current user info loaded: " + currentUserName);
                    } else {
                        currentUserName = currentUser.getEmail() != null ? currentUser.getEmail() : "User";
                        currentUserAvatar = "";
                        Log.d(TAG, "User document doesn't exist, using email");
                    }

                    // Create chat document and start loading messages
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                    createInitialChatDocument();
                    loadRealTimeMessages();
                })
                .addOnFailureListener(e -> {
<<<<<<< HEAD
                    Log.e(TAG, "❌ Error fetching user: " + e.getMessage());
                    currentUserName = currentUser.getEmail() != null ? currentUser.getEmail() : "User";
                    currentUserAvatar = "";
=======
                    Log.e(TAG, "Error fetching user info: " + e.getMessage());
                    currentUserName = currentUser.getEmail() != null ? currentUser.getEmail() : "User";
                    currentUserAvatar = "";

                    // Continue anyway
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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

<<<<<<< HEAD
        ItemTouchHelper.Callback callback = new SwipeToDeleteCallback(this, messageAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvMessages);

        messageAdapter.setDeleteListener(message -> {
            Log.d(TAG, "Message deleted: " + message.getMessageId());
        });
    }

=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    private void generateChatId() {
        String id1 = currentUser.getUid();
        String id2 = receiverId;
        chatId = id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
        Log.d(TAG, "Generated Chat ID: " + chatId);
    }

<<<<<<< HEAD
    private void createInitialChatDocument() {
        long timestamp = System.currentTimeMillis();

=======
    /**
     * ✅ Create initial chat document with participants array
     */
    private void createInitialChatDocument() {
        long timestamp = System.currentTimeMillis();

        // Shared chat document with participants
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        Map<String, Object> sharedChatData = new HashMap<>();
        sharedChatData.put("id", chatId);
        sharedChatData.put("participants", Arrays.asList(currentUser.getUid(), receiverId));
        sharedChatData.put("createdAt", timestamp);

        db.collection("chats")
                .document(chatId)
<<<<<<< HEAD
                .set(sharedChatData, SetOptions.merge());

=======
                .set(sharedChatData, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Error creating shared chat: " + e.getMessage()));

        // Current user's chat list entry (shows receiver's info)
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        Map<String, Object> chatDataCurrent = new HashMap<>();
        chatDataCurrent.put("id", chatId);
        chatDataCurrent.put("name", receiverName != null ? receiverName : "Unknown");
        chatDataCurrent.put("avatarUrl", receiverAvatar != null ? receiverAvatar : "");
        chatDataCurrent.put("lastMessage", "");
        chatDataCurrent.put("lastMessageTimestamp", timestamp);
        chatDataCurrent.put("unreadCount", 0);

<<<<<<< HEAD
        Map<String, Object> chatDataReceiver = new HashMap<>();
        chatDataReceiver.put("id", chatId);
        chatDataReceiver.put("name", currentUserName);
        chatDataReceiver.put("avatarUrl", currentUserAvatar);
=======
        // Receiver's chat list entry (shows current user's info)
        Map<String, Object> chatDataReceiver = new HashMap<>();
        chatDataReceiver.put("id", chatId);
        chatDataReceiver.put("name", currentUserName != null ? currentUserName : "Unknown");
        chatDataReceiver.put("avatarUrl", currentUserAvatar != null ? currentUserAvatar : "");
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        chatDataReceiver.put("lastMessage", "");
        chatDataReceiver.put("lastMessageTimestamp", timestamp);
        chatDataReceiver.put("unreadCount", 0);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("chats")
                .document(chatId)
<<<<<<< HEAD
                .set(chatDataCurrent, SetOptions.merge());
=======
                .set(chatDataCurrent)
                .addOnFailureListener(e -> Log.e(TAG, "Error creating current user's chat: " + e.getMessage()));
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

        db.collection("users")
                .document(receiverId)
                .collection("chats")
                .document(chatId)
<<<<<<< HEAD
                .set(chatDataReceiver, SetOptions.merge());
=======
                .set(chatDataReceiver)
                .addOnFailureListener(e -> Log.e(TAG, "Error creating receiver's chat: " + e.getMessage()));
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

        Log.d(TAG, "Chat document created");
    }

<<<<<<< HEAD
    private void loadRealTimeMessages() {
        Log.d(TAG, "Loading messages for chatId: " + chatId);

        messageAdapter.setCurrentChatId(chatId);
        messageAdapter.setChatType("private");
=======
    /**
     * ✅ Load real-time messages
     */
    private void loadRealTimeMessages() {
        // ==================== SET CHAT TYPE AND ID FOR SWIPE DELETE ====================
        messageAdapter.setCurrentChatId(chatId);
        messageAdapter.setChatType("private");  // ✅ Important: Set chat type to "private" for 1-on-1 chats
        Log.d(TAG, "Loading messages for chatId: " + chatId);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

        messagesRef = db.collection("chats")
                .document(chatId)
                .collection("messages");

        listener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
<<<<<<< HEAD
                        Log.e(TAG, "❌ Firestore Error: " + e.getMessage());
=======
                        Log.e(TAG, "Firestore Error: " + e.getMessage());
                        Toast.makeText(ChatDetailActivity.this,
                                "Error loading messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                        return;
                    }

                    if (snapshots == null) {
                        Log.d(TAG, "No messages yet");
                        return;
                    }

<<<<<<< HEAD
                    Log.d(TAG, "✅ Messages loaded: " + snapshots.size());

                    List<Message> newMessages = new ArrayList<>();
=======
                    Log.d(TAG, "Messages loaded: " + snapshots.size());

                    List<Message> messages = new ArrayList<>();
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                message.setMessageId(doc.getId());
<<<<<<< HEAD
                                newMessages.add(message);
=======
                                messages.add(message);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing message: " + ex.getMessage());
                        }
                    }

<<<<<<< HEAD
                    messageAdapter.setMessages(newMessages);

                    if (!newMessages.isEmpty()) {
                        rvMessages.scrollToPosition(newMessages.size() - 1);
=======
                    messageAdapter.setMessages(messages);

                    // Scroll to latest message
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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
<<<<<<< HEAD

        // Voice recording with press and hold
        fabVoice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startVoiceRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopVoiceRecording(event.getAction() == MotionEvent.ACTION_CANCEL);
                    return true;
            }
            return false;
        });
        btnAttach.setOnClickListener(v -> showAttachmentBottomSheet());
        btnEmoji.setOnClickListener(v -> Toast.makeText(this, "Emoji picker coming soon", Toast.LENGTH_SHORT).show());

        btnVoiceCall.setOnClickListener(v -> initiateVoiceCall());
        btnVideoCall.setOnClickListener(v -> initiateVideoCall());
        btnMore.setOnClickListener(v -> Toast.makeText(this, "More options coming soon", Toast.LENGTH_SHORT).show());
    }

    private void initiateVoiceCall() {
        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Cannot start call: Invalid user", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "📞 INITIATING VOICE CALL to: " + receiverName);

        if (firebaseSignaling != null) {
            CallData callData = new CallData();
            callData.setTargetId(receiverId);
            callData.setSenderId(currentUserId);
            callData.setType(CallData.Type.OFFER);
            callData.setCallType("voice");
            callData.setData(currentUserName);

            firebaseSignaling.sendCallData(callData, () -> {
                Log.e(TAG, "❌ Failed to send call via FirebaseSignaling");
                Toast.makeText(ChatDetailActivity.this, "Failed to send call", Toast.LENGTH_SHORT).show();
            });
        }

        MainRepository repository = MainRepository.getInstance();
        repository.sendCallRequest(receiverId, () -> {
            Log.e(TAG, "❌ Failed to send call via MainRepository");
            runOnUiThread(() -> {
                Toast.makeText(ChatDetailActivity.this, "User not available", Toast.LENGTH_SHORT).show();
            });
        });

        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.putExtra(VoiceCallActivity.EXTRA_USER_ID, receiverId);
        intent.putExtra(VoiceCallActivity.EXTRA_USER_NAME, receiverName);
        intent.putExtra(VoiceCallActivity.EXTRA_USER_AVATAR, receiverAvatar);
        intent.putExtra(VoiceCallActivity.EXTRA_CURRENT_USER_ID, currentUserId);
        intent.putExtra(VoiceCallActivity.EXTRA_IS_INCOMING, false);

        startActivity(intent);
    }

    private void initiateVideoCall() {
        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Cannot start call: Invalid user", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "📹 INITIATING VIDEO CALL to: " + receiverName);

        if (firebaseSignaling != null) {
            CallData callData = new CallData();
            callData.setTargetId(receiverId);
            callData.setSenderId(currentUserId);
            callData.setType(CallData.Type.OFFER);
            callData.setCallType("video");
            callData.setData(currentUserName);

            firebaseSignaling.sendCallData(callData, () -> {
                Log.e(TAG, "❌ Failed to send call via FirebaseSignaling");
                Toast.makeText(ChatDetailActivity.this, "Failed to send call", Toast.LENGTH_SHORT).show();
            });
        }

        MainRepository repository = MainRepository.getInstance();
        repository.sendCallRequest(receiverId, () -> {
            Log.e(TAG, "❌ Failed to send call via MainRepository");
            runOnUiThread(() -> {
                Toast.makeText(ChatDetailActivity.this, "User not available", Toast.LENGTH_SHORT).show();
            });
        });

        Intent intent = new Intent(this, IntegratedVideoCallActivity.class);
        intent.putExtra(IntegratedVideoCallActivity.EXTRA_USER_ID, receiverId);
        intent.putExtra(IntegratedVideoCallActivity.EXTRA_USER_NAME, receiverName);
        intent.putExtra(IntegratedVideoCallActivity.EXTRA_USER_AVATAR, receiverAvatar);
        intent.putExtra(IntegratedVideoCallActivity.EXTRA_CURRENT_USER_ID, currentUserId);
        intent.putExtra(IntegratedVideoCallActivity.EXTRA_IS_INCOMING, false);

        startActivity(intent);
    }

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

    private void sendTextMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
            return;
        }

        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                messageText,
                Message.MessageType.TEXT
        );

<<<<<<< HEAD
=======
        // Add sender info
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
<<<<<<< HEAD
                    Log.d(TAG, "✅ Message saved");
                    etMessage.setText("");
                    updateChatListBothUsers(messageText);

                    MessageNotificationManager.sendMessageNotification(
                            receiverId,
                            currentUserName,
                            messageText,
                            chatId,
                            currentUser.getUid()
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed: " + e.getMessage());
                });
    }

    private void updateChatListBothUsers(String messagePreview) {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> chatDataCurrent = new HashMap<>();
        chatDataCurrent.put("lastMessage", messagePreview);
        chatDataCurrent.put("lastMessageTimestamp", timestamp);
        chatDataCurrent.put("unreadCount", 0);
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

        db.collection("users")
                .document(currentUser.getUid())
                .collection("chats")
                .document(chatId)
                .update(chatDataCurrent);

<<<<<<< HEAD
        Map<String, Object> chatDataReceiver = new HashMap<>();
        chatDataReceiver.put("lastMessage", messagePreview);
        chatDataReceiver.put("lastMessageTimestamp", timestamp);
        chatDataReceiver.put("unreadCount", com.google.firebase.firestore.FieldValue.increment(1));

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        db.collection("users")
                .document(receiverId)
                .collection("chats")
                .document(chatId)
                .update(chatDataReceiver);
    }

<<<<<<< HEAD
    // ============================================================
    // Voice Recording Methods
    // ============================================================

    private void startVoiceRecording() {
        if (isRecording) return;

        // Check permission
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 100);
            return;
        }

        try {
            currentRecordingCallback = new AudioRecorderManager.RecordingCallback() {
                @Override
                public void onRecordingStarted() {
                    runOnUiThread(() -> {
                        isRecording = true;
                        recordingStartTime = System.currentTimeMillis();
                        voiceRecordingOverlay.setVisibility(View.VISIBLE);
                        startRecordingTimer();
                    });
                }

                @Override
                public void onRecordingProgress(long durationMs) {
                    // Progress handled by timer
                }

                @Override
                public void onRecordingCompleted(String filePath, long durationMs) {
                    runOnUiThread(() -> {
                        isRecording = false;
                        voiceRecordingOverlay.setVisibility(View.GONE);
                        recordingHandler.removeCallbacksAndMessages(null);
                        uploadVoiceMessage(filePath, durationMs);
                    });
                }

                @Override
                public void onRecordingError(String error) {
                    runOnUiThread(() -> {
                        isRecording = false;
                        voiceRecordingOverlay.setVisibility(View.GONE);
                        recordingHandler.removeCallbacksAndMessages(null);
                        Toast.makeText(ChatDetailActivity.this, "Recording error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            };
            audioRecorder.startRecording(this, currentRecordingCallback);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start recording", e);
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoiceRecording(boolean cancel) {
        if (!isRecording) return;

        if (cancel) {
            audioRecorder.cancelRecording();
            isRecording = false;
            voiceRecordingOverlay.setVisibility(View.GONE);
            recordingHandler.removeCallbacksAndMessages(null);
            Toast.makeText(this, "Recording cancelled", Toast.LENGTH_SHORT).show();
        } else {
            audioRecorder.stopRecording(currentRecordingCallback);
        }
    }

    private void startRecordingTimer() {
        recordingHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsed = System.currentTimeMillis() - recordingStartTime;
                    int seconds = (int) (elapsed / 1000) % 60;
                    int minutes = (int) (elapsed / 1000) / 60;
                    tvRecordingTime.setText(String.format("%02d:%02d", minutes, seconds));
                    recordingHandler.postDelayed(this, 100);
                }
            }
        });
    }

    private void uploadVoiceMessage(String filePath, long durationMs) {
        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            Toast.makeText(this, "Audio file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Uploading voice message...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                byte[] fileBytes = new byte[(int) audioFile.length()];
                java.io.FileInputStream fis = new java.io.FileInputStream(audioFile);
                fis.read(fileBytes);
                fis.close();

                String fileName = "voice_" + System.currentTimeMillis() + ".m4a";
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("audio/mp4"),
                        fileBytes
                );

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET_AUDIO + "/" + fileName)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "audio/mp4")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String fileUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_AUDIO + "/" + fileName;

                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        sendAudioMessage(fileUrl, durationMs);
                        audioFile.delete();
                    });
                } else {
                    throw new Exception("Upload failed: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload error", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChatDetailActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    audioFile.delete();
                });
            }
        }).start();
    }

    private void sendAudioMessage(String audioUrl, long durationMs) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                audioUrl,
                Message.MessageType.AUDIO
        );

        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());
        message.setDuration(durationMs);

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "✅ Voice message sent");
                    updateChatListBothUsers("🎤 Voice message");

                    MessageNotificationManager.sendMessageNotification(
                            receiverId,
                            currentUserName,
                            "🎤 Voice message",
                            chatId,
                            currentUser.getUid()
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send voice message: " + e.getMessage());
                    Toast.makeText(this, "Failed to send voice message", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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

<<<<<<< HEAD
    // ============================================================
    // IMAGE HANDLING
    // ============================================================

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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
<<<<<<< HEAD
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
=======
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        }
    }

    private void openCamera() {
        try {
<<<<<<< HEAD
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

=======
            Log.d(TAG, "📷 Opening camera...");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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

<<<<<<< HEAD
    private void uploadImageToSupabase(Uri imageUri) {
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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
<<<<<<< HEAD
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show());
                    return;
                }
=======
                if (inputStream == null) return;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

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
<<<<<<< HEAD
                    runOnUiThread(() -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
=======
                    runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
            }
        }).start();
    }

<<<<<<< HEAD
=======
    /**
     * ✅ Send image message with sender info
     */
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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
<<<<<<< HEAD
                    updateChatListBothUsers("[Image]");
                    Toast.makeText(this, "Image sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send image", Toast.LENGTH_SHORT).show());
    }

    // ============================================================
    // DOCUMENT HANDLING
    // ============================================================
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
<<<<<<< HEAD
=======
        String[] mimeTypes = {"application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        documentLauncher.launch(intent);
    }

    private void uploadDocumentToSupabase(Uri documentUri) {
<<<<<<< HEAD
        if (documentUri == null) {
            Toast.makeText(this, "No document selected", Toast.LENGTH_SHORT).show();
            return;
        }

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading document...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(documentUri);
<<<<<<< HEAD
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to read document", Toast.LENGTH_SHORT).show());
                    return;
                }

                byte[] documentData = readBytes(inputStream);
                String fileName = getFileName(documentUri);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "document_" + System.currentTimeMillis();
                }

                String filePathWithoutExtension = "chat_" + chatId + "_" + System.currentTimeMillis();
                String filePath = "chat_documents/" + filePathWithoutExtension + "_" + fileName;
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_DOCUMENTS + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(documentData, MediaType.parse("application/octet-stream"));
=======
                if (inputStream == null) return;

                byte[] fileData = readBytes(inputStream);
                String fileName = getFileName(documentUri);
                String filePath = "documents/" + chatId + "_" + System.currentTimeMillis() + "_" + fileName;
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_DOCUMENTS + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(fileData, MediaType.parse("application/octet-stream"));
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

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
<<<<<<< HEAD
                    String finalFileName = fileName;
                    runOnUiThread(() -> sendDocumentMessage(documentUrl, finalFileName));
                    Log.d(TAG, "✅ Document uploaded successfully: " + documentUrl);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "❌ Upload failed: " + response.code() + " - " + errorBody);
                    runOnUiThread(() -> Toast.makeText(this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show());
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "❌ Document upload error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
=======
                    runOnUiThread(() -> sendDocumentMessage(documentUrl, fileName));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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
<<<<<<< HEAD

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        message.setFileUrl(documentUrl);
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
<<<<<<< HEAD
                    Log.d(TAG, "✅ Document message saved");
                    updateChatListBothUsers("[Document: " + fileName + "]");
                    Toast.makeText(this, "Document sent successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to save document message: " + e.getMessage());
                    Toast.makeText(this, "Failed to send document", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // ✅ AUDIO HANDLING
    // ============================================================
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        audioLauncher.launch(intent);
    }

<<<<<<< HEAD
    /**
     * ✅ COMPLETE: Upload Audio to Supabase
     */
    private void uploadAudioToSupabase(Uri audioUri) {
        if (audioUri == null) {
            Toast.makeText(this, "No audio selected", Toast.LENGTH_SHORT).show();
            return;
        }

=======
    private void uploadAudioToSupabase(Uri audioUri) {
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading audio...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
<<<<<<< HEAD
                // ✅ STEP 1: Read the audio file
                InputStream inputStream = getContentResolver().openInputStream(audioUri);
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to read audio", Toast.LENGTH_SHORT).show());
                    return;
                }

                byte[] audioData = readBytes(inputStream);

                // ✅ STEP 2: Get the file name
                String fileName = getFileName(audioUri);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "audio_" + System.currentTimeMillis() + ".mp3";
                }

                Log.d(TAG, "🎵 Audio name: " + fileName);

                // ✅ STEP 3: Create file path in Supabase
                String filePath = "chat_audio/" + "chat_" + chatId + "_" + System.currentTimeMillis() + "_" + fileName;

                // ✅ STEP 4: Upload to Supabase
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_AUDIO + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(audioData, MediaType.parse("application/octet-stream"));
=======
                InputStream inputStream = getContentResolver().openInputStream(audioUri);
                if (inputStream == null) return;

                byte[] audioData = readBytes(inputStream);
                String fileName = "audio_" + chatId + "_" + System.currentTimeMillis() + ".m4a";
                String filePath = "audio/" + fileName;
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_AUDIO + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(audioData, MediaType.parse("audio/mp4"));
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(body)
<<<<<<< HEAD
                        .addHeader("Content-Type", "application/octet-stream")
=======
                        .addHeader("Content-Type", "audio/mp4")
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .build();

<<<<<<< HEAD
                Log.d(TAG, "📤 Uploading audio...");

                Response response = client.newCall(request).execute();

                // ✅ STEP 5: Handle response
                if (response.isSuccessful()) {
                    String audioUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_AUDIO + "/" + filePath;
                    Log.d(TAG, "✅ Audio uploaded successfully: " + audioUrl);
                    String finalFileName = fileName;
                    runOnUiThread(() -> sendAudioFileMessage(audioUrl, finalFileName));
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "❌ Upload failed: " + response.code() + " - " + errorBody);
                    runOnUiThread(() -> Toast.makeText(this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show());
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "❌ Audio upload error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
=======
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String audioUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_AUDIO + "/" + filePath;
                    runOnUiThread(() -> sendAudioMessage(audioUrl));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ChatDetailActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
            }
        }).start();
    }

<<<<<<< HEAD
    /**
     * ✅ NEW: Send Audio File Message to Firestore
     */
    private void sendAudioFileMessage(String audioUrl, String fileName) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                fileName,
                Message.MessageType.AUDIO
        );

=======
    private void sendAudioMessage(String audioUrl) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                "",
                Message.MessageType.AUDIO
        );
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        message.setFileUrl(audioUrl);
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

<<<<<<< HEAD
        Log.d(TAG, "💾 Saving audio message to Firestore...");

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "✅ Audio message saved to Firebase");
                    updateChatListBothUsers("[Audio]");
                    Toast.makeText(this, "Audio sent successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to save audio message: " + e.getMessage());
                    Toast.makeText(this, "Failed to send audio", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // ✅ VIDEO HANDLING
    // ============================================================

    /**
     * ✅ NEW: Open Video Picker
     */
    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        videoLauncher.launch(intent);
    }

    /**
     * ✅ COMPLETE: Upload Video to Supabase
     */
    private void uploadVideoToSupabase(Uri videoUri) {
        if (videoUri == null) {
            Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading video...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // ✅ STEP 1: Read the video file
                InputStream inputStream = getContentResolver().openInputStream(videoUri);
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to read video", Toast.LENGTH_SHORT).show());
                    return;
                }

                byte[] videoData = readBytes(inputStream);

                // ✅ STEP 2: Get the file name
                String fileName = getFileName(videoUri);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "video_" + System.currentTimeMillis() + ".mp4";
                }

                Log.d(TAG, "🎬 Video name: " + fileName);

                // ✅ STEP 3: Create file path in Supabase
                String filePath = "chat_video/" + "chat_" + chatId + "_" + System.currentTimeMillis() + "_" + fileName;

                // ✅ STEP 4: Upload to Supabase
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_VIDEO + "/" + filePath;

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(videoData, MediaType.parse("application/octet-stream"));

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(body)
                        .addHeader("Content-Type", "application/octet-stream")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .build();

                Log.d(TAG, "📤 Uploading video...");

                Response response = client.newCall(request).execute();

                // ✅ STEP 5: Handle response
                if (response.isSuccessful()) {
                    String videoUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_VIDEO + "/" + filePath;
                    Log.d(TAG, "✅ Video uploaded successfully: " + videoUrl);
                    String finalFileName = fileName;
                    runOnUiThread(() -> sendVideoMessage(videoUrl, finalFileName));
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "❌ Upload failed: " + response.code() + " - " + errorBody);
                    runOnUiThread(() -> Toast.makeText(this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show());
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "❌ Video upload error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * ✅ NEW: Send Video Message to Firestore
     */
    private void sendVideoMessage(String videoUrl, String fileName) {
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                fileName,
                Message.MessageType.VIDEO
        );

        message.setFileUrl(videoUrl);
        message.setSenderName(currentUserName);
        message.setSenderAvatarUrl(currentUserAvatar);
        message.setTimestamp(System.currentTimeMillis());

        Log.d(TAG, "💾 Saving video message to Firestore...");

        messagesRef.add(message)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "✅ Video message saved to Firebase");
                    updateChatListBothUsers("[Video]");
                    Toast.makeText(this, "Video sent successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to save video message: " + e.getMessage());
                    Toast.makeText(this, "Failed to send video", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // LOCATION & CONTACT HANDLING
    // ============================================================
=======
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
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

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
<<<<<<< HEAD
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
=======
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

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
<<<<<<< HEAD
                            Log.d(TAG, "✅ LOCATION message saved");
                            updateChatListBothUsers("[Location]");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Failed to save location message");
=======
                            Log.d(TAG, "✅ Location sent");
                            updateChatList("[Location]");
                            Toast.makeText(this, "Location sent", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to send location: " + e.getMessage());
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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

<<<<<<< HEAD
=======
    // ==================== CONTACT HANDLING ====================

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
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
<<<<<<< HEAD
                    Log.d(TAG, "✅ CONTACT message saved");
                    updateChatListBothUsers("[Contact: " + contactName + "]");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to save contact message");
=======
                    Log.d(TAG, "✅ Contact sent");
                    updateChatList("[Contact]");
                    Toast.makeText(this, "Contact sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send contact: " + e.getMessage());
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                    Toast.makeText(ChatDetailActivity.this, "Failed to send contact", Toast.LENGTH_SHORT).show();
                });
    }

<<<<<<< HEAD
    // ============================================================
    // UTILITY METHODS
    // ============================================================
=======
    // ==================== UTILITY METHODS ====================
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

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
<<<<<<< HEAD

        if (firebaseSignaling != null) {
            firebaseSignaling.removeListener();
            Log.d(TAG, "Firebase Signaling listener removed");
        }
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }
}