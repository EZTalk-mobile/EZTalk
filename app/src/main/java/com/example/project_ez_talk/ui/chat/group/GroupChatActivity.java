package com.example.project_ez_talk.ui.chat.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.MessageAdapter;
import com.example.project_ez_talk.model.Message;
import com.example.project_ez_talk.ui.BaseActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ALL")
public class GroupChatActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private LinearLayout layoutGroupHeader;
    private ShapeableImageView ivGroupIcon;
    private TextView tvGroupName;
    private TextView tvMemberCount;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageView btnSend, btnAttach, btnEmoji;

    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    private String groupId;
    private String groupName;
    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // Get group info from intent
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        if (groupId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initFirebase();
        initViews();
        setupListeners();
        loadGroupInfo();
        loadMessages();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutGroupHeader = findViewById(R.id.layout_group_header);
        ivGroupIcon = findViewById(R.id.iv_group_icon);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvMemberCount = findViewById(R.id.tv_member_count);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnAttach = findViewById(R.id.btn_attach);
        btnEmoji = findViewById(R.id.btn_emoji);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(messageList, this);
        rvMessages.setAdapter(messageAdapter);

        if (groupName != null) {
            tvGroupName.setText(groupName);
        }
    }

    private void setupListeners() {
        // Back button - now works perfectly!
        toolbar.setNavigationOnClickListener(v -> finish());

        // === JAVA-ONLY FIX: Allow back arrow clicks by ignoring touches on the left side ===
        layoutGroupHeader.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // If touch is on the left ~80dp (where back arrow is), let toolbar handle it
                if (event.getX() < dpToPx(80)) {
                    return false; // Do not consume — pass to parent (toolbar)
                }
                // Otherwise, open group details
                openGroupDetails();
                return true;
            }
            return false;
        });

        // Optional: Keep setOnClickListener for accessibility / long press, etc.
        layoutGroupHeader.setOnClickListener(v -> openGroupDetails());

        // Send message
        btnSend.setOnClickListener(v -> sendMessage());

        // Attach file
        btnAttach.setOnClickListener(v -> Toast.makeText(this, "File attachment coming soon", Toast.LENGTH_SHORT).show());

        // Emoji picker
        btnEmoji.setOnClickListener(v -> Toast.makeText(this, "Emoji picker coming soon", Toast.LENGTH_SHORT).show());
    }

    private void openGroupDetails() {
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private void loadGroupInfo() {
        db.collection("groups")
                .document(groupId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(GroupChatActivity.this, "Failed to load group info", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String iconUrl = documentSnapshot.getString("icon");
                        Map<String, Boolean> members = (Map<String, Boolean>) documentSnapshot.get("members");

                        if (name != null) {
                            tvGroupName.setText(name);
                            groupName = name;
                        }
                        if (iconUrl != null && !iconUrl.isEmpty()) {
                            Glide.with(GroupChatActivity.this)
                                    .load(iconUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_group)
                                    .into(ivGroupIcon);
                        }
                        if (members != null) {
                            int memberCount = members.size();
                            tvMemberCount.setText(memberCount + (memberCount == 1 ? " member" : " members"));
                        }
                    }
                });
    }

    private void loadMessages() {
        db.collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(GroupChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        messageList.clear();
                        for (var documentSnapshot : querySnapshot.getDocuments()) {
                            Message message = documentSnapshot.toObject(Message.class);
                            if (message != null) {
                                messageList.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvMessages.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        btnSend.setEnabled(false);

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUserName = documentSnapshot.exists()
                            ? documentSnapshot.getString("name")
                            : "Unknown User";

                    String messageId = UUID.randomUUID().toString();
                    long timestamp = System.currentTimeMillis();

                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("messageId", messageId);
                    messageData.put("senderId", currentUserId);
                    messageData.put("senderName", currentUserName);
                    messageData.put("content", messageText);
                    messageData.put("text", messageText);
                    messageData.put("timestamp", timestamp);
                    messageData.put("type", "text");
                    messageData.put("status", "sent");

                    db.collection("groups")
                            .document(groupId)
                            .collection("messages")
                            .document(messageId)
                            .set(messageData)
                            .addOnSuccessListener(unused -> {
                                etMessage.setText("");
                                btnSend.setEnabled(true);

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("lastMessage", messageText);
                                updates.put("lastMessageTime", timestamp);

                                db.collection("groups")
                                        .document(groupId)
                                        .update(updates)
                                        .addOnFailureListener(e ->
                                                android.util.Log.e("GroupChat", "Failed to update group: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> {
                                btnSend.setEnabled(true);
                                Toast.makeText(GroupChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(GroupChatActivity.this, "Failed to get user info", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper: Convert dp to pixels
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}