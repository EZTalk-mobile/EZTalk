package com.example.project_ez_talk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.Group;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private final List<Group> groups;
    private final Context context;
    private OnGroupClickListener clickListener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.clickListener = listener;
    }

    public GroupAdapter(List<Group> groups, Context context) {
        this.groups = groups;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ FIXED: Use item_group.xml for group list items
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);

        // Set group name
        holder.tvName.setText(group.getName() != null ? group.getName() : "Group");

        // Set member count
        int memberCount = group.getMembers() != null ? group.getMembers().size() : 0;
        holder.tvMembers.setText(memberCount + " member" + (memberCount == 1 ? "" : "s"));

        // Set time - format relative time
        if (group.getLastMessageTime() > 0) {
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    group.getLastMessageTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            holder.tvTime.setText(timeAgo);
        } else {
            holder.tvTime.setText("No messages");
        }

        // Load group icon/avatar
        if (group.getGroupIcon() != null && !group.getGroupIcon().isEmpty()) {
            Glide.with(context)
                    .load(group.getGroupIcon())
                    .centerCrop()
                    .placeholder(R.drawable.ic_group)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_group);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateGroups(List<Group> newGroups) {
        groups.clear();
        groups.addAll(newGroups);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvName, tvMembers, tvTime;

        @SuppressLint("WrongViewCast")
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}