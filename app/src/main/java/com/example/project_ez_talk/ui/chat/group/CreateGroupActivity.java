package com.example.project_ez_talk.ui.chat.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.ContactSelectionAdapter;
import com.example.project_ez_talk.model.Contact;
import com.example.project_ez_talk.ui.BaseActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class CreateGroupActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private Toolbar toolbar;
    private TextView tvMemberCount;
    private MaterialCardView cvGroupIcon;
    private ImageView ivGroupIcon;
    private EditText etGroupName, etGroupDescription, etSearchMembers;
    private RecyclerView rvSelectedMembers, rvContacts;

    private final List<Contact> selectedMembers = new ArrayList<>();
    private final List<Contact> allContacts = new ArrayList<>();
    private final List<Contact> filteredContacts = new ArrayList<>();

    private ContactSelectionAdapter contactAdapter;
    private ContactSelectionAdapter selectedMembersAdapter;  // ✅ NEW: Adapter for selected members
    private Uri groupImageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initFirebase();
        initViews();
        setupRecyclerViews();
        setupListeners();
        loadContacts();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("group_images");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cvGroupIcon = findViewById(R.id.cvGroupIcon);
        ivGroupIcon = findViewById(R.id.ivGroupIcon);
        etGroupName = findViewById(R.id.etGroupName);
        etGroupDescription = findViewById(R.id.etGroupDescription);
        etSearchMembers = findViewById(R.id.etSearchMembers);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        rvSelectedMembers = findViewById(R.id.rvSelectedMembers);
        rvContacts = findViewById(R.id.rvContacts);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        updateMemberCount();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupRecyclerViews() {
        // ✅ FIXED: Setup contacts list properly
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setNestedScrollingEnabled(false);  // ✅ Allow parent scroll

        contactAdapter = new ContactSelectionAdapter(filteredContacts, contact -> {
            if (contact.isSelected()) {
                selectedMembers.remove(contact);
            } else {
                selectedMembers.add(contact);
            }
            contact.setSelected(!contact.isSelected());
            contactAdapter.notifyDataSetChanged();
            selectedMembersAdapter.notifyDataSetChanged();  // ✅ Update selected list
            updateMemberCount();
        });

        rvContacts.setAdapter(contactAdapter);

        // ✅ FIXED: Setup selected members list with adapter
        rvSelectedMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSelectedMembers.setNestedScrollingEnabled(false);

        selectedMembersAdapter = new ContactSelectionAdapter(selectedMembers, contact -> {
            // Remove from selected
            selectedMembers.remove(contact);
            contact.setSelected(false);

            // Update both adapters
            contactAdapter.notifyDataSetChanged();
            selectedMembersAdapter.notifyDataSetChanged();
            updateMemberCount();
        });

        rvSelectedMembers.setAdapter(selectedMembersAdapter);
    }

    private void setupListeners() {
        // Back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Create button
        TextView btnCreate = findViewById(R.id.btnCreate);
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> createGroup());
        }

        // Group icon clicks
        cvGroupIcon.setOnClickListener(v -> selectGroupImage());
        ivGroupIcon.setOnClickListener(v -> selectGroupImage());

        // Search functionality
        etSearchMembers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void selectGroupImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadContacts() {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allContacts.clear();

                    for (var document : querySnapshot.getDocuments()) {
                        String userId = document.getId();

                        if (!userId.equals(currentUserId)) {
                            Contact contact = new Contact();
                            contact.setId(userId);

                            String name = document.getString("name");
                            String phone = document.getString("phoneNumber");
                            String avatar = document.getString("profilePicture");

                            contact.setName(name != null ? name : "Unknown User");
                            contact.setPhone(phone != null ? phone : "");
                            contact.setAvatarUrl(avatar != null ? avatar : "");
                            contact.setSelected(false);

                            allContacts.add(contact);
                        }
                    }

                    filteredContacts.clear();
                    filteredContacts.addAll(allContacts);
                    contactAdapter.notifyDataSetChanged();

                    Toast.makeText(CreateGroupActivity.this, "Loaded " + allContacts.size() + " contacts", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateGroupActivity.this, "Failed to load contacts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterContacts(String query) {
        filteredContacts.clear();

        if (TextUtils.isEmpty(query)) {
            filteredContacts.addAll(allContacts);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();

            for (Contact contact : allContacts) {
                String name = contact.getName() != null ? contact.getName().toLowerCase() : "";
                String phone = contact.getPhone() != null ? contact.getPhone() : "";

                if (name.contains(lowerCaseQuery) || phone.contains(lowerCaseQuery)) {
                    filteredContacts.add(contact);
                }
            }
        }

        contactAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    private void updateMemberCount() {
        int count = selectedMembers.size();
        tvMemberCount.setText(count + " selected");
        rvSelectedMembers.setVisibility(selectedMembers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void createGroup() {
        String name = etGroupName.getText().toString().trim();
        String desc = etGroupDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etGroupName.setError("Enter group name");
            etGroupName.requestFocus();
            return;
        }

        if (selectedMembers.isEmpty()) {
            Toast.makeText(this, "Add at least one member", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView btnCreate = findViewById(R.id.btnCreate);
        if (btnCreate != null) {
            btnCreate.setEnabled(false);
            btnCreate.setText("Creating...");
        }

        String groupId = db.collection("groups").document().getId();

        if (groupImageUri != null) {
            uploadGroupImageAndCreate(groupId, name, desc);
        } else {
            createGroupInDatabase(groupId, name, desc, null);
        }
    }

    private void uploadGroupImageAndCreate(String groupId, String groupName, String groupDesc) {
        StorageReference imageRef = storageRef.child(groupId + "_" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(groupImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                createGroupInDatabase(groupId, groupName, groupDesc, uri.toString())
                        )
                )
                .addOnFailureListener(e -> {
                    TextView btnCreate = findViewById(R.id.btnCreate);
                    if (btnCreate != null) {
                        btnCreate.setEnabled(true);
                        btnCreate.setText("Create");
                    }
                    Toast.makeText(CreateGroupActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void createGroupInDatabase(String groupId, String groupName, String groupDesc, String imageUrl) {
        String currentUserId = auth.getCurrentUser().getUid();

        Map<String, Boolean> members = new HashMap<>();
        members.put(currentUserId, true);
        for (Contact contact : selectedMembers) {
            members.put(contact.getId(), true);
        }

        List<String> memberIds = new ArrayList<>();
        memberIds.add(currentUserId);
        for (Contact contact : selectedMembers) {
            memberIds.add(contact.getId());
        }

        Map<String, String> memberRoles = new HashMap<>();
        memberRoles.put(currentUserId, "admin");
        for (Contact contact : selectedMembers) {
            memberRoles.put(contact.getId(), "member");
        }

        int memberCount = memberIds.size();

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("id", groupId);
        groupData.put("name", groupName);
        groupData.put("description", groupDesc);
        groupData.put("icon", imageUrl != null ? imageUrl : "");
        groupData.put("ownerId", currentUserId);
        groupData.put("createdAt", System.currentTimeMillis());
        groupData.put("lastMessage", "");
        groupData.put("lastMessageTime", System.currentTimeMillis());
        groupData.put("members", members);
        groupData.put("memberIds", memberIds);
        groupData.put("memberRoles", memberRoles);
        groupData.put("memberCount", memberCount);

        db.collection("groups")
                .document(groupId)
                .set(groupData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(CreateGroupActivity.this, "Group created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateGroupActivity.this, GroupChatActivity.class);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("groupName", groupName);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    TextView btnCreate = findViewById(R.id.btnCreate);
                    if (btnCreate != null) {
                        btnCreate.setEnabled(true);
                        btnCreate.setText("Create");
                    }
                    Toast.makeText(CreateGroupActivity.this, "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && requestCode == PICK_IMAGE_REQUEST) {
            groupImageUri = data.getData();
            Glide.with(this)
                    .load(groupImageUri)
                    .centerCrop()
                    .into(ivGroupIcon);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}