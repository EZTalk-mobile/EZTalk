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
import android.widget.ImageView;
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

@SuppressWarnings("ALL")
public class GroupSettingsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = "groupId";
    private static final int PICK_IMAGE_REQUEST = 200;

    private String groupId;
    private ShapeableImageView ivGroupIcon;
    private MaterialCardView cvGroupIcon;
    private EditText etGroupName, etGroupDescription;
    private MaterialButton btnSave, btnCancel;
    private ImageView btnClose, btnChangeIcon;
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
        View view = inflater.inflate(R.layout.bottom_sheet_group_settings, container, false);
        if (view == null) {
            Toast.makeText(getContext(), "Failed to load layout", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        if (allViewsInitialized()) {
            loadGroupData();
            setupListeners();
        } else {
            Toast.makeText(getContext(),
                    "Layout error: Some views not found. Check XML IDs match Java code.",
                    Toast.LENGTH_LONG).show();
            dismiss();
        }
    }

    private void initViews(View view) {
        try {
            ivGroupIcon = view.findViewById(R.id.iv_group_icon);
            cvGroupIcon = view.findViewById(R.id.cv_group_icon);
            etGroupName = view.findViewById(R.id.et_group_name);
            etGroupDescription = view.findViewById(R.id.et_group_description);
            btnSave = view.findViewById(R.id.btn_save);
            btnCancel = view.findViewById(R.id.btn_cancel);
            btnClose = view.findViewById(R.id.btn_close);
            btnChangeIcon = view.findViewById(R.id.btn_change_icon);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error initializing views: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean allViewsInitialized() {
        return ivGroupIcon != null && cvGroupIcon != null &&
                etGroupName != null && etGroupDescription != null &&
                btnSave != null && btnCancel != null;
    }

    private void loadGroupData() {
        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid group ID", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        db.collection("groups")
                .document(groupId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load group data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        try {
                            String name = doc.getString("name");
                            String description = doc.getString("description");
                            String iconUrl = doc.getString("icon");

                            if (name != null && !name.isEmpty() && etGroupName != null) {
                                etGroupName.setText(name);
                            }

                            if (description != null && !description.isEmpty() && etGroupDescription != null) {
                                etGroupDescription.setText(description);
                            }

                            if (iconUrl != null && !iconUrl.isEmpty() && ivGroupIcon != null) {
                                Glide.with(GroupSettingsBottomSheet.this)
                                        .load(iconUrl)
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_group)
                                        .into(ivGroupIcon);
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error loading group data: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupListeners() {
        // Change group icon
        if (cvGroupIcon != null) {
            cvGroupIcon.setOnClickListener(v -> selectGroupImage());
        }
        if (ivGroupIcon != null) {
            ivGroupIcon.setOnClickListener(v -> selectGroupImage());
        }
        if (btnChangeIcon != null) {
            btnChangeIcon.setOnClickListener(v -> selectGroupImage());
        }

        // Close button
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        // Save changes
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveChanges());
        }

        // Cancel
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }
    }

    private void selectGroupImage() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot open image picker: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        if (etGroupName == null || etGroupDescription == null || btnSave == null) {
            Toast.makeText(getContext(), "Form not initialized properly", Toast.LENGTH_SHORT).show();
            return;
        }

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
            uploadImageAndSave(name, desc);
        } else {
            updateGroupInfo(name, desc, null);
        }
    }

    private void uploadImageAndSave(String name, String desc) {
        try {
            StorageReference imageRef = storageRef.child(groupId + "_" + System.currentTimeMillis() + ".jpg");

            imageRef.putFile(newImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                    updateGroupInfo(name, desc, uri.toString())
                            )
                    )
                    .addOnFailureListener(e -> {
                        resetSaveButton();
                        Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            resetSaveButton();
            Toast.makeText(getContext(), "Error uploading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGroupInfo(String name, String desc, String imageUrl) {
        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid group ID", Toast.LENGTH_SHORT).show();
            resetSaveButton();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", desc);
        if (imageUrl != null) {
            updates.put("icon", imageUrl);
        }

        db.collection("groups")
                .document(groupId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    updateMembersChats(name, imageUrl);
                    Toast.makeText(getContext(), "Group updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    resetSaveButton();
                    Toast.makeText(getContext(), "Failed to update group: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateMembersChats(String name, String imageUrl) {
        if (groupId == null || groupId.isEmpty()) {
            return;
        }

        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Boolean> members = (Map<String, Boolean>) doc.get("members");
                        if (members != null) {
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
                                        .update(chatUpdates)
                                        .addOnFailureListener(e -> {
                                            // Log but don't crash
                                            android.util.Log.e("GroupSettings", "Failed to update member chat: " + e.getMessage());
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("GroupSettings", "Failed to get group members: " + e.getMessage());
                });
    }

    private void resetSaveButton() {
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setText("Save");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            try {
                newImageUri = data.getData();

                if (ivGroupIcon != null) {
                    Glide.with(this)
                            .load(newImageUri)
                            .centerCrop()
                            .into(ivGroupIcon);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error loading image: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}