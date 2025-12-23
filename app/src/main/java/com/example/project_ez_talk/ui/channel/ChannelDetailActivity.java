package com.example.project_ez_talk.ui.channel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelDetailActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private ShapeableImageView ivChannelAvatar;
    private TextView tvChannelName, tvSubscriberCount;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageView btnSend, btnEmoji;

    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    private String channelId;
    private String channelName;
    private String currentUserId;
    private boolean isAdmin = false;

    private DatabaseReference channelRef;
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);

        // Get channel info from intent
        channelId = getIntent().getStringExtra("channelId");
        channelName = getIntent().getStringExtra("channelName");

        if (channelId == null) {
            Toast.makeText(this, "Invalid channel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initFirebase();
        initViews();
        setupListeners();
        loadChannelInfo();
        loadMessages();
    }

    private void initFirebase() {
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        channelRef = FirebaseDatabase.getInstance().getReference("channels").child(channelId);
        messagesRef = FirebaseDatabase.getInstance().getReference("channel-messages").child(channelId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivChannelAvatar = findViewById(R.id.iv_channel_avatar);
        tvChannelName = findViewById(R.id.tv_channel_name);
        tvSubscriberCount = findViewById(R.id.tv_subscriber_count);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnEmoji = findViewById(R.id.btn_emoji);

        setSupportActionBar(toolbar);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setAdapter(messageAdapter);

        tvChannelName.setText(channelName != null ? channelName : "Channel");
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        btnEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Show emoji picker
                Toast.makeText(ChannelDetailActivity.this, "Emoji picker coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChannelInfo() {
        channelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                    Integer subscriberCount = snapshot.child("subscriberCount").getValue(Integer.class);

                    if (name != null) {
                        tvChannelName.setText(name);
                    }

                    if (subscriberCount != null) {
                        tvSubscriberCount.setText(subscriberCount + " subscribers");
                    }

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(ChannelDetailActivity.this)
                                .load(avatarUrl)
                                .centerCrop()
                                .into(ivChannelAvatar);
                    }

                    // Check if user is admin
                    DataSnapshot adminsSnapshot = snapshot.child("admins");
                    isAdmin = adminsSnapshot.hasChild(currentUserId);

                    // Only admins can send messages in channels
                    etMessage.setEnabled(isAdmin);
                    btnSend.setEnabled(isAdmin);

                    if (!isAdmin) {
                        etMessage.setHint("Only admins can send messages");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChannelDetailActivity.this, "Failed to load channel info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    rvMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChannelDetailActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        if (!isAdmin) {
            Toast.makeText(this, "Only admins can send messages", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);

        String messageId = messagesRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", messageId);
        messageData.put("senderId", currentUserId);
        messageData.put("text", messageText);
        messageData.put("timestamp", timestamp);
        messageData.put("type", "text");

        messagesRef.child(messageId).setValue(messageData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        etMessage.setText("");
                        btnSend.setEnabled(true);

                        // Update channel's last message
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastMessage", messageText);
                        updates.put("lastMessageTimestamp", timestamp);
                        channelRef.updateChildren(updates);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btnSend.setEnabled(true);
                        Toast.makeText(ChannelDetailActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}