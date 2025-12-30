package com.example.project_ez_talk.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.GroupMember;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private final List<GroupMember> members;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(GroupMember member);
    }

    public GroupMemberAdapter(List<GroupMember> members) {
        this.members = members;
    }

    public GroupMemberAdapter(List<GroupMember> members, OnMemberClickListener listener) {
        this.members = members;
        this.listener = listener;
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMember member = members.get(position);

        // Set member name
        holder.tvName.setText(member.getName());

        // Show admin badge if user is admin
        if (member.isAdmin()) {
            holder.tvRole.setText("Admin");
            holder.tvRole.setVisibility(View.VISIBLE);
        } else {
            holder.tvRole.setVisibility(View.GONE);
        }

        // Load avatar
        if (member.getAvatarUrl() != null && !member.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(member.getAvatarUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Click listener
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onMemberClick(member));
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMembers(List<GroupMember> newMembers) {
        members.clear();
        members.addAll(newMembers);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;  // ✅ Changed to ImageView to match your layout
        TextView tvName;
        TextView tvRole;

        ViewHolder(View itemView) {
            super(itemView);
            // ✅✅✅ CRITICAL FIX: Use the CORRECT IDs from your layout (no underscores!)
            ivAvatar = itemView.findViewById(R.id.ivAvatar);  // Your layout uses ivAvatar
            tvName = itemView.findViewById(R.id.tvName);      // Your layout uses tvName
            tvRole = itemView.findViewById(R.id.tvRole);      // Your layout uses tvRole
        }
    }
}