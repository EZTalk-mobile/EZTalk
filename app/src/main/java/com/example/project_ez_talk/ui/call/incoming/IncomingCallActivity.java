<<<<<<< HEAD
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
=======
/* package com.example.project_ez_talk.ui.call.incoming;
import androidx.activity.OnBackPressedCallback;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.CallLog;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.ongoing.CallActivity;
import com.example.project_ez_talk.ui.call.video.VideoCallActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class IncomingCallActivity extends BaseActivity {

    private static final String TAG = "IncomingCallActivity";
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

    public static final String EXTRA_CALLER_NAME = "caller_name";
    public static final String EXTRA_CALLER_AVATAR = "caller_avatar";
    public static final String EXTRA_CALLER_ID = "caller_id";
    public static final String EXTRA_CALL_TYPE = "call_type"; // "voice" or "video"

    private ImageView ivCallerAvatar;
    private TextView tvCallerName, tvCallType;
    private LinearLayout btnAccept, btnDecline;

    private String callerName;
    private String callerAvatar;
    private String callerId;
    private String callType;

    private String currentUserId;
    private String currentUserName = "";
    private String currentUserAvatar = "";
    private FirebaseFirestore firestore;
    private DatabaseReference callLogsRef;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

<<<<<<< HEAD
        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "   📞 INCOMING CALL ACTIVITY STARTED");
        Log.d(TAG, "════════════════════════════════════════");

        // Prevent back press - user must accept or decline
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "⚠️ Back press blocked - user must answer call");
=======
        // Disable back press - modern approach
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }

            public void handleOnBackPress() {
                // Prevent back press during incoming call
                Toast.makeText(IncomingCallActivity.this,
                        "Please accept or decline the call",
                        Toast.LENGTH_SHORT).show();
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
            }
        });

        // Get call data from intent
<<<<<<< HEAD
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
=======
        callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);
        callerAvatar = getIntent().getStringExtra(EXTRA_CALLER_AVATAR);
        callerId = getIntent().getStringExtra(EXTRA_CALLER_ID);
        callType = getIntent().getStringExtra(EXTRA_CALL_TYPE);

        // Initialize Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        firestore = FirebaseFirestore.getInstance();
        callLogsRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("call_logs");

        // Load current user info for call log
        loadCurrentUserInfo();

        initViews();
        setupListeners();
        loadCallerInfo();
    }

    private void initViews() {
        ivCallerAvatar = findViewById(R.id.ivCallerAvatar);
        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallType = findViewById(R.id.tvCallType);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);
    }

    private void setupListeners() {
        btnAccept.setOnClickListener(v -> acceptCall());
        btnDecline.setOnClickListener(v -> declineCall());
    }

    private void loadCallerInfo() {
        // Set caller name
        if (callerName != null && !callerName.isEmpty()) {
            tvCallerName.setText(callerName);
        } else {
            tvCallerName.setText("Unknown Caller");
        }

        // Set call type
        if ("video".equals(callType)) {
            tvCallType.setText(R.string.video_call);
        } else {
            tvCallType.setText(R.string.voice_call);
        }

        // Load caller avatar
        Glide.with(this)
                .load(callerAvatar)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(ivCallerAvatar);
    }

    private void acceptCall() {
        Toast.makeText(this, "Call accepted", Toast.LENGTH_SHORT).show();

        // Navigate to call screen
        Intent intent;
        if ("video".equals(callType)) {
            intent = new Intent(this, VideoCallActivity.class);
            intent.putExtra(VideoCallActivity.EXTRA_USER_ID, callerId);
            intent.putExtra(VideoCallActivity.EXTRA_USER_NAME, callerName);
            intent.putExtra(VideoCallActivity.EXTRA_USER_AVATAR, callerAvatar);
            intent.putExtra(VideoCallActivity.EXTRA_IS_INCOMING, true);
        } else {
            intent = new Intent(this, CallActivity.class);
            intent.putExtra(CallActivity.EXTRA_USER_ID, callerId);
            intent.putExtra(CallActivity.EXTRA_USER_NAME, callerName);
            intent.putExtra(CallActivity.EXTRA_USER_AVATAR, callerAvatar);
            intent.putExtra(CallActivity.EXTRA_IS_INCOMING, true);
        }

        startActivity(intent);
        finish();
    }

    private void declineCall() {
        Toast.makeText(this, "Call declined", Toast.LENGTH_SHORT).show();

        // Save rejected call log
        saveRejectedCallLog();

        // TODO: Send decline notification to caller via FCM or WebSocket
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        finish();
    }

    /**
<<<<<<< HEAD
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
=======
     * Load current user info from Firestore for call log
     */
    private void loadCurrentUserInfo() {
        if (currentUserId == null) return;

        firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentUserName = doc.getString("name");
                        currentUserAvatar = doc.getString("profilePicture");
                        Log.d(TAG, "✅ Current user info loaded: " + currentUserName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to load current user info: " + e.getMessage());
                });
    }

    /**
     * Save rejected call log to Firebase Realtime Database
     */
    private void saveRejectedCallLog() {
        if (currentUserId == null || callerId == null) {
            Log.e(TAG, "❌ Cannot save call log: missing user IDs");
            return;
        }

        // Create call log for rejected call
        CallLog callLog = new CallLog();

        // Caller is the remote user, receiver is current user
        callLog.setCallerId(callerId);
        callLog.setReceiverId(currentUserId);
        callLog.setCallerName(callerName != null ? callerName : "Unknown");
        callLog.setReceiverName(currentUserName != null ? currentUserName : "You");
        callLog.setCallerAvatar(callerAvatar != null ? callerAvatar : "");
        callLog.setReceiverAvatar(currentUserAvatar != null ? currentUserAvatar : "");

        callLog.setCallType(callType != null ? callType : "video");
        callLog.setStatus("rejected");
        callLog.setStartTime(System.currentTimeMillis());
        callLog.setDuration(0);
        callLog.setTimestamp(System.currentTimeMillis());

        // Save to Realtime Database
        String callLogId = callLogsRef.push().getKey();
        if (callLogId != null) {
            callLogsRef.child(callLogId)
                    .setValue(callLog)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Rejected call log saved: " + callLogId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to save rejected call log: " + e.getMessage());
                    });
        }
    }
}
*/
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
