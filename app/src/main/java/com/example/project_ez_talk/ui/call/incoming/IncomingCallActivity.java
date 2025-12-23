package com.example.project_ez_talk.ui.call.incoming;
import androidx.activity.OnBackPressedCallback;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.call.ongoing.CallActivity;
import com.example.project_ez_talk.ui.call.video.VideoCallActivity;

public class IncomingCallActivity extends BaseActivity {

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
        } else {
            intent = new Intent(this, CallActivity.class);
        }

        intent.putExtra(CallActivity.EXTRA_USER_ID, callerId);
        intent.putExtra(CallActivity.EXTRA_USER_NAME, callerName);
        intent.putExtra(CallActivity.EXTRA_USER_AVATAR, callerAvatar);
        intent.putExtra(CallActivity.EXTRA_IS_INCOMING, true);

        startActivity(intent);
        finish();
    }

    private void declineCall() {
        Toast.makeText(this, "Call declined", Toast.LENGTH_SHORT).show();
        // TODO: Send decline notification to caller via FCM or WebSocket
        finish();
    }
}