package com.example.project_ez_talk.ui.call.incoming;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

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
            }
        });

        // Get call data from intent
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
        finish();
    }

    /**
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