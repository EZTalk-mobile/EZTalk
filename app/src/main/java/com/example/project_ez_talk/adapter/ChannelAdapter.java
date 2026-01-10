// File: app/java/com/example/project_ez_talk/adapter/ChannelAdapter.java

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
import com.example.project_ez_talk.model.Channel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

@SuppressWarnings("ALL")
public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private final List<Channel> channels;
    private final OnChannelClickListener listener;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);

        void onClick(Channel channel);
    }

    public ChannelAdapter(List<Channel> channels, OnChannelClickListener listener) {
        this.channels = channels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channels.get(position);

        holder.tvChannelName.setText(channel.getName());
        holder.tvChannelDescription.setText(channel.getDescription());
        holder.tvSubscriberCount.setText(
                holder.itemView.getContext().getString(R.string.subscriber_count, channel.getSubscriberCount())
        );

        // Load avatar
        Glide.with(holder.itemView.getContext())
                .load(channel.getAvatarUrl())
                .placeholder(R.drawable.ic_channel_default)
                .circleCrop()
                .into(holder.ivChannelAvatar);

        // Subscribe button text
        holder.btnSubscribe.setText("Subscribe");

        // Click listeners
        holder.itemView.setOnClickListener(v -> listener.onChannelClick(channel));
        holder.btnSubscribe.setOnClickListener(v -> {
            // TODO: Handle subscribe
            holder.btnSubscribe.setText("Subscribed");
            holder.btnSubscribe.setEnabled(false);
        });
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivChannelAvatar;
        TextView tvChannelName, tvChannelDescription, tvSubscriberCount;
        MaterialButton btnSubscribe;

        ChannelViewHolder(View itemView) {
            super(itemView);
            ivChannelAvatar = itemView.findViewById(R.id.iv_channel_avatar);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvChannelDescription = itemView.findViewById(R.id.tv_channel_description);
            tvSubscriberCount = itemView.findViewById(R.id.tv_subscriber_count);
            btnSubscribe = itemView.findViewById(R.id.btn_subscribe);
        }
    }
}