package com.example.project_ez_talk.ui.chat.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.GroupChannelAdapter;
import com.example.project_ez_talk.model.GroupChannelItem;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.channel.ChannelDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class Grouplistactivity extends BaseActivity {

    private Toolbar toolbar;
    private ImageView btnCreate;
    private RecyclerView rvGroups;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private MaterialButton btnCreateFromEmpty;

    private GroupChannelAdapter adapter;
    private List<GroupChannelItem> allItems = new ArrayList<>();
    private List<GroupChannelItem> displayedItems = new ArrayList<>();

    private String currentUserId;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private static final int TAB_GROUPS = 0;
    private static final int TAB_CHANNELS = 1;
    private int currentTab = TAB_GROUPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        initFirebase();
        initViews();
        setupListeners();
        setupRecyclerView();
        loadGroupsAndChannels();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnCreate = findViewById(R.id.btnCreate);
        rvGroups = findViewById(R.id.rvGroups);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tabLayout = findViewById(R.id.tabLayout);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        btnCreateFromEmpty = findViewById(R.id.btnCreateFromEmpty);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        btnCreate.setOnClickListener(v -> openCreateGroup());
        btnCreateFromEmpty.setOnClickListener(v -> openCreateGroup());

        // FIXED: Use hex colors directly
        swipeRefresh.setOnRefreshListener(this::refreshData);
        swipeRefresh.setColorSchemeColors(0xFF6200EE, 0xFF3700B3, 0xFF03DAC5);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                filterByTab();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupChannelAdapter(displayedItems, item -> {
            if ("group".equals(item.getType())) {
                openGroup(item);
            } else {
                openChannel(item);
            }
        });
        rvGroups.setAdapter(adapter);
    }

    private void loadGroupsAndChannels() {
        showLoading(true);
        allItems.clear();
        loadGroups();
        loadChannels();
    }

    private void loadGroups() {
        db.collection("groups")
                .whereArrayContains("memberIds", currentUserId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        loadGroupsWithFallback();
                        return;
                    }

                    allItems.removeIf(item -> "group".equals(item.getType()));

                    if (querySnapshot != null) {
                        for (var doc : querySnapshot.getDocuments()) {
                            String groupId = doc.getId();
                            String name = doc.getString("name");
                            String icon = doc.getString("icon");
                            String lastMsg = doc.getString("lastMessage");
                            Long timestamp = doc.getLong("lastMessageTime");

                            GroupChannelItem item = new GroupChannelItem();
                            item.setId(groupId);
                            item.setName(name != null ? name : "Group");
                            item.setAvatarUrl(icon);
                            item.setLastMessage(lastMsg != null ? lastMsg : "No messages");
                            item.setTimestamp(timestamp != null ? timestamp : 0);
                            item.setType("group");
                            allItems.add(item);
                        }
                    }
                    sortAndUpdateUI();
                });
    }

    private void loadGroupsWithFallback() {
        db.collection("groups")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        showLoading(false);
                        return;
                    }

                    allItems.removeIf(item -> "group".equals(item.getType()));

                    if (querySnapshot != null) {
                        for (var doc : querySnapshot.getDocuments()) {
                            java.util.Map<String, Object> members = (java.util.Map<String, Object>) doc.get("members");
                            if (members != null && members.containsKey(currentUserId)) {
                                String groupId = doc.getId();
                                String name = doc.getString("name");
                                String icon = doc.getString("icon");
                                String lastMsg = doc.getString("lastMessage");
                                Long timestamp = doc.getLong("lastMessageTime");

                                GroupChannelItem item = new GroupChannelItem();
                                item.setId(groupId);
                                item.setName(name != null ? name : "Group");
                                item.setAvatarUrl(icon);
                                item.setLastMessage(lastMsg != null ? lastMsg : "No messages");
                                item.setTimestamp(timestamp != null ? timestamp : 0);
                                item.setType("group");
                                allItems.add(item);
                            }
                        }
                    }
                    sortAndUpdateUI();
                });
    }

    private void loadChannels() {
        db.collection("channels")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        showLoading(false);
                        return;
                    }

                    allItems.removeIf(item -> "channel".equals(item.getType()));

                    if (querySnapshot != null) {
                        for (var doc : querySnapshot.getDocuments()) {
                            java.util.Map<String, Object> subscribers = (java.util.Map<String, Object>) doc.get("subscribers");
                            if (subscribers != null && subscribers.containsKey(currentUserId)) {
                                String channelId = doc.getId();
                                String name = doc.getString("name");
                                String icon = doc.getString("avatarUrl");
                                String lastMsg = doc.getString("lastMessage");
                                Long timestamp = doc.getLong("lastMessageTimestamp");
                                Long subCount = doc.getLong("subscriberCount");

                                GroupChannelItem item = new GroupChannelItem();
                                item.setId(channelId);
                                item.setName(name != null ? name : "Channel");
                                item.setAvatarUrl(icon);
                                item.setLastMessage(lastMsg != null ? lastMsg : "No messages");
                                item.setTimestamp(timestamp != null ? timestamp : 0);
                                item.setType("channel");
                                item.setMemberCount(subCount != null ? subCount.intValue() : 0);
                                allItems.add(item);
                            }
                        }
                    }
                    sortAndUpdateUI();
                });
    }

    private void sortAndUpdateUI() {
        showLoading(false);
        allItems.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        filterByTab();
    }

    private void filterByTab() {
        displayedItems.clear();
        if (currentTab == TAB_GROUPS) {
            for (GroupChannelItem item : allItems) {
                if ("group".equals(item.getType())) {
                    displayedItems.add(item);
                }
            }
        } else {
            for (GroupChannelItem item : allItems) {
                if ("channel".equals(item.getType())) {
                    displayedItems.add(item);
                }
            }
        }

        if (displayedItems.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            adapter.notifyDataSetChanged();
        }
    }

    private void refreshData() {
        loadGroupsAndChannels();
        swipeRefresh.setRefreshing(false);
    }

    private void openCreateGroup() {
        startActivity(new Intent(this, CreateGroupActivity.class));
    }

    private void openGroup(GroupChannelItem item) {
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra("groupId", item.getId());
        intent.putExtra("groupName", item.getName());
        startActivity(intent);
    }

    private void openChannel(GroupChannelItem item) {
        Intent intent = new Intent(this, ChannelDetailActivity.class);
        intent.putExtra("channelId", item.getId());
        intent.putExtra("channelName", item.getName());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvGroups.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvGroups.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        rvGroups.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupsAndChannels();
    }
}