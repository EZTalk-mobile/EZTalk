package com.example.project_ez_talk.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.SearchResultAdapter;
import com.example.project_ez_talk.model.SearchResult;
import com.example.project_ez_talk.ui.chat.detail.ChatDetailActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private EditText etSearch;
    private ImageView btnClear;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvSearchResults;
    private View layoutEmpty;

    private SearchResultAdapter adapter;
    private List<SearchResult> searchResults = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews();
        setupRecyclerView();
        setupListeners();

        etSearch.requestFocus();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        btnClear = findViewById(R.id.btn_clear);
        chipGroupFilter = findViewById(R.id.chip_group_filter);
        rvSearchResults = findViewById(R.id.rv_search_results);
        layoutEmpty = findViewById(R.id.layout_empty);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        adapter = new SearchResultAdapter(searchResults, this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);

        // Click user → Show options dialog
        adapter.setOnItemClickListener(result -> showUserOptionsDialog(result));
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 0) {
                    btnClear.setVisibility(View.VISIBLE);
                    performSearch(query);
                } else {
                    btnClear.setVisibility(View.GONE);
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                    showEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
        });
    }

    private void performSearch(String query) {
        String lowerQuery = query.toLowerCase();

        if (lowerQuery.contains("@")) {
            searchByEmail(lowerQuery);
        } else {
            searchByName(lowerQuery);
        }
    }

    private void searchByName(String nameQuery) {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    searchResults.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        String userId = doc.getId();

                        // Don't show current user
                        if (userId.equals(currentUserId)) continue;

                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String profilePic = doc.getString("profilePicture");

                        if (name != null && name.toLowerCase().contains(nameQuery)) {
                            SearchResult result = new SearchResult();
                            result.setUserId(userId);
                            result.setTitle(name);
                            result.setSubtitle(email != null ? email : "");
                            result.setAvatarUrl(profilePic);
                            result.setType("contact");
                            result.setTime("");
                            searchResults.add(result);
                        }
                    }
                    updateResultsUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void searchByEmail(String emailQuery) {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    searchResults.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        String userId = doc.getId();

                        // Don't show current user
                        if (userId.equals(currentUserId)) continue;

                        String email = doc.getString("email");
                        String name = doc.getString("name");
                        String profilePic = doc.getString("profilePicture");

                        if (email != null && email.toLowerCase().equals(emailQuery)) {
                            SearchResult result = new SearchResult();
                            result.setUserId(userId);
                            result.setTitle(name != null ? name : "Unknown");
                            result.setSubtitle(email);
                            result.setAvatarUrl(profilePic);
                            result.setType("contact");
                            result.setTime("");
                            searchResults.add(result);
                        }
                    }
                    updateResultsUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    /**
     * Show options when user is clicked
     */
    private void showUserOptionsDialog(SearchResult user) {
        String[] options = {"Send Message", "View Profile", "Add Friend"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(user.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Send Message
                            openChat(user);
                            break;
                        case 1: // View Profile
                            Toast.makeText(this, "View profile - Coming soon", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Add Friend
                            addFriend(user);
                            break;
                    }
                })
                .show();
    }

    /**
     * Open ChatDetailActivity for private messaging
     */
    private void openChat(SearchResult user) {
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("user_id", user.getUserId());
        intent.putExtra("user_name", user.getTitle());
        intent.putExtra("user_avatar", user.getAvatarUrl());
        startActivity(intent);
        finish(); // Close search activity
    }

    /**
     * Add user as friend in Firestore
     */
    private void addFriend(SearchResult user) {
        // Add to current user's friends collection
        db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(user.getUserId())
                .set(new HashMap<String, Object>() {{
                    put("userId", user.getUserId());
                    put("name", user.getTitle());
                    put("email", user.getSubtitle());
                    put("avatarUrl", user.getAvatarUrl());
                    put("addedAt", System.currentTimeMillis());
                }})
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, user.getTitle() + " added as friend!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add friend", Toast.LENGTH_SHORT).show();
                });

        // Also add current user to their friends list (mutual friendship)
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String myName = doc.getString("name");
                    String myEmail = doc.getString("email");
                    String myAvatar = doc.getString("profilePicture");

                    db.collection("users")
                            .document(user.getUserId())
                            .collection("friends")
                            .document(currentUserId)
                            .set(new HashMap<String, Object>() {{
                                put("userId", currentUserId);
                                put("name", myName);
                                put("email", myEmail);
                                put("avatarUrl", myAvatar);
                                put("addedAt", System.currentTimeMillis());
                            }});
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateResultsUI() {
        adapter.notifyDataSetChanged();
        if (searchResults.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    public ChipGroup getChipGroupFilter() {
        return chipGroupFilter;
    }

    public void setChipGroupFilter(ChipGroup chipGroupFilter) {
        this.chipGroupFilter = chipGroupFilter;
    }
}