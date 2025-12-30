package com.example.project_ez_talk.ui.call.incoming;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.CallData;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.video.IntegratedVideoCallActivity;
import com.example.project_ez_talk.ui.call.voice.VoiceCallActivity;
import com.example.project_ez_talk.utils.PermissionHelper;
import com.example.project_ez_talk.webrtc.FirebaseSignaling;
import com.example.project_ez_talk.webrtc.MainRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ FIXED IntegratedIncomingCallActivity - Shows incoming call screen
 * CRITICAL: Initializes WebRTC immediately so OFFER can be processed
 */
public class IntegratedIncomingCallActivity extends BaseActivity {

    private static final String TAG = "IncomingCall";
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

    // Intent extra keys
    public static final String EXTRA_CALLER_ID = "caller_id";
    public static final String EXTRA_CALLER_NAME = "caller_name";
    public static final String EXTRA_CALLER_AVATAR = "caller_avatar";
    public static final String EXTRA_CALL_TYPE = "call_type";
    public static final String EXTRA_CURRENT_USER_ID = "current_user_id";

    private ImageView ivCallerAvatar;
    private TextView tvCallerName;
    private TextView tvCallType;
    private LinearLayout btnAccept;
    private LinearLayout btnDecline;

    private String callerId;
    private String callerName;
    private String callerAvatar;
    private String callType;
    private String currentUserId;

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
                finish();
                return;
            }
        }

        initViews();
        setupListeners();
        loadCallerInfo();

        // ✅ CRITICAL FIX: Handle permissions and initialize WebRTC immediately
        if (callType != null && callType.equals("video")) {
            // Video call needs camera + microphone
            if (PermissionHelper.hasCameraPermission(this) &&
                    PermissionHelper.hasAudioPermission(this)) {
                Log.d(TAG, "✅ All permissions granted");
                initializeFirebaseSignaling();
                initializeWebRTCImmediately();
            } else {
                Log.d(TAG, "🎥 Requesting camera & microphone permissions...");
                PermissionHelper.requestCameraAndAudioPermission(this);
            }
        } else {
            // Voice call needs only microphone
            if (PermissionHelper.hasAudioPermission(this)) {
                Log.d(TAG, "✅ Audio permission granted");
                initializeFirebaseSignaling();
                initializeWebRTCImmediately();
            } else {
                Log.d(TAG, "🎤 Requesting audio permission...");
                PermissionHelper.requestAudioPermission(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE_CAMERA_AUDIO) {
            if (PermissionHelper.isPermissionGranted(grantResults)) {
                Log.d(TAG, "✅ Camera & microphone permissions granted");
                initializeFirebaseSignaling();
                initializeWebRTCImmediately();
            } else {
                Log.e(TAG, "❌ Permissions denied");
                Toast.makeText(this, "Permissions required for calls", Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == PermissionHelper.PERMISSION_REQUEST_CODE_AUDIO) {
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
     * Initialize UI views
     */
    private void initViews() {
        ivCallerAvatar = findViewById(R.id.ivCallerAvatar);
        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallType = findViewById(R.id.tvCallType);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);

        if (tvCallerName == null) {
            Log.e(TAG, "❌ tvCallerName is null!");
        }
        if (tvCallType == null) {
            Log.e(TAG, "❌ tvCallType is null!");
        }
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> acceptCall());
        } else {
            Log.e(TAG, "❌ btnAccept is null!");
        }

        if (btnDecline != null) {
            btnDecline.setOnClickListener(v -> declineCall());
        } else {
            Log.e(TAG, "❌ btnDecline is null!");
        }
    }

    /**
     * Load caller information on UI
     */
    private void loadCallerInfo() {
        // Set caller name
        if (tvCallerName != null) {
            if (callerName != null && !callerName.isEmpty()) {
                tvCallerName.setText(callerName);
            } else {
                tvCallerName.setText("Unknown Caller");
            }
        }

        // Set call type
        if (tvCallType != null) {
            if ("video".equals(callType)) {
                tvCallType.setText(R.string.video_call);
            } else {
                tvCallType.setText(R.string.voice_call);
            }
        }

        // Load caller avatar
        if (ivCallerAvatar != null) {
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
                Toast.makeText(IntegratedIncomingCallActivity.this,
                        "Failed to initialize call",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Handle accept call action
     */
    private void acceptCall() {
        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "✅ USER ACCEPTED THE CALL");
        Log.d(TAG, "════════════════════════════════════════");

        // Validate current user
        if (currentUserId == null || currentUserId.isEmpty()) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                Log.e(TAG, "❌ User not authenticated");
                Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            currentUserId = firebaseUser.getUid();
        }

        // ✅ Send ACCEPT signal back to caller
        if (firebaseSignaling != null && callerId != null) {
            CallData acceptData = new CallData();
            acceptData.setTargetId(callerId);
            acceptData.setSenderId(currentUserId);
            acceptData.setType(CallData.Type.ACCEPT);
            acceptData.setCallType(callType);
            acceptData.setData("Call accepted");

            firebaseSignaling.sendCallData(acceptData, () -> {
                Log.e(TAG, "❌ Failed to send accept signal");
            });

            Log.d(TAG, "📤 ACCEPT signal sent to caller: " + callerId);
        }

        // ✅ Start appropriate call activity
        Intent intent;
        if ("video".equals(callType)) {
            Log.d(TAG, "📹 Starting video call activity");
            intent = new Intent(this, IntegratedVideoCallActivity.class);
            intent.putExtra(IntegratedVideoCallActivity.EXTRA_USER_ID, callerId);
            intent.putExtra(IntegratedVideoCallActivity.EXTRA_USER_NAME, callerName);
            intent.putExtra(IntegratedVideoCallActivity.EXTRA_USER_AVATAR, callerAvatar);
            intent.putExtra(IntegratedVideoCallActivity.EXTRA_CURRENT_USER_ID, currentUserId);
            intent.putExtra(IntegratedVideoCallActivity.EXTRA_IS_INCOMING, true);
        } else {
            Log.d(TAG, "🎤 Starting voice call activity");
            intent = new Intent(this, VoiceCallActivity.class);
            intent.putExtra(VoiceCallActivity.EXTRA_USER_ID, callerId);
            intent.putExtra(VoiceCallActivity.EXTRA_USER_NAME, callerName);
            intent.putExtra(VoiceCallActivity.EXTRA_USER_AVATAR, callerAvatar);
            intent.putExtra(VoiceCallActivity.EXTRA_CURRENT_USER_ID, currentUserId);
            intent.putExtra(VoiceCallActivity.EXTRA_IS_INCOMING, true);
        }

        startActivity(intent);
        finish();
    }

    /**
     * ✅ Handle decline call action
     */
    private void declineCall() {
        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "❌ USER DECLINED THE CALL");
        Log.d(TAG, "════════════════════════════════════════");

        Toast.makeText(this, "Call declined", Toast.LENGTH_SHORT).show();

        // ✅ Send REJECT signal to caller
        if (firebaseSignaling != null && callerId != null && currentUserId != null) {
            CallData rejectData = new CallData();
            rejectData.setTargetId(callerId);
            rejectData.setSenderId(currentUserId);
            rejectData.setType(CallData.Type.REJECT);
            rejectData.setCallType(callType);
            rejectData.setData("Call declined");

            firebaseSignaling.sendCallData(rejectData, () -> {
                Log.e(TAG, "❌ Failed to send reject signal");
            });

            Log.d(TAG, "📤 REJECT signal sent to caller: " + callerId);

            // ✅ Log missed call to database
            logMissedCall();
        } else {
            Log.w(TAG, "⚠️ Cannot send reject signal - missing data");
        }

        finish();
    }

    /**
     * ✅ Log missed call when user declines
     */
    private void logMissedCall() {
        DatabaseReference callLogsRef = FirebaseDatabase.getInstance(DATABASE_URL)
                .getReference("call_logs");

        String callId = callLogsRef.push().getKey();
        if (callId == null) {
            Log.e(TAG, "Failed to generate call ID");
            return;
        }

        // Get receiver name
        String receiverName = "Unknown";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            receiverName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (receiverName == null || receiverName.isEmpty()) {
                receiverName = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }
        }

        Map<String, Object> callLog = new HashMap<>();
        callLog.put("callId", callId);
        callLog.put("callerId", callerId);
        callLog.put("receiverId", currentUserId);
        callLog.put("callerName", callerName != null ? callerName : "Unknown");
        callLog.put("receiverName", receiverName != null ? receiverName : "Unknown");
        callLog.put("callerAvatar", callerAvatar != null ? callerAvatar : "");
        callLog.put("receiverAvatar", "");
        callLog.put("callType", callType != null ? callType : "voice");
        callLog.put("status", "missed");
        callLog.put("startTime", System.currentTimeMillis());
        callLog.put("duration", 0);
        callLog.put("timestamp", System.currentTimeMillis());

        callLogsRef.child(callId).setValue(callLog)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Missed call logged to database"))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to log missed call: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Cleaning up");

        // Clean up Firebase Signaling listener
        if (firebaseSignaling != null) {
            firebaseSignaling.removeListener();
            Log.d(TAG, "🧹 Firebase Signaling listener removed");
        }
    }
}