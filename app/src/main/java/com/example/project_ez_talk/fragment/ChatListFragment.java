package com.example.project_ez_talk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.ChatAdapter;
import com.example.project_ez_talk.model.Chat;
import com.example.project_ez_talk.ui.chat.detail.ChatDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private RecyclerView rvChats;
    private ProgressBar progressBar;
    private LinearLayout llEmpty;

    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private ListenerRegistration chatsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChats = view.findViewById(R.id.rvChats);
        progressBar = view.findViewById(R.id.progressBar);
        llEmpty = view.findViewById(R.id.llEmpty);

        setupRecyclerView();
        loadRealTimeChats();
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(requireContext());
        rvChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChats.setAdapter(chatAdapter);

        chatAdapter.setOnChatClickListener((chat, position) -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String[] users = chat.getId().split("_");
            String otherUserId = users[0].equals(currentUserId) ? users[1] : users[0];

            Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
            intent.putExtra("user_id", otherUserId);
            intent.putExtra("user_name", chat.getName());
            intent.putExtra("user_avatar", chat.getAvatarUrl());
            startActivity(intent);
        });
    }

    private void loadRealTimeChats() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId == null) {
            showEmptyState();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        chatsListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("chats")
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null || snapshots == null) {
                        showEmptyState();
                        return;
                    }

                    chatList.clear();
                    for (var doc : snapshots.getDocuments()) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat != null) {
                            chat.setId(doc.getId());
                            chatList.add(chat);
                        }
                    }

                    chatAdapter.updateData(chatList);

                    if (chatList.isEmpty()) {
                        showEmptyState();
                    } else {
                        rvChats.setVisibility(View.VISIBLE);
                        llEmpty.setVisibility(View.GONE);
                    }
                });
    }

    private void showEmptyState() {
        rvChats.setVisibility(View.GONE);
        llEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatsListener != null) {
            chatsListener.remove();
        }
    }
}