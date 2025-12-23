package com.example.project_ez_talk.ui.chat.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.GroupMemberAdapter;
import com.example.project_ez_talk.model.GroupMember;
import com.example.project_ez_talk.ui.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class GroupDetailsActivity extends BaseActivity {

    private Toolbar toolbar;
    private ShapeableImageView ivGroupIcon;
    private TextView tvGroupName, tvMemberCount, tvGroupDescription;
    private ImageView btnEditGroup;
    private LinearLayout btnVoiceCall, btnVideoCall, btnSearch, btnMedia;
    private LinearLayout btnMuteNotifications, btnReportGroup, btnExitGroup;
    private SwitchMaterial switchMute;
    private RecyclerView rvMembers;

    private NestedScrollView nestedScrollView;
    private LinearLayout settingsSection;
    private ImageView btnScrollToSettings;

    private String groupId;
    private String currentUserId;
    private boolean isAdmin = false;

    private GroupMemberAdapter memberAdapter;
    private List<GroupMember> memberList = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initFirebase();
        initViews();
        setupListeners();
        loadGroupDetails();
        loadMembers();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivGroupIcon = findViewById(R.id.iv_group_icon);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvMemberCount = findViewById(R.id.tv_member_count);
        tvGroupDescription = findViewById(R.id.tv_group_description);
        btnEditGroup = findViewById(R.id.btn_edit_group);

        btnVoiceCall = findViewById(R.id.btn_voice_call);
        btnVideoCall = findViewById(R.id.btn_video_call);
        btnSearch = findViewById(R.id.btn_search);
        btnMedia = findViewById(R.id.btn_media);

        rvMembers = findViewById(R.id.rv_members);

        btnMuteNotifications = findViewById(R.id.btn_mute_notifications);
        switchMute = findViewById(R.id.switch_mute);
        btnReportGroup = findViewById(R.id.btn_report_group);
        btnExitGroup = findViewById(R.id.btn_exit_group);

        nestedScrollView = findViewById(R.id.nested_scroll_view);
        settingsSection = findViewById(R.id.settings_section);
        btnScrollToSettings = findViewById(R.id.btn_scroll_to_settings);

        // Set toolbar as ActionBar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // === FINAL FIX: Prevent overlapping view from blocking back arrow ===
        if (btnScrollToSettings != null) {
            btnScrollToSettings.setOnTouchListener((v, event) -> {
                if (event.getX() < dpToPx(80)) {
                    return false; // Let back arrow handle touch
                }
                return false;
            });
        }

        // Setup RecyclerView
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new GroupMemberAdapter(memberList);
        rvMembers.setAdapter(memberAdapter);
    }

    private void setupListeners() {
        btnEditGroup.setOnClickListener(v -> openEditGroupBottomSheet());

        if (btnScrollToSettings != null) {
            btnScrollToSettings.setOnClickListener(v -> scrollToSettings());
        }

        btnVoiceCall.setOnClickListener(v -> Toast.makeText(this, "Voice call feature coming soon", Toast.LENGTH_SHORT).show());
        btnVideoCall.setOnClickListener(v -> Toast.makeText(this, "Video call feature coming soon", Toast.LENGTH_SHORT).show());
        btnSearch.setOnClickListener(v -> Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show());
        btnMedia.setOnClickListener(v -> Toast.makeText(this, "Media gallery coming soon", Toast.LENGTH_SHORT).show());

        switchMute.setOnCheckedChangeListener((buttonView, isChecked) -> updateMuteStatus(isChecked));
        btnMuteNotifications.setOnClickListener(v -> switchMute.toggle());

        btnReportGroup.setOnClickListener(v -> showReportDialog());
        btnExitGroup.setOnClickListener(v -> showExitGroupDialog());
    }

    private void scrollToSettings() {
        if (nestedScrollView != null && settingsSection != null) {
            nestedScrollView.post(() -> nestedScrollView.smoothScrollTo(0, settingsSection.getTop()));
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadGroupDetails() {
        db.collection("groups")
                .document(groupId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                        Toast.makeText(this, "Failed to load group details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String name = documentSnapshot.getString("name");
                    String description = documentSnapshot.getString("description");
                    String iconUrl = documentSnapshot.getString("icon");
                    Map<String, Boolean> members = (Map<String, Boolean>) documentSnapshot.get("members");
                    Map<String, String> memberRoles = (Map<String, String>) documentSnapshot.get("memberRoles");

                    if (name != null) tvGroupName.setText(name);
                    if (description != null && !description.isEmpty()) {
                        tvGroupDescription.setText(description);
                        tvGroupDescription.setVisibility(View.VISIBLE);
                    } else {
                        tvGroupDescription.setVisibility(View.GONE);
                    }

                    if (iconUrl != null && !iconUrl.isEmpty()) {
                        Glide.with(this).load(iconUrl).centerCrop().placeholder(R.drawable.ic_group).into(ivGroupIcon);
                    }

                    if (members != null) {
                        int count = members.size();
                        tvMemberCount.setText(count + (count == 1 ? " member" : " members"));
                    }

                    if (memberRoles != null) {
                        String role = memberRoles.get(currentUserId);
                        isAdmin = "admin".equals(role);
                        btnEditGroup.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void loadMembers() {
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    Map<String, Boolean> members = (Map<String, Boolean>) documentSnapshot.get("members");
                    Map<String, String> memberRoles = (Map<String, String>) documentSnapshot.get("memberRoles");

                    if (members == null) return;

                    memberList.clear();
                    int total = members.size();
                    final int[] loaded = {0};

                    for (String userId : members.keySet()) {
                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String name = userDoc.getString("name");
                                        String avatar = userDoc.getString("profilePicture");
                                        boolean admin = memberRoles != null && "admin".equals(memberRoles.get(userId));

                                        memberList.add(new GroupMember(userId, name != null ? name : "Unknown User", avatar != null ? avatar : "", admin));
                                    }
                                    loaded[0]++;
                                    if (loaded[0] == total) {
                                        memberAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load members", Toast.LENGTH_SHORT).show());
    }

    private void openEditGroupBottomSheet() {
        if (!isAdmin) {
            Toast.makeText(this, "Only admins can edit group", Toast.LENGTH_SHORT).show();
            return;
        }
        GroupSettingsBottomSheet bottomSheet = GroupSettingsBottomSheet.newInstance(groupId);
        bottomSheet.show(getSupportFragmentManager(), "GroupSettings");
    }

    private void updateMuteStatus(boolean muted) {
        db.collection("users")
                .document(currentUserId)
                .collection("groupSettings")
                .document(groupId)
                .set(new java.util.HashMap<String, Object>() {{ put("muted", muted); }}, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> Toast.makeText(this, muted ? "Notifications muted" : "Notifications unmuted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update settings", Toast.LENGTH_SHORT).show());
    }

    private void showReportDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Report Group")
                .setMessage("Are you sure you want to report this group?")
                .setPositiveButton("Report", (dialog, which) -> Toast.makeText(this, "Group reported", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showExitGroupDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit Group")
                .setMessage("Are you sure you want to exit this group?")
                .setPositiveButton("Exit", (dialog, which) -> exitGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exitGroup() {
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    Map<String, Boolean> members = (Map<String, Boolean>) documentSnapshot.get("members");
                    Map<String, String> memberRoles = (Map<String, String>) documentSnapshot.get("memberRoles");

                    if (members != null) members.remove(currentUserId);
                    if (memberRoles != null) memberRoles.remove(currentUserId);

                    db.collection("groups")
                            .document(groupId)
                            .update("members", members, "memberRoles", memberRoles)
                            .addOnSuccessListener(unused -> {
                                db.collection("users")
                                        .document(currentUserId)
                                        .collection("chats")
                                        .document(groupId)
                                        .delete();
                                Toast.makeText(this, "You left the group", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to exit group", Toast.LENGTH_SHORT).show());
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Helper for touch blocking fix
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}