package com.example.project_ez_talk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.GroupChannelAdapter;
import com.example.project_ez_talk.model.GroupChannelItem;
import com.example.project_ez_talk.ui.chat.group.CreateGroupActivity;
import com.example.project_ez_talk.ui.chat.group.GroupChatActivity;
import com.example.project_ez_talk.ui.chat.group.Grouplistactivity;
import com.example.project_ez_talk.ui.channel.CreateChannelActivity;
import com.example.project_ez_talk.ui.channel.ChannelDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // Views
    private TextView tvUserName, tvWelcome;
    private ImageView ivUserAvatar, ivSettings;
    private CardView cardCreateGroup, cardCreateChannel;
    private RecyclerView rvRecentChats;
    private LinearLayout layoutEmpty;
    private TextView tvSeeAll;

    private GroupChannelAdapter combinedAdapter;
    private List<GroupChannelItem> combinedList = new ArrayList<>();

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private ListenerRegistration groupListener;
    private ListenerRegistration channelListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        initViews(view);
        setupUserInfo();
        setupQuickActions();
        setupRecyclerView();
        loadGroupsAndChannels();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        ivSettings = view.findViewById(R.id.ivSettings);

        cardCreateGroup = view.findViewById(R.id.cardCreateGroup);
        cardCreateChannel = view.findViewById(R.id.cardCreateChannel);

        rvRecentChats = view.findViewById(R.id.rvRecentChats);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvSeeAll = view.findViewById(R.id.tvSeeAll);

        Log.d(TAG, "Views initialized");
    }

    private void setupUserInfo() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (tvWelcome != null) {
                tvWelcome.setText("Welcome back! 👋");
            }

            if (displayName != null && !displayName.isEmpty()) {
                if (tvUserName != null) tvUserName.setText(displayName);
            } else if (email != null) {
                if (tvUserName != null) tvUserName.setText(email.split("@")[0]);
            } else {
                if (tvUserName != null) tvUserName.setText("User");
            }

            loadUserAvatar();
        } else {
            if (tvUserName != null) tvUserName.setText("Guest User");
        }

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> {
                // TODO: Open settings
            });
        }
    }

    private void loadUserAvatar() {
        if (currentUser == null || !isAdded()) return;

        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    if (documentSnapshot.exists()) {
                        String avatarUrl = documentSnapshot.getString("profilePicture");
                        if (avatarUrl != null && !avatarUrl.isEmpty() && ivUserAvatar != null) {
                            Glide.with(this)
                                    .load(avatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_profile)
                                    .into(ivUserAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading avatar: " + e.getMessage()));
    }

    private void setupQuickActions() {
        if (cardCreateGroup != null) {
            cardCreateGroup.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), CreateGroupActivity.class));
            });
        }

        if (cardCreateChannel != null) {
            cardCreateChannel.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), CreateChannelActivity.class));
            });
        }

        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), Grouplistactivity.class));
            });
        }
    }

    private void setupRecyclerView() {
        rvRecentChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        combinedAdapter = new GroupChannelAdapter(combinedList, item -> {
            if ("group".equals(item.getType())) {
                openGroup(item);
            } else if ("channel".equals(item.getType())) {
                openChannel(item);
            }
        });
        rvRecentChats.setAdapter(combinedAdapter);
    }

    private void loadGroupsAndChannels() {
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        String userId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        // Remove old listeners if any
        if (groupListener != null) groupListener.remove();
        if (channelListener != null) channelListener.remove();

        combinedList.clear();
        combinedAdapter.notifyDataSetChanged();

        // Load Groups
        groupListener = db.collection("groups")
                .whereArrayContains("memberIds", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        Log.e(TAG, "Error loading groups: " + (error != null ? error.getMessage() : "null snapshot"));
                        return;
                    }

                    // Remove old groups from combined list
                    combinedList.removeIf(item -> "group".equals(item.getType()));

                    for (var doc : snapshots.getDocuments()) {
                        String groupId = doc.getId();
                        String name = doc.getString("name");
                        String icon = doc.getString("icon");
                        String lastMsg = doc.getString("lastMessage");
                        Long timestamp = doc.getLong("lastMessageTime");

                        GroupChannelItem item = new GroupChannelItem();
                        item.setId(groupId);
                        item.setName(name != null ? name : "Group");
                        item.setAvatarUrl(icon);
                        item.setLastMessage(lastMsg != null && !lastMsg.isEmpty() ? lastMsg : "No messages yet");
                        item.setTimestamp(timestamp != null ? timestamp : 0L);
                        item.setType("group");

                        combinedList.add(item);
                    }

                    sortAndUpdateUI();
                });

        // Load Channels (from user's subscribed list)
        channelListener = db.collection("users")
                .document(userId)
                .collection("channels")
                .addSnapshotListener((channelSnapshots, error) -> {
                    if (error != null || channelSnapshots == null) {
                        Log.e(TAG, "Error loading user channels: " + (error != null ? error.getMessage() : "null"));
                        return;
                    }

                    // Remove old channels
                    combinedList.removeIf(item -> "channel".equals(item.getType()));

                    for (var doc : channelSnapshots.getDocuments()) {
                        String channelId = doc.getId();
                        String name = doc.getString("name");
                        String avatar = doc.getString("avatar");

                        // Load full channel data
                        db.collection("channels").document(channelId).get()
                                .addOnSuccessListener(channelDoc -> {
                                    if (channelDoc.exists()) {
                                        String lastMsg = channelDoc.getString("lastMessage");
                                        Long timestamp = channelDoc.getLong("lastMessageTimestamp");

                                        GroupChannelItem item = new GroupChannelItem();
                                        item.setId(channelId);
                                        item.setName(name != null ? name : "Channel");
                                        item.setAvatarUrl(avatar);
                                        item.setLastMessage(lastMsg != null && !lastMsg.isEmpty() ? lastMsg : "No messages yet");
                                        item.setTimestamp(timestamp != null ? timestamp : 0L);
                                        item.setType("channel");

                                        combinedList.add(item);
                                        sortAndUpdateUI();
                                    }
                                });
                    }
                });
    }

    private void sortAndUpdateUI() {
        if (!isAdded()) return;

        // Sort by latest activity
        combinedList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        if (combinedList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            combinedAdapter.notifyDataSetChanged();
        }

        Log.d(TAG, "Displaying " + combinedList.size() + " items (groups + channels only)");
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvRecentChats.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        rvRecentChats.setVisibility(View.VISIBLE);
    }

    private void openGroup(GroupChannelItem item) {
        Intent intent = new Intent(requireContext(), GroupChatActivity.class);
        intent.putExtra("groupId", item.getId());
        intent.putExtra("groupName", item.getName());
        startActivity(intent);
    }

    private void openChannel(GroupChannelItem item) {
        Intent intent = new Intent(requireContext(), ChannelDetailActivity.class);
        intent.putExtra("channelId", item.getId());
        intent.putExtra("channelName", item.getName());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadGroupsAndChannels(); // Refresh on resume
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (groupListener != null) groupListener.remove();
        if (channelListener != null) channelListener.remove();
    }
}