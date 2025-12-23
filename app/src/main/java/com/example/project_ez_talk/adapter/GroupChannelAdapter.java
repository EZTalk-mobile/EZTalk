package com.example.project_ez_talk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.GroupChannelItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChannelAdapter extends RecyclerView.Adapter<GroupChannelAdapter.ViewHolder> {

    private List<GroupChannelItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GroupChannelItem item);
    }

    public GroupChannelAdapter(List<GroupChannelItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupChannelItem item = items.get(position);

        // ✅ Set name
        if (holder.tvName != null) {
            holder.tvName.setText(item.getName() != null ? item.getName() : "Unknown");
        }

        // ✅ Set last message
        if (holder.tvLastMessage != null) {
            String lastMessage = item.getLastMessage();
            if (lastMessage != null && !lastMessage.isEmpty()) {
                holder.tvLastMessage.setText(lastMessage);
            } else {
                holder.tvLastMessage.setText("No messages yet");
            }
        }

        // ✅ Set timestamp
        if (holder.tvTime != null) {
            if (item.getTimestamp() > 0) {
                holder.tvTime.setText(formatTimestamp(item.getTimestamp()));
                holder.tvTime.setVisibility(View.VISIBLE);
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }
        }

        // ✅ Set member count
        if (holder.tvMembers != null) {
            if ("group".equals(item.getType())) {
                if (item.getMemberCount() > 0) {
                    holder.tvMembers.setText(item.getMemberCount() + " members");
                    holder.tvMembers.setVisibility(View.VISIBLE);
                } else {
                    holder.tvMembers.setVisibility(View.GONE);
                }
            } else if ("channel".equals(item.getType())) {
                if (item.getMemberCount() > 0) {
                    holder.tvMembers.setText(item.getMemberCount() + " subscribers");
                    holder.tvMembers.setVisibility(View.VISIBLE);
                } else {
                    holder.tvMembers.setVisibility(View.GONE);
                }
            } else {
                holder.tvMembers.setVisibility(View.GONE);
            }
        }

        // ✅ Set icon based on type
        if (holder.ivAvatar != null) {
            if ("group".equals(item.getType())) {
                // Group icon
                if (item.getAvatarUrl() != null && !item.getAvatarUrl().isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(item.getAvatarUrl())
                            .placeholder(R.drawable.ic_group)
                            .circleCrop()
                            .into(holder.ivAvatar);
                } else {
                    holder.ivAvatar.setImageResource(R.drawable.ic_group);
                }
            } else if ("channel".equals(item.getType())) {
                // Channel icon
                if (item.getAvatarUrl() != null && !item.getAvatarUrl().isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(item.getAvatarUrl())
                            .placeholder(R.drawable.ic_channel)
                            .circleCrop()
                            .into(holder.ivAvatar);
                } else {
                    holder.ivAvatar.setImageResource(R.drawable.ic_channel);
                }
            }
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        Date now = new Date();

        long diff = now.getTime() - date.getTime();
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);

        if (hours < 1) {
            long minutes = diff / (1000 * 60);
            return minutes + "m ago";
        } else if (hours < 24) {
            return hours + "h ago";
        } else if (days < 7) {
            return days + "d ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(date);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvLastMessage;
        TextView tvTime;          // ✅ Changed from tvTimestamp to tvTime
        TextView tvMembers;       // ✅ Added for member count
        TextView tvUnreadCount;   // ✅ Added for unread badge

        ViewHolder(View itemView) {
            super(itemView);

            // ✅ Match the IDs from your layout file
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}