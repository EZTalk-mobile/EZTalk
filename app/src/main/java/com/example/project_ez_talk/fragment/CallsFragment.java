package com.example.project_ez_talk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.CallLogAdapter;
import com.example.project_ez_talk.model.CallLog;
import com.example.project_ez_talk.repository.CallRepository;
<<<<<<< HEAD
import com.example.project_ez_talk.ui.call.video.IntegratedVideoCallActivity;
=======
import com.example.project_ez_talk.ui.call.video.VideoCallActivity;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import com.example.project_ez_talk.ui.call.voice.VoiceCallActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
<<<<<<< HEAD
import com.google.firebase.database.Query;
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallsFragment extends Fragment {
    private static final String TAG = "CallsFragment";
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";

    private RecyclerView rvCallHistory;
    private TabLayout tabLayout;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private CallLogAdapter adapter;
    private List<CallLog> allCallLogs = new ArrayList<>();
    private List<CallLog> filteredCallLogs = new ArrayList<>();

    private DatabaseReference callLogsRef;
    private String currentUserId;
    private ValueEventListener callLogsListener;

    private boolean showOnlyMissed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calls, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        callLogsRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("call_logs");

<<<<<<< HEAD
        Log.d(TAG, "🔗 Connected to Firebase: " + DATABASE_URL);
        Log.d(TAG, "👤 Current User ID: " + currentUserId);
=======
        Log.d(TAG, "✅ Current User ID: " + currentUserId);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

        initViews(view);
        setupRecyclerView();
        setupTabs();
        initCallRepository();
        loadCallLogs();
    }

    private void initViews(View view) {
        rvCallHistory = view.findViewById(R.id.rvCallHistory);
        tabLayout = view.findViewById(R.id.tabLayout);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new CallLogAdapter(filteredCallLogs, requireContext(),
                new CallLogAdapter.OnCallLogClickListener() {
                    @Override
                    public void onCallClick(CallLog callLog) {
                        redialCall(callLog);
                    }

                    @Override
                    public void onDeleteClick(CallLog callLog) {
                        showDeleteConfirmation(callLog);
                    }
                });

        rvCallHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCallHistory.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showOnlyMissed = tab.getPosition() == 1;
                filterCallLogs();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadCallLogs() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

<<<<<<< HEAD
        Query query = callLogsRef.orderByChild("startTime");

        callLogsListener = query.addValueEventListener(new ValueEventListener() {
=======
        if (callLogsListener != null) {
            callLogsRef.removeEventListener(callLogsListener);
        }

        callLogsListener = callLogsRef.addValueEventListener(new ValueEventListener() {
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCallLogs.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        CallLog log = data.getValue(CallLog.class);

<<<<<<< HEAD
                        // ✅ FIX: Check for null and validate data
                        if (log != null && isValidCallLog(log)) {
=======
                        if (log != null && isValidCallLog(log)) {
                            // Set the callId from the key
                            log.setCallId(data.getKey());

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                            // Only show logs where current user is involved
                            if (log.getCallerId().equals(currentUserId) ||
                                    log.getReceiverId().equals(currentUserId)) {
                                allCallLogs.add(log);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing call log", e);
                    }
                }

                // Sort by time descending (newest first)
                Collections.sort(allCallLogs, (a, b) ->
                        Long.compare(b.getStartTime(), a.getStartTime()));

                filterCallLogs();
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "✅ Loaded " + allCallLogs.size() + " call logs");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Failed to load call logs: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(),
                        "Failed to load calls: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Helper method to validate call log data
    private boolean isValidCallLog(CallLog log) {
<<<<<<< HEAD
        return log.getCallId() != null &&
                log.getCallerId() != null &&
                log.getReceiverId() != null &&
                log.getStatus() != null &&
=======
        // callId is set by Firestore, so we only check other required fields
        return log.getCallerId() != null &&
                log.getReceiverId() != null &&
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                log.getCallType() != null;
    }

    private void filterCallLogs() {
        filteredCallLogs.clear();

        if (showOnlyMissed) {
<<<<<<< HEAD
            // Show only missed calls where current user is receiver
            for (CallLog log : allCallLogs) {
                if ("missed".equals(log.getStatus()) &&
                        !log.getCallerId().equals(currentUserId)) {
=======
            // Show missed/rejected calls where current user is receiver
            for (CallLog log : allCallLogs) {
                String status = log.getStatus();
                boolean isReceiver = log.getReceiverId().equals(currentUserId);

                if (isReceiver && ("missed".equals(status) || "rejected".equals(status))) {
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                    filteredCallLogs.add(log);
                }
            }
        } else {
            // Show all calls
            filteredCallLogs.addAll(allCallLogs);
        }

        adapter.notifyDataSetChanged();

        // Update empty state
        if (filteredCallLogs.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCallHistory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCallHistory.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "📊 Filtered: " + filteredCallLogs.size() + " calls");
    }

    private void initCallRepository() {
        try {
            CallRepository.getInstance().init(
                    requireContext(),
                    currentUserId,
                    new CallRepository.OnInitListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "✅ Call repository initialized");
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(requireContext(),
                                    "Failed to initialize calls",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error initializing call repository", e);
        }
    }

    private void redialCall(CallLog callLog) {
        // Determine target user
        boolean isIncoming = !callLog.getCallerId().equals(currentUserId);
        String targetUserId = isIncoming ? callLog.getCallerId() : callLog.getReceiverId();
        String targetUserName = isIncoming ? callLog.getCallerName() : callLog.getReceiverName();
        String targetAvatar = isIncoming ? callLog.getCallerAvatar() : callLog.getReceiverAvatar();

        Intent intent;
        if ("video".equals(callLog.getCallType())) {
<<<<<<< HEAD
            intent = new Intent(requireContext(), IntegratedVideoCallActivity.class);
=======
            intent = new Intent(requireContext(), VideoCallActivity.class);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
        } else {
            intent = new Intent(requireContext(), VoiceCallActivity.class);
        }

<<<<<<< HEAD
        intent.putExtra("user_id", targetUserId);
        intent.putExtra("user_name", targetUserName != null ? targetUserName : "Unknown");
        intent.putExtra("user_avatar", targetAvatar != null ? targetAvatar : "");
        intent.putExtra("current_user_id", currentUserId);
        intent.putExtra("is_incoming", false);
=======
        intent.putExtra(VideoCallActivity.EXTRA_USER_ID, targetUserId);
        intent.putExtra(VideoCallActivity.EXTRA_USER_NAME, targetUserName != null ? targetUserName : "Unknown");
        intent.putExtra(VideoCallActivity.EXTRA_USER_AVATAR, targetAvatar != null ? targetAvatar : "");
        intent.putExtra(VideoCallActivity.EXTRA_IS_INCOMING, false);
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

        startActivity(intent);
    }

    private void showDeleteConfirmation(CallLog callLog) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete call log?")
                .setMessage("This action cannot be undone")
                .setPositiveButton("Delete", (dialog, which) -> deleteCallLog(callLog))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCallLog(CallLog callLog) {
        if (callLog.getCallId() != null) {
<<<<<<< HEAD
            callLogsRef.child(callLog.getCallId()).removeValue()
=======
            callLogsRef.child(callLog.getCallId())
                    .removeValue()
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(),
                                "Call log deleted",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Failed to delete: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (callLogsListener != null && callLogsRef != null) {
            callLogsRef.removeEventListener(callLogsListener);
        }
    }
<<<<<<< HEAD
}
=======
    }
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
