package com.example.project_ez_talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.Message;
import com.example.project_ez_talk.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private Context context;
    private String currentUserId;

    private static final int TYPE_TEXT_SENT = 1;
    private static final int TYPE_TEXT_RECEIVED = 2;
    private static final int TYPE_IMAGE_SENT = 3;
    private static final int TYPE_IMAGE_RECEIVED = 4;

    // ✅ Constructor with Context
    public MessageAdapter(List<Message> messageList, Context context) {
        this.messages = messageList != null ? messageList : new ArrayList<>();
        this.context = context;
        this.currentUserId = FirebaseUtils.getCurrentUserId();
    }

    // ✅ Overloaded constructor with currentUserId
    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messages = messageList != null ? messageList : new ArrayList<>();
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        boolean isSentByMe = msg.isSentByMe(currentUserId);

        // ✅ FIXED: Use getTypeEnum() to get the enum, then compare
        Message.MessageType messageType = msg.getTypeEnum();

        if (messageType == Message.MessageType.IMAGE) {
            return isSentByMe ? TYPE_IMAGE_SENT : TYPE_IMAGE_RECEIVED;
        } else {
            return isSentByMe ? TYPE_TEXT_SENT : TYPE_TEXT_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_TEXT_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new TextSentVH(view);
        } else if (viewType == TYPE_TEXT_RECEIVED) {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new TextReceivedVH(view);
        } else if (viewType == TYPE_IMAGE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new ImageSentVH(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ImageReceivedVH(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);

        if (holder instanceof TextSentVH) {
            ((TextSentVH) holder).bind(msg);
        } else if (holder instanceof TextReceivedVH) {
            ((TextReceivedVH) holder).bind(msg);
        } else if (holder instanceof ImageSentVH) {
            ((ImageSentVH) holder).bind(msg);
        } else if (holder instanceof ImageReceivedVH) {
            ((ImageReceivedVH) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> newMessages) {
        messages = newMessages != null ? newMessages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // ==================== TEXT SENT ====================
    static class TextSentVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        TextSentVH(View view) {
            super(view);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
        }

        void bind(Message msg) {
            // Use getContent() which handles both content and text fields
            String messageText = msg.getContent();
            if (messageText == null || messageText.isEmpty()) {
                messageText = msg.getText();
            }
            tvMessage.setText(messageText);
            tvTime.setText(msg.getFormattedTime());
        }
    }

    // ==================== TEXT RECEIVED ====================
    static class TextReceivedVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvSenderName, tvMessage, tvTime;

        TextReceivedVH(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvSenderName = view.findViewById(R.id.tvSenderName);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
        }

        void bind(Message msg) {
            // Use getContent() which handles both content and text fields
            String messageText = msg.getContent();
            if (messageText == null || messageText.isEmpty()) {
                messageText = msg.getText();
            }
            tvMessage.setText(messageText);
            tvTime.setText(msg.getFormattedTime());

            // Show sender name in groups/channels
            if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                tvSenderName.setText(msg.getSenderName());
                tvSenderName.setVisibility(View.VISIBLE);
            } else {
                tvSenderName.setVisibility(View.GONE);
            }

            // Load sender avatar
            if (msg.getSenderAvatarUrl() != null && !msg.getSenderAvatarUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(msg.getSenderAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    // ==================== IMAGE SENT ====================
    static class ImageSentVH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTime;

        ImageSentVH(View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivImage);
            tvTime = view.findViewById(R.id.tvTime);
        }

        void bind(Message msg) {
            tvTime.setText(msg.getFormattedTime());

            if (msg.getFileUrl() != null && !msg.getFileUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(msg.getFileUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivImage);
            }
        }
    }

    // ==================== IMAGE RECEIVED ====================
    static class ImageReceivedVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivImage;
        TextView tvSenderName, tvTime;

        ImageReceivedVH(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            ivImage = view.findViewById(R.id.ivImage);
            tvSenderName = view.findViewById(R.id.tvSenderName);
            tvTime = view.findViewById(R.id.tvTime);
        }

        void bind(Message msg) {
            tvTime.setText(msg.getFormattedTime());

            // Show sender name
            if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                tvSenderName.setText(msg.getSenderName());
                tvSenderName.setVisibility(View.VISIBLE);
            } else {
                tvSenderName.setVisibility(View.GONE);
            }

            // Load sender avatar
            if (msg.getSenderAvatarUrl() != null && !msg.getSenderAvatarUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(msg.getSenderAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_profile);
            }

            // Load image
            if (msg.getFileUrl() != null && !msg.getFileUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(msg.getFileUrl())
                        .placeholder(R.drawable.ic_profile)
                        .into(ivImage);
            }
        }
    }
}