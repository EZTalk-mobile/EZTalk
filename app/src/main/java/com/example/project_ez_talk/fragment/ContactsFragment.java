package com.example.project_ez_talk.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.ContactAdapter;
import com.example.project_ez_talk.model.Contact;
import com.example.project_ez_talk.ui.chat.detail.ChatDetailActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";

    private EditText etSearch;
    private TabLayout tabLayout;
    private RecyclerView rvContacts;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private ContactAdapter contactAdapter;
    private final List<Contact> allFriends = new ArrayList<>();
    private final List<Contact> onlineFriends = new ArrayList<>();
    private final List<Contact> friendRequests = new ArrayList<>();
    private List<Contact> currentDisplayList = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;

    private ListenerRegistration friendsListener;
    private ListenerRegistration requestsListener;

    private String currentTab = "all"; // all, online, requests

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d(TAG, "✅ Current User ID: " + currentUserId);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupTabs();
        loadAllData();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        tabLayout = view.findViewById(R.id.tabLayout);
        rvContacts = view.findViewById(R.id.rvContacts);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        contactAdapter = new ContactAdapter(currentDisplayList, requireContext());
        contactAdapter.setOnContactClickListener(contact -> {
            if (contact.isPendingRequest()) {
                // Don't open chat for pending requests
                return;
            }
            Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
            intent.putExtra("user_id", contact.getId());
            intent.putExtra("user_name", contact.getName());
            intent.putExtra("user_avatar", contact.getAvatarUrl());
            startActivity(intent);
        });
        rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvContacts.setAdapter(contactAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0: // All Friends
                        currentTab = "all";
                        currentDisplayList = new ArrayList<>(allFriends);
                        break;
                    case 1: // Online Friends
                        currentTab = "online";
                        currentDisplayList = new ArrayList<>(onlineFriends);
                        break;
                    case 2: // Friend Requests
                        currentTab = "requests";
                        currentDisplayList = new ArrayList<>(friendRequests);
                        break;
                }
                filterList(etSearch.getText().toString());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAllData() {
        progressBar.setVisibility(View.VISIBLE);
        loadAcceptedFriends();
        loadPendingRequests();
    }

    // ==================== NEW METHOD - Load accepted friends from friendRequests collection ====================
    private void loadAcceptedFriends() {
        if (friendsListener != null) friendsListener.remove();

        Log.d(TAG, "🔍 [FRIENDS] Loading accepted friends...");

        // Query the friendRequests collection for all accepted requests
        friendsListener = db.collection("friendRequests")
                .whereEqualTo("status", "accepted")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        Log.e(TAG, "❌ [FRIENDS] Error loading friends: " + error);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    allFriends.clear();
                    onlineFriends.clear();

                    Log.d(TAG, "📊 [FRIENDS] Found " + snapshots.size() + " accepted friend requests");

                    // Process each accepted friend request
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String senderId = doc.getString("senderId");
                        String receiverId = doc.getString("receiverId");
                        String senderName = doc.getString("senderName");
                        String receiverName = doc.getString("receiverName");
                        String senderProfilePicture = doc.getString("senderProfilePicture");

                        // Determine who is the friend (the OTHER person in this request)
                        String friendId;
                        String friendName;
                        String friendAvatar;

                        if (senderId != null && senderId.equals(currentUserId)) {
                            // I (current user) sent the request, so the friend is the RECEIVER
                            friendId = receiverId;
                            friendName = receiverName;
                            friendAvatar = ""; // Will get from user document below
                        } else {
                            // I (current user) received the request, so the friend is the SENDER
                            friendId = senderId;
                            friendName = senderName;
                            friendAvatar = senderProfilePicture;
                        }

                        if (friendId != null) {
                            Log.d(TAG, "📝 [FRIENDS] Processing friend: " + friendName + " (ID: " + friendId + ")");

                            // Get friend's current status from users collection
                            db.collection("users").document(friendId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            // Get friend's current online status and avatar
                                            boolean isOnline = userDoc.getBoolean("online") != null &&
                                                    userDoc.getBoolean("online");
                                            String avatar = userDoc.getString("avatarUrl");
                                            String name = userDoc.getString("name");

                                            // Create Contact object
                                            Contact friend = new Contact();
                                            friend.setId(friendId);
                                            friend.setName(name != null ? name : friendName);
                                            friend.setAvatarUrl(avatar != null ? avatar : friendAvatar);
                                            friend.setOnline(isOnline);
                                            friend.setStatus(isOnline ? "online" : "offline");
                                            friend.setPendingRequest(false);

                                            // Add to all friends (avoid duplicates)
                                            boolean alreadyExists = false;
                                            for (Contact c : allFriends) {
                                                if (c.getId().equals(friendId)) {
                                                    alreadyExists = true;
                                                    break;
                                                }
                                            }

                                            if (!alreadyExists) {
                                                allFriends.add(friend);
                                                Log.d(TAG, "✅ [FRIENDS] Added friend: " + name + " (online: " + isOnline + ")");
                                            }

                                            // Add to online friends if online
                                            if (isOnline) {
                                                boolean onlineExists = false;
                                                for (Contact c : onlineFriends) {
                                                    if (c.getId().equals(friendId)) {
                                                        onlineExists = true;
                                                        break;
                                                    }
                                                }
                                                if (!onlineExists) {
                                                    onlineFriends.add(friend);
                                                }
                                            }

                                            updateDisplayList();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "❌ [FRIENDS] Error getting user data for " + friendId + ": " + e.getMessage());
                                    });
                        }
                    }

                    Log.d(TAG, "✅ [FRIENDS] Loaded " + allFriends.size() + " total friends, " +
                            onlineFriends.size() + " online");
                    progressBar.setVisibility(View.GONE);
                });
    }

    // Load pending friend requests (only requests sent TO current user)
    private void loadPendingRequests() {
        if (requestsListener != null) requestsListener.remove();

        Log.d(TAG, "🔍 [REQUESTS] Looking for pending requests where receiverId = " + currentUserId);

        requestsListener = db.collection("friendRequests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        Log.e(TAG, "❌ [REQUESTS] Error loading requests: " + error);
                        return;
                    }

                    friendRequests.clear();
                    int count = snapshots.size();
                    Log.d(TAG, "📊 [REQUESTS] Found " + count + " pending requests");

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String senderId = doc.getString("senderId");
                        String senderName = doc.getString("senderName");
                        String senderProfilePicture = doc.getString("senderProfilePicture");

                        Log.d(TAG, "📝 [REQUESTS] Processing request from: " + senderName + " (ID: " + senderId + ")");

                        if (senderId != null) {
                            Contact request = new Contact();
                            request.setId(senderId);
                            request.setName(senderName != null ? senderName : "Unknown");
                            request.setAvatarUrl(senderProfilePicture);
                            request.setPendingRequest(true);

                            // Avoid duplicates
                            boolean exists = false;
                            for (Contact c : friendRequests) {
                                if (c.getId().equals(senderId)) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (!exists) {
                                friendRequests.add(request);
                                Log.d(TAG, "✅ [REQUESTS] Added request from: " + senderName);
                            }
                        }
                    }

                    updateDisplayList();
                });
    }

    private void updateDisplayList() {
        if (!isAdded()) return;

        switch (currentTab) {
            case "all":
                currentDisplayList = new ArrayList<>(allFriends);
                break;
            case "online":
                currentDisplayList = new ArrayList<>(onlineFriends);
                break;
            case "requests":
                currentDisplayList = new ArrayList<>(friendRequests);
                break;
        }

        filterList(etSearch.getText().toString());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterList(String query) {
        List<Contact> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Contact contact : currentDisplayList) {
            if (contact.getName() != null && contact.getName().toLowerCase().contains(lowerQuery)) {
                filtered.add(contact);
            }
        }

        // Update adapter with new filtered list
        contactAdapter = new ContactAdapter(filtered, requireContext());
        contactAdapter.setOnContactClickListener(contact -> {
            if (contact.isPendingRequest()) return;
            Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
            intent.putExtra("user_id", contact.getId());
            intent.putExtra("user_name", contact.getName());
            intent.putExtra("user_avatar", contact.getAvatarUrl());
            startActivity(intent);
        });
        rvContacts.setAdapter(contactAdapter);

        // Show empty state if no results
        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendsListener != null) friendsListener.remove();
        if (requestsListener != null) requestsListener.remove();
    }
}