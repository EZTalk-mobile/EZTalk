package com.example.project_ez_talk.ui.channel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.ChannelAdapter;
import com.example.project_ez_talk.model.Channel;
import com.example.project_ez_talk.ui.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChannelListActivity extends BaseActivity {

    private static final String TAG = "ChannelListActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvChannels;
    private FloatingActionButton fabCreateChannel;

    private ChannelAdapter channelAdapter;
    private List<Channel> allChannels = new ArrayList<>();
    private List<Channel> myChannels = new ArrayList<>();
    private List<Channel> displayedChannels = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;
    private boolean showingMyChannels = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);

        initFirebase();
        initViews();
        setupListeners();
        loadChannels();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvChannels = findViewById(R.id.rv_channels);
        fabCreateChannel = findViewById(R.id.fab_create_channel);

        setSupportActionBar(toolbar);

        // Setup RecyclerView
        rvChannels.setLayoutManager(new LinearLayoutManager(this));
        channelAdapter = new ChannelAdapter(displayedChannels, new ChannelAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(Channel channel) {
                openChannelDetail(channel);
            }

            @Override
            public void onClick(Channel channel) {
                openChannelDetail(channel);
            }
        });
        rvChannels.setAdapter(channelAdapter);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        fabCreateChannel.setOnClickListener(v -> {
            Intent intent = new Intent(ChannelListActivity.this, CreateChannelActivity.class);
            startActivity(intent);
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Discover tab - show all public channels
                    showingMyChannels = false;
                    displayedChannels.clear();
                    displayedChannels.addAll(allChannels);
                    channelAdapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ Showing all channels: " + allChannels.size());
                } else {
                    // My Channels tab - show user's subscribed channels
                    showingMyChannels = true;
                    displayedChannels.clear();
                    displayedChannels.addAll(myChannels);
                    channelAdapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ Showing my channels: " + myChannels.size());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadChannels() {
        // ✅ Load all public channels from Firestore
        db.collection("channels")
                .whereEqualTo("isPublic", true)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Error loading channels: " + error.getMessage());
                        return;
                    }

                    if (querySnapshot != null) {
                        allChannels.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            try {
                                Channel channel = document.toObject(Channel.class);
                                if (channel != null) {
                                    channel.setId(document.getId());
                                    allChannels.add(channel);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing channel: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "✅ Loaded " + allChannels.size() + " public channels");

                        if (!showingMyChannels) {
                            displayedChannels.clear();
                            displayedChannels.addAll(allChannels);
                            channelAdapter.notifyDataSetChanged();
                        }
                    }
                });

        // ✅ Load user's subscribed channels from Firestore
        db.collection("users")
                .document(currentUserId)
                .collection("channels")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Error loading user channels: " + error.getMessage());
                        return;
                    }

                    if (querySnapshot != null) {
                        myChannels.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String channelId = document.getId();
                            loadChannelDetails(channelId);
                        }

                        Log.d(TAG, "✅ Found " + querySnapshot.size() + " subscribed channels");
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadChannelDetails(String channelId) {
        db.collection("channels")
                .document(channelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Channel channel = documentSnapshot.toObject(Channel.class);
                            if (channel != null) {
                                channel.setId(documentSnapshot.getId());

                                // Check if channel already exists in myChannels
                                boolean exists = false;
                                for (Channel existingChannel : myChannels) {
                                    if (existingChannel.getId().equals(channel.getId())) {
                                        exists = true;
                                        break;
                                    }
                                }

                                if (!exists) {
                                    myChannels.add(channel);
                                    Log.d(TAG, "✅ Added channel to myChannels: " + channel.getName());
                                }

                                if (showingMyChannels) {
                                    displayedChannels.clear();
                                    displayedChannels.addAll(myChannels);
                                    channelAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing channel details: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to load channel details: " + e.getMessage());
                });
    }

    private void openChannelDetail(Channel channel) {
        Intent intent = new Intent(this, ChannelDetailActivity.class);
        intent.putExtra("channelId", channel.getId());
        intent.putExtra("channelName", channel.getName());
        startActivity(intent);
        Log.d(TAG, "✅ Opening channel: " + channel.getName());
    }
}