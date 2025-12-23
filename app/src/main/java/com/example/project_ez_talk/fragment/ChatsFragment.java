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

public class ChatsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";

    private EditText etSearch;
    private TabLayout tabLayout;
    private RecyclerView rvContacts;
    private View layoutEmpty;
    private ProgressBar progressBar;

    private ContactAdapter contactAdapter;
    private final List<Contact> allFriends = new ArrayList<>();
    private final List<Contact> onlineFriends = new ArrayList<>();
    private final List<Contact> friendRequests = new ArrayList<>();
    private List<Contact> currentSourceList = new ArrayList<>();

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

        initViews(view);
        setupRecyclerView();  // ← Adapter created first
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
        contactAdapter = new ContactAdapter(new ArrayList<>(), requireContext()); // Start with empty list
        contactAdapter.setOnContactClickListener(contact -> {
            if (contact.isPendingRequest()) return;
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
                if (position == 0) {
                    currentTab = "all";
                    currentSourceList = allFriends;
                } else if (position == 1) {
                    currentTab = "online";
                    currentSourceList = onlineFriends;
                } else {
                    currentTab = "requests";
                    currentSourceList = friendRequests;
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

    private void loadAcceptedFriends() {
        if (friendsListener != null) friendsListener.remove();

        friendsListener = db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    allFriends.clear();
                    onlineFriends.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Contact friend = doc.toObject(Contact.class);
                        if (friend != null) {
                            friend.setId(doc.getId());
                            friend.setPendingRequest(false);
                            allFriends.add(friend);
                            if (friend.isOnline()) {
                                onlineFriends.add(friend);
                            }
                        }
                    }

                    updateDisplayList();
                });
    }

    private void loadPendingRequests() {
        if (requestsListener != null) requestsListener.remove();

        requestsListener = db.collection("friendRequests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    friendRequests.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String senderId = doc.getString("senderId");
                        if (senderId != null) {
                            db.collection("users").document(senderId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            Contact request = userDoc.toObject(Contact.class);
                                            if (request != null) {
                                                request.setId(senderId);
                                                request.setPendingRequest(true);
                                                friendRequests.add(request);
                                                updateDisplayList();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void updateDisplayList() {
        if (!isAdded()) return;

        switch (currentTab) {
            case "all":
                currentSourceList = allFriends;
                break;
            case "online":
                currentSourceList = onlineFriends;
                break;
            case "requests":
                currentSourceList = friendRequests;
                break;
        }

        filterList(etSearch.getText().toString());
        progressBar.setVisibility(View.GONE);
    }

    private void filterList(String query) {
        List<Contact> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Contact contact : currentSourceList) {
            if (contact.getName() != null && contact.getName().toLowerCase().contains(lowerQuery)) {
                filtered.add(contact);
            }
        }

        // Update the existing adapter instead of creating new one
        contactAdapter.updateList(filtered);

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