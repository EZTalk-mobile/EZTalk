package com.example.project_ez_talk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private final Context context;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat, int position);
    }

    public ChatAdapter(Context context) {
        this.chatList = new ArrayList<>();
        this.context = context;
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // Name
        holder.tvName.setText(chat.getName() != null && !chat.getName().isEmpty()
                ? chat.getName() : "Unknown User");

        // Last message
        String lastMsg = chat.getLastMessage();
        holder.tvLastMessage.setText(lastMsg != null && !lastMsg.isEmpty()
                ? lastMsg : "No messages yet");

        // Time
        holder.tvTime.setText(chat.getFormattedTime());

        // Avatar
        if (chat.getAvatarUrl() != null && !chat.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(chat.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Unread badge
        int unreadCount = chat.getUnreadCount();
        if (unreadCount > 0) {
            holder.cvBadge.setVisibility(View.VISIBLE);
            holder.tvBadgeCount.setText(String.valueOf(unreadCount));
        } else {
            holder.cvBadge.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // Real-time update method
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Chat> newChatList) {
        this.chatList = new ArrayList<>(newChatList);
        notifyDataSetChanged();
    }

    // Optional: add single chat (for future use)
    public void addChat(Chat chat) {
        chatList.add(0, chat); // Add to top
        notifyItemInserted(0);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvLastMessage, tvTime, tvBadgeCount;
        CardView cvBadge;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            cvBadge = itemView.findViewById(R.id.cvBadge);
            tvBadgeCount = itemView.findViewById(R.id.tvBadgeCount);
        }
    }
}