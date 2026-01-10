package com.example.project_ez_talk.ui.call.incoming;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.voice.VoiceCallActivity;
import com.example.project_ez_talk.utils.PermissionHelper;
import com.example.project_ez_talk.webrtc.FirebaseSignaling;
import com.example.project_ez_talk.webrtc.MainRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ✅ COMPLETE IncomingCallActivity - Shows incoming call alert
 * CRITICAL: Initializes WebRTC immediately so OFFER can be processed
 */
public class IncomingCallActivity extends BaseActivity {

    private static final String TAG = "IncomingCall";

    // Intent extras
    public static final String EXTRA_CALLER_ID = "caller_id";
    public static final String EXTRA_CALLER_NAME = "caller_name";
    public static final String EXTRA_CALLER_AVATAR = "caller_avatar";
    public static final String EXTRA_CALL_TYPE = "call_type";
    public static final String EXTRA_CURRENT_USER_ID = "current_user_id";

    // UI Components
    private AppCompatImageView ivCallerAvatar;
    private MaterialButton btnAccept;
    private MaterialButton btnReject;

    // Call data
    private String callerId;
    private String callerName;
    private String callerAvatar;
    private String callType;
    private String currentUserId;

    // Firebase
    private FirebaseSignaling firebaseSignaling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "   📞 INCOMING CALL ACTIVITY STARTED");
        Log.d(TAG, "════════════════════════════════════════");

        // Prevent back press - user must accept or decline
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "⚠️ Back press blocked - user must answer call");
            }
        });

        // Get call data from intent
        callerId = getIntent().getStringExtra(EXTRA_CALLER_ID);
        callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);
        callerAvatar = getIntent().getStringExtra(EXTRA_CALLER_AVATAR);
        callType = getIntent().getStringExtra(EXTRA_CALL_TYPE);
        currentUserId = getIntent().getStringExtra(EXTRA_CURRENT_USER_ID);

        Log.d(TAG, "Caller ID: " + callerId);
        Log.d(TAG, "Caller Name: " + callerName);
        Log.d(TAG, "Call Type: " + callType);

        // Get current user if not provided
        if (currentUserId == null || currentUserId.isEmpty()) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                currentUserId = firebaseUser.getUid();
            } else {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "User not authenticated");
                finish();
                return;
            }
        }

        Log.d(TAG, "Current User: " + currentUserId);

        // ✅ Initialize UI
        initViews();
        setupListeners();
        loadCallerInfo();

        // ✅ Handle microphone permissions
        if (PermissionHelper.hasAudioPermission(this)) {
            Log.d(TAG, "✅ Audio permission already granted");
            initializeFirebaseSignaling();
            initializeWebRTCImmediately();
        } else {
            Log.d(TAG, "🎤 Requesting audio permission...");
            PermissionHelper.requestAudioPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE_AUDIO) {
            if (PermissionHelper.isPermissionGranted(grantResults)) {
                Log.d(TAG, "✅ Audio permission granted");
                initializeFirebaseSignaling();
                initializeWebRTCImmediately();
            } else {
                Log.e(TAG, "❌ Audio permission denied");
                Toast.makeText(this, "Audio permission required for calls", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * ✅ Initialize UI views
     */
    @SuppressLint("WrongViewCast")
    private void initViews() {
        ivCallerAvatar = findViewById(R.id.ivCallerAvatar);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        Log.d(TAG, "✅ Views initialized");
    }

    /**
     * ✅ Setup button listeners
     */
    private void setupListeners() {
        btnAccept.setOnClickListener(v -> onAcceptCall());
        btnReject.setOnClickListener(v -> onRejectCall());

        Log.d(TAG, "✅ Click listeners attached");
    }

    /**
     * ✅ Load caller information and display
     */
    private void loadCallerInfo() {
        Log.d(TAG, "📱 Loading caller info...");

        if (callerAvatar != null && !callerAvatar.isEmpty()) {
            Glide.with(this)
                    .load(callerAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivCallerAvatar);
        } else {
            ivCallerAvatar.setImageResource(R.drawable.ic_profile);
        }

        Log.d(TAG, "✅ Caller info loaded on UI");
    }

    /**
     * ✅ Initialize Firebase Signaling for sending ACCEPT/REJECT signals
     */
    private void initializeFirebaseSignaling() {
        Log.d(TAG, "🔔 Initializing Firebase Signaling for incoming call");

        firebaseSignaling = FirebaseSignaling.getInstance();
        firebaseSignaling.init(currentUserId, new FirebaseSignaling.OnSuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Firebase Signaling initialized for incoming call");
            }

            @Override
            public void onError() {
                Log.e(TAG, "❌ Firebase Signaling initialization failed");
            }
        });
    }

    /**
     * ✅ CRITICAL FIX: Initialize WebRTC as soon as incoming call arrives
     * This ensures PeerConnection is ready when OFFER arrives from caller
     *
     * Timeline:
     * - IncomingCallActivity appears
     * - initializeWebRTCImmediately() creates PeerConnection NOW
     * - OFFER arrives seconds later
     * - PeerConnection is READY to process it ✅
     *
     * WITHOUT this:
     * - OFFER arrives immediately
     * - PeerConnection doesn't exist yet ❌
     * - OFFER processing fails
     * - User has to click accept multiple times
     */
    private void initializeWebRTCImmediately() {
        Log.d(TAG, "🔄 Initializing WebRTC for incoming call...");
        Log.d(TAG, "   Creating PeerConnection BEFORE OFFER arrives...");

        MainRepository repository = MainRepository.getInstance();

        // ✅ Create PeerConnection without sending OFFER
        repository.loginForCall(currentUserId, new MainRepository.CallCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "════════════════════════════════════════");
                Log.d(TAG, "✅ WebRTC READY - WAITING FOR OFFER");
                Log.d(TAG, "════════════════════════════════════════");
                Log.d(TAG, "PeerConnection created and ready");
                Log.d(TAG, "Can now process OFFER when it arrives");
            }

            @Override
            public void onError() {
                Log.e(TAG, "❌ Failed to initialize WebRTC");
                Toast.makeText(IncomingCallActivity.this,
                        "Failed to initialize call",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Handle accept call
     */
    private void onAcceptCall() {
        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "✅ USER ACCEPTED THE CALL");
        Log.d(TAG, "════════════════════════════════════════");

        // Send ACCEPT signal back to caller
        if (firebaseSignaling != null) {
            firebaseSignaling.acceptCall(callerId, callType, () -> {
                Log.e(TAG, "❌ Failed to send ACCEPT signal");
            });
            Log.d(TAG, "📤 ACCEPT signal sent to caller: " + callerId);
        }

        // Start voice call activity
        Log.d(TAG, "🎤 Starting voice call activity");
        startVoiceCall();
    }

    /**
     * ✅ Handle reject call
     */
    private void onRejectCall() {
        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "❌ USER REJECTED THE CALL");
        Log.d(TAG, "════════════════════════════════════════");

        // Send REJECT signal back to caller
        if (firebaseSignaling != null) {
            firebaseSignaling.rejectCall(callerId, callType, () -> {
                Log.e(TAG, "❌ Failed to send REJECT signal");
            });
            Log.d(TAG, "📤 REJECT signal sent to caller: " + callerId);
        }

        Log.d(TAG, "✅ Missed call logged");

        // Close activity
        finish();
    }

    /**
     * ✅ Start voice call activity
     */
    private void startVoiceCall() {
        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.putExtra(VoiceCallActivity.EXTRA_USER_ID, callerId);
        intent.putExtra(VoiceCallActivity.EXTRA_USER_NAME, callerName);
        intent.putExtra(VoiceCallActivity.EXTRA_USER_AVATAR, callerAvatar);
        intent.putExtra(VoiceCallActivity.EXTRA_CURRENT_USER_ID, currentUserId);
        intent.putExtra(VoiceCallActivity.EXTRA_IS_INCOMING, true);
        intent.putExtra(VoiceCallActivity.EXTRA_CALL_TYPE, callType);

        Log.d(TAG, "🔄 Launching VoiceCallActivity");
        startActivity(intent);

        // Close incoming call screen
        finish();
    }

    /**
     * Cleanup when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Cleaning up");

        // Remove Firebase Signaling listener
        if (firebaseSignaling != null) {
            firebaseSignaling.removeListener();
            Log.d(TAG, "🧹 Firebase Signaling listener removed");
        }
    }
}