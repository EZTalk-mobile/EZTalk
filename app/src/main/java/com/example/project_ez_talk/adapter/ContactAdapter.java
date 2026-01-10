package com.example.project_ez_talk.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.Contact;
import com.example.project_ez_talk.ui.chat.detail.ChatDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private static final String TAG = "ContactAdapter";

    private List<Contact> contactList = new ArrayList<>();
    private final Context context;

    @FunctionalInterface
    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    private OnContactClickListener clickListener;

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.clickListener = listener;
    }

    public ContactAdapter(List<Contact> contactList, Context context) {
        this.contactList = contactList != null ? contactList : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        holder.tvName.setText(contact.getName() != null ? contact.getName() : "Unknown");
        holder.vOnlineStatus.setVisibility(contact.isOnline() ? View.VISIBLE : View.GONE);

        if (contact.isPendingRequest()) {
            holder.llActions.setVisibility(View.VISIBLE);
            holder.btnAccept.setOnClickListener(v -> acceptFriendRequest(contact));
            holder.btnReject.setOnClickListener(v -> rejectFriendRequest(contact));
        } else {
            holder.llActions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!contact.isPendingRequest() && clickListener != null) {
                clickListener.onContactClick(contact);
            }
        });

        // Load avatar
        String avatarUrl = contact.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    // ==================== ACCEPT FRIEND REQUEST ====================
    private void acceptFriendRequest(Contact contact) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "🔄 [ACCEPT] Accepting friend request from: " + contact.getName());

        db.collection("friendRequests")
                .whereEqualTo("senderId", contact.getId())
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String requestId = querySnapshot.getDocuments().get(0).getId();

                        Log.d(TAG, "✅ [ACCEPT] Found request: " + requestId);

                        // Update the friend request to "accepted"
                        db.collection("friendRequests").document(requestId)
                                .update("status", "accepted")
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ [ACCEPT] Updated request status to 'accepted'");
                                    Toast.makeText(context, "Friend request accepted!", Toast.LENGTH_SHORT).show();

                                    // Get current user info and create chat
                                    db.collection("users").document(currentUserId).get()
                                            .addOnSuccessListener(currentUserDoc -> {
                                                if (currentUserDoc.exists()) {
                                                    String currentUserName = currentUserDoc.getString("name");
                                                    String currentUserAvatar = currentUserDoc.getString("avatarUrl");

                                                    createChatBetweenUsers(currentUserId, contact.getId(),
                                                            currentUserName, contact.getName(),
                                                            currentUserAvatar, contact.getAvatarUrl());
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ [ACCEPT] Error accepting request: " + e.getMessage());
                                    Toast.makeText(context, "Error accepting request", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.w(TAG, "❌ [ACCEPT] Friend request not found");
                        Toast.makeText(context, "Request not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ [ACCEPT] Error finding request: " + e.getMessage());
                    Toast.makeText(context, "Error finding request", Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== REJECT FRIEND REQUEST ====================
    private void rejectFriendRequest(Contact contact) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "🚫 [REJECT] Rejecting friend request from: " + contact.getName());

        db.collection("friendRequests")
                .whereEqualTo("senderId", contact.getId())
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String requestId = querySnapshot.getDocuments().get(0).getId();

                        db.collection("friendRequests").document(requestId)
                                .update("status", "rejected")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Request rejected", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "✅ [REJECT] Request rejected successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ [REJECT] Error rejecting request: " + e.getMessage());
                                    Toast.makeText(context, "Error rejecting request", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ [REJECT] Error finding request: " + e.getMessage());
                });
    }

    // ==================== CREATE CHAT BETWEEN USERS ====================
    private void createChatBetweenUsers(String currentUserId, String otherUserId,
                                        String currentUserName, String otherUserName,
                                        String currentUserAvatar, String otherUserAvatar) {
        String id1 = currentUserId;
        String id2 = otherUserId;
        String chatId = id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long timestamp = System.currentTimeMillis();

        Log.d(TAG, "💬 [CHAT] Creating chat between users: " + chatId);

        // Create main chat document
        db.collection("chats").document(chatId).set(new HashMap<String, Object>() {{
            put("id", chatId);
            put("participants", List.of(currentUserId, otherUserId));
            put("createdAt", timestamp);
        }});

        // Add chat to current user's chats list
        db.collection("users").document(currentUserId)
                .collection("chats").document(chatId).set(new HashMap<String, Object>() {{
                    put("id", chatId);
                    put("name", otherUserName);
                    put("avatarUrl", otherUserAvatar != null ? otherUserAvatar : "");
                    put("lastMessage", "");
                    put("lastMessageTimestamp", timestamp);
                    put("unreadCount", 0);
                }});

        // Add chat to other user's chats list
        db.collection("users").document(otherUserId)
                .collection("chats").document(chatId).set(new HashMap<String, Object>() {{
                    put("id", chatId);
                    put("name", currentUserName);
                    put("avatarUrl", currentUserAvatar != null ? currentUserAvatar : "");
                    put("lastMessage", "");
                    put("lastMessageTimestamp", timestamp);
                    put("unreadCount", 0);
                }});

        Log.d(TAG, "✅ [CHAT] Chat created: " + chatId);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateList(List<Contact> newList) {
        contactList.clear();
        contactList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        View vOnlineStatus;
        android.widget.ImageView ivAvatar;
        LinearLayout llActions;
        ImageButton btnAccept, btnReject;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            vOnlineStatus = itemView.findViewById(R.id.vOnlineStatus);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            llActions = itemView.findViewById(R.id.llActions);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}