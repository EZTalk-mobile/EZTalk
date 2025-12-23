package com.example.project_ez_talk.ui.chat.group;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class GroupSettingsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = "groupId";
    private static final int PICK_IMAGE_REQUEST = 200;

    private String groupId;
    private ShapeableImageView ivGroupIcon;
    private MaterialCardView cvGroupIcon;
    private EditText etGroupName, etGroupDescription;
    private MaterialButton btnSave, btnCancel;
    private Uri newImageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    public static GroupSettingsBottomSheet newInstance(String groupId) {
        GroupSettingsBottomSheet fragment = new GroupSettingsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("group_images");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_group_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadGroupData();
        setupListeners();
    }

    private void initViews(View view) {
        ivGroupIcon = view.findViewById(R.id.iv_group_icon);
        cvGroupIcon = view.findViewById(R.id.cv_group_icon);
        etGroupName = view.findViewById(R.id.et_group_name);
        etGroupDescription = view.findViewById(R.id.et_group_description);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void loadGroupData() {
        // Load current group data in real-time
        db.collection("groups")
                .document(groupId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load group data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        etGroupName.setText(doc.getString("name"));
                        etGroupDescription.setText(doc.getString("description"));

                        String iconUrl = doc.getString("icon");
                        if (iconUrl != null && !iconUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(iconUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_group)
                                    .into(ivGroupIcon);
                        }
                    }
                });
    }

    private void setupListeners() {
        // Change group icon
        cvGroupIcon.setOnClickListener(v -> selectGroupImage());
        ivGroupIcon.setOnClickListener(v -> selectGroupImage());

        // Save changes
        btnSave.setOnClickListener(v -> saveChanges());

        // Cancel
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }
    }

    private void selectGroupImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void saveChanges() {
        String name = etGroupName.getText().toString().trim();
        String desc = etGroupDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etGroupName.setError("Enter group name");
            etGroupName.requestFocus();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        if (newImageUri != null) {
            // Upload new image first
            uploadImageAndSave(name, desc);
        } else {
            // Just update text fields
            updateGroupInfo(name, desc, null);
        }
    }

    private void uploadImageAndSave(String name, String desc) {
        StorageReference imageRef = storageRef.child(groupId + "_" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(newImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                updateGroupInfo(name, desc, uri.toString())
                        )
                )
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGroupInfo(String name, String desc, String imageUrl) {
        // Prepare update data
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", desc);
        if (imageUrl != null) {
            updates.put("icon", imageUrl);
        }

        // Update group document - this will trigger real-time update in GroupDetailsActivity
        db.collection("groups")
                .document(groupId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    // Also update in all members' chat lists for real-time sync
                    updateMembersChats(name, imageUrl);

                    Toast.makeText(getContext(), "Group updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    Toast.makeText(getContext(), "Failed to update group", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateMembersChats(String name, String imageUrl) {
        // Get all members
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Boolean> members = (Map<String, Boolean>) doc.get("members");
                        if (members != null) {
                            // Update each member's chat list with new group info
                            for (String userId : members.keySet()) {
                                Map<String, Object> chatUpdates = new HashMap<>();
                                chatUpdates.put("name", name);
                                if (imageUrl != null) {
                                    chatUpdates.put("icon", imageUrl);
                                }

                                db.collection("users")
                                        .document(userId)
                                        .collection("chats")
                                        .document(groupId)
                                        .update(chatUpdates);
                            }
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            newImageUri = data.getData();

            // Display selected image immediately
            Glide.with(this)
                    .load(newImageUri)
                    .centerCrop()
                    .into(ivGroupIcon);
        }
    }
}