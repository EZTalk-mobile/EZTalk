package com.example.project_ez_talk.ui.channel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChannelListActivity extends BaseActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvChannels;
    private FloatingActionButton fabCreateChannel;

    private ChannelAdapter channelAdapter;
    private List<Channel> allChannels = new ArrayList<>();
    private List<Channel> myChannels = new ArrayList<>();
    private List<Channel> displayedChannels = new ArrayList<>();

    private DatabaseReference channelsRef;
    private DatabaseReference userChannelsRef;
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
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        channelsRef = FirebaseDatabase.getInstance().getReference("channels");
        userChannelsRef = FirebaseDatabase.getInstance().getReference("user-channels").child(currentUserId);
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
            /**
             * @param channel
             */
            @Override
            public void onChannelClick(Channel channel) {

            }

            @Override
            public void onClick(Channel channel) {
                openChannelDetail(channel);
            }
        });
        rvChannels.setAdapter(channelAdapter);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fabCreateChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChannelListActivity.this, CreateChannelActivity.class);
                startActivity(intent);
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Discover tab
                    showingMyChannels = false;
                    displayedChannels.clear();
                    displayedChannels.addAll(allChannels);
                    channelAdapter.notifyDataSetChanged();
                } else {
                    // My Channels tab
                    showingMyChannels = true;
                    displayedChannels.clear();
                    displayedChannels.addAll(myChannels);
                    channelAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadChannels() {
        // Load all public channels
        channelsRef.orderByChild("isPublic").equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allChannels.clear();
                        for (DataSnapshot channelSnapshot : snapshot.getChildren()) {
                            Channel channel = channelSnapshot.getValue(Channel.class);
                            if (channel != null) {
                                allChannels.add(channel);
                            }
                        }
                        if (!showingMyChannels) {
                            displayedChannels.clear();
                            displayedChannels.addAll(allChannels);
                            channelAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Load user's channels
        userChannelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myChannels.clear();
                for (DataSnapshot channelSnapshot : snapshot.getChildren()) {
                    String channelId = channelSnapshot.getKey();
                    loadChannelDetails(channelId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadChannelDetails(String channelId) {
        channelsRef.child(channelId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Channel channel = snapshot.getValue(Channel.class);
                if (channel != null) {
                    myChannels.add(channel);
                    if (showingMyChannels) {
                        displayedChannels.clear();
                        displayedChannels.addAll(myChannels);
                        channelAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void openChannelDetail(Channel channel) {
        Intent intent = new Intent(this, ChannelDetailActivity.class);
        intent.putExtra("channelId", channel.getId());
        intent.putExtra("channelName", channel.getName());
        startActivity(intent);
    }
}