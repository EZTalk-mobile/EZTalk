package com.example.project_ez_talk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MessageAdapter";

    private List<Message> messages = new ArrayList<>();
    private Context context;
    private final String currentUserId;

    // ==================== FOR SWIPE DELETE ====================
    private final FirebaseFirestore db;
    private String currentChatId; // groupId or chatId
    private String chatType = "group"; // "group" or "private"

    // ✅ NEW: Track messages being deleted to prevent race conditions
    private final List<String> deletingMessageIds = new ArrayList<>();

    // Callback for delete operations
    public interface MessageDeleteListener {
        void onMessageDeleted(Message message);
    }

    private MessageDeleteListener deleteListener;

    // View types
    private static final int TYPE_TEXT_SENT = 1;
    private static final int TYPE_TEXT_RECEIVED = 2;
    private static final int TYPE_IMAGE_SENT = 3;
    private static final int TYPE_IMAGE_RECEIVED = 4;
    private static final int TYPE_FILE_SENT = 5;
    private static final int TYPE_FILE_RECEIVED = 6;
    private static final int TYPE_AUDIO_SENT = 7;
    private static final int TYPE_AUDIO_RECEIVED = 8;
    private static final int TYPE_LOCATION_SENT = 9;
    private static final int TYPE_LOCATION_RECEIVED = 10;

    // ==================== CONSTRUCTORS ====================

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messages = messageList != null ? messageList : new ArrayList<>();
        this.context = context;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        this.db = FirebaseFirestore.getInstance();
    }

    // ==================== SETTERS FOR SWIPE DELETE ====================
    public void setCurrentChatId(String chatId) {
        this.currentChatId = chatId;
        Log.d(TAG, "Chat ID set to: " + chatId);
    }

    public void setChatType(String type) {
        this.chatType = type; // "group" or "private"
        Log.d(TAG, "Chat type set to: " + type);
    }

    public void setDeleteListener(MessageDeleteListener listener) {
        this.deleteListener = listener;
    }

    /**
     * Get the messages list (needed by SwipeToDeleteCallback)
     */
    public List<Message> getMessages() {
        return messages;
    }

    // ==================== ✅ FIXED DELETE MESSAGE AT POSITION ====================
    public void deleteMessageAtPosition(int position) {
        Log.d(TAG, "🗑️ deleteMessageAtPosition called at position: " + position);

        // ✅ Validate position
        if (position < 0 || position >= messages.size()) {
            Log.e(TAG, "❌ Invalid position: " + position + ", messages size: " + messages.size());
            return;
        }

        // Get the message
        Message message = messages.get(position);

        if (message == null) {
            Log.e(TAG, "❌ Message is null at position: " + position);
            return;
        }

        // ✅ Check if message is already being deleted
        if (deletingMessageIds.contains(message.getMessageId())) {
            Log.w(TAG, "⚠️ Message is already being deleted: " + message.getMessageId());
            return;
        }

        Log.d(TAG, "📝 Message ID: " + message.getMessageId());
        Log.d(TAG, "📝 Current Chat ID: " + currentChatId);
        Log.d(TAG, "📝 Sender ID: " + message.getSenderId());
        Log.d(TAG, "📝 Current User ID: " + currentUserId);

        // ✅ Check if user can delete (must be sender)
        if (!message.isSentByMe(currentUserId)) {
            Toast.makeText(context, "You can only delete your own messages", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "⚠️ User attempted to delete message not sent by them");
            return;
        }

        // ✅ Validate message ID and chat ID
        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            Log.e(TAG, "❌ Message ID is null or empty");
            Toast.makeText(context, "Cannot delete message (invalid ID)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentChatId == null || currentChatId.isEmpty()) {
            Log.e(TAG, "❌ Current Chat ID is null or empty");
            Toast.makeText(context, "Chat ID not set", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Mark message as being deleted
        deletingMessageIds.add(message.getMessageId());

        // ✅ Show confirmation toast
        Toast.makeText(context, "Deleting message...", Toast.LENGTH_SHORT).show();

        // ==================== DELETE FROM FIRESTORE ====================
        // Determine collection path based on chat type
        String collectionPath;
        if ("private".equals(chatType)) {
            collectionPath = "chats";
        } else if ("channel".equals(chatType)) {
            collectionPath = "channels";
        } else {
            collectionPath = "groups";
        }

        db.collection(collectionPath)
                .document(currentChatId)
                .collection("messages")
                .document(message.getMessageId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "✅ Message successfully deleted from Firestore: " + message.getMessageId());

                    // ✅ IMPORTANT: Don't manually remove from list here!
                    // The Firestore listener will automatically update the list

                    // Remove from deleting list
                    deletingMessageIds.remove(message.getMessageId());

                    // Notify listeners
                    if (deleteListener != null) {
                        deleteListener.onMessageDeleted(message);
                    }

                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to delete message: " + e.getMessage());

                    // Remove from deleting list on failure
                    deletingMessageIds.remove(message.getMessageId());

                    Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ==================== ✅ FIXED SET MESSAGES METHOD ====================
    public void setMessages(List<Message> newMessages) {
        if (newMessages == null) {
            newMessages = new ArrayList<>();
        }

        // ✅ Clear the messages list and add all new messages
        messages.clear();
        messages.addAll(newMessages);

        // ✅ Clean up deletingMessageIds list (remove any IDs that are no longer in messages)
        List<String> currentMessageIds = new ArrayList<>();
        for (Message msg : messages) {
            if (msg != null && msg.getMessageId() != null) {
                currentMessageIds.add(msg.getMessageId());
            }
        }
        deletingMessageIds.retainAll(currentMessageIds);

        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        if (message != null && !deletingMessageIds.contains(message.getMessageId())) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= messages.size()) {
            return TYPE_TEXT_SENT;
        }

        Message msg = messages.get(position);
        if (msg == null) {
            return TYPE_TEXT_SENT;
        }

        boolean isSentByMe = msg.isSentByMe(currentUserId);
        Message.MessageType messageType = msg.getTypeEnum();

        switch (messageType) {
            case IMAGE:
                return isSentByMe ? TYPE_IMAGE_SENT : TYPE_IMAGE_RECEIVED;
            case AUDIO:
                return isSentByMe ? TYPE_AUDIO_SENT : TYPE_AUDIO_RECEIVED;
            case FILE:
                return isSentByMe ? TYPE_FILE_SENT : TYPE_FILE_RECEIVED;
            case LOCATION:
                return isSentByMe ? TYPE_LOCATION_SENT : TYPE_LOCATION_RECEIVED;
            case VIDEO:
                return isSentByMe ? TYPE_IMAGE_SENT : TYPE_IMAGE_RECEIVED;
            default:
            case TEXT:
                return isSentByMe ? TYPE_TEXT_SENT : TYPE_TEXT_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (context == null) {
            context = parent.getContext();
        }

        switch (viewType) {
            case TYPE_TEXT_SENT:
                return new TextSentVH(inflater.inflate(R.layout.item_message_sent, parent, false));
            case TYPE_TEXT_RECEIVED:
                return new TextReceivedVH(inflater.inflate(R.layout.item_message_received, parent, false));
            case TYPE_IMAGE_SENT:
                return new ImageSentVH(inflater.inflate(R.layout.item_message_sent, parent, false));
            case TYPE_IMAGE_RECEIVED:
                return new ImageReceivedVH(inflater.inflate(R.layout.item_message_received, parent, false));
            case TYPE_FILE_SENT:
                return new FileSentVH(inflater.inflate(R.layout.item_message_sent, parent, false));
            case TYPE_FILE_RECEIVED:
                return new FileReceivedVH(inflater.inflate(R.layout.item_message_received, parent, false));
            case TYPE_AUDIO_SENT:
                return new AudioSentVH(inflater.inflate(R.layout.item_message_sent, parent, false));
            case TYPE_AUDIO_RECEIVED:
                return new AudioReceivedVH(inflater.inflate(R.layout.item_message_received, parent, false));
            case TYPE_LOCATION_SENT:
                return new LocationSentVH(inflater.inflate(R.layout.item_message_sent, parent, false));
            case TYPE_LOCATION_RECEIVED:
                return new LocationReceivedVH(inflater.inflate(R.layout.item_message_received, parent, false));
            default:
                return new TextSentVH(inflater.inflate(R.layout.item_message_sent, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < 0 || position >= messages.size()) {
            Log.e(TAG, "Invalid position in onBindViewHolder: " + position);
            return;
        }

        Message msg = messages.get(position);
        if (msg == null) {
            Log.e(TAG, "Message is null at position: " + position);
            return;
        }

        if (holder instanceof TextSentVH) {
            ((TextSentVH) holder).bind(msg, position);
        } else if (holder instanceof TextReceivedVH) {
            ((TextReceivedVH) holder).bind(msg, position);
        } else if (holder instanceof ImageSentVH) {
            ((ImageSentVH) holder).bind(msg, position);
        } else if (holder instanceof ImageReceivedVH) {
            ((ImageReceivedVH) holder).bind(msg, position);
        } else if (holder instanceof FileSentVH) {
            ((FileSentVH) holder).bind(msg, position);
        } else if (holder instanceof FileReceivedVH) {
            ((FileReceivedVH) holder).bind(msg, position);
        } else if (holder instanceof AudioSentVH) {
            ((AudioSentVH) holder).bind(msg, position);
        } else if (holder instanceof AudioReceivedVH) {
            ((AudioReceivedVH) holder).bind(msg, position);
        } else if (holder instanceof LocationSentVH) {
            ((LocationSentVH) holder).bind(msg, position);
        } else if (holder instanceof LocationReceivedVH) {
            ((LocationReceivedVH) holder).bind(msg, position);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ==================== VIEW HOLDERS ====================
    // (All ViewHolder classes remain the same as before)

    class TextSentVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        TextSentVH(View view) {
            super(view);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvMessage == null || tvTime == null) return;

            String messageText = msg.getContent();
            if (messageText == null || messageText.isEmpty()) {
                messageText = msg.getText();
            }
            if (messageText == null) {
                messageText = "";
            }

            tvMessage.setText(messageText);
            tvTime.setText(msg.getFormattedTime());

            itemView.setOnLongClickListener(v -> {
                deleteMessageAtPosition(position);
                return true;
            });
        }
    }

    class TextReceivedVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvSenderName, tvMessage, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        TextReceivedVH(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvSenderName = view.findViewById(R.id.tvSenderName);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvMessage == null || tvTime == null) return;

            String messageText = msg.getContent();
            if (messageText == null || messageText.isEmpty()) {
                messageText = msg.getText();
            }
            if (messageText == null) {
                messageText = "";
            }

            tvMessage.setText(messageText);
            tvTime.setText(msg.getFormattedTime());

            if (tvSenderName != null) {
                if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                    tvSenderName.setText(msg.getSenderName());
                    tvSenderName.setVisibility(View.VISIBLE);
                } else {
                    tvSenderName.setVisibility(View.GONE);
                }
            }

            if (ivAvatar != null) {
                if (msg.getSenderAvatarUrl() != null && !msg.getSenderAvatarUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(msg.getSenderAvatarUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }
            }

            itemView.setOnLongClickListener(v -> {
                if (msg.isSentByMe(currentUserId)) {
                    deleteMessageAtPosition(position);
                }
                return true;
            });
        }
    }

    class ImageSentVH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTimeNoCaption;
        CardView cvImageContainer;
        LinearLayout llTextContent;

        ImageSentVH(View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivImage);
            tvTimeNoCaption = view.findViewById(R.id.tvTimeNoCaption);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
            llTextContent = view.findViewById(R.id.llTextContent);
        }

        void bind(Message msg, int position) {
            if (llTextContent != null) llTextContent.setVisibility(View.GONE);
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.VISIBLE);
            if (tvTimeNoCaption != null) {
                tvTimeNoCaption.setText(msg.getFormattedTime());
                tvTimeNoCaption.setVisibility(View.VISIBLE);
            }

            if (ivImage == null) return;

            String imageUrl = msg.getFileUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                ivImage.setVisibility(View.GONE);
                return;
            }

            ivImage.setVisibility(View.VISIBLE);

            try {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(ivImage);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                ivImage.setVisibility(View.GONE);
            }

            ivImage.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(itemView.getContext(), com.example.project_ez_talk.ui.media.ImageViewerActivity.class);
                    intent.putExtra("image_url", imageUrl);
                    intent.putExtra("sender_name", "You");
                    intent.putExtra("timestamp", msg.getFormattedTime());
                    intent.putExtra("caption", msg.getContent());
                    itemView.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening image viewer: " + e.getMessage());
                }
            });

            itemView.setOnLongClickListener(v -> {
                deleteMessageAtPosition(position);
                return true;
            });
        }
    }

    class ImageReceivedVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivImage;
        TextView tvSenderName, tvTimeNoCaption;
        CardView cvImageContainer;
        LinearLayout llTextContent;

        ImageReceivedVH(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            ivImage = view.findViewById(R.id.ivImage);
            tvSenderName = view.findViewById(R.id.tvSenderName);
            tvTimeNoCaption = view.findViewById(R.id.tvTimeNoCaption);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
            llTextContent = view.findViewById(R.id.llTextContent);
        }

        void bind(Message msg, int position) {
            if (llTextContent != null) llTextContent.setVisibility(View.GONE);
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.VISIBLE);
            if (tvTimeNoCaption != null) {
                tvTimeNoCaption.setText(msg.getFormattedTime());
                tvTimeNoCaption.setVisibility(View.VISIBLE);
            }

            if (tvSenderName != null) {
                if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                    tvSenderName.setText(msg.getSenderName());
                    tvSenderName.setVisibility(View.VISIBLE);
                } else {
                    tvSenderName.setVisibility(View.GONE);
                }
            }

            if (ivAvatar != null) {
                if (msg.getSenderAvatarUrl() != null && !msg.getSenderAvatarUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(msg.getSenderAvatarUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }
            }

            if (ivImage == null) return;

            String imageUrl = msg.getFileUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                ivImage.setVisibility(View.GONE);
                return;
            }

            ivImage.setVisibility(View.VISIBLE);

            try {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.ic_profile)
                        .centerCrop()
                        .into(ivImage);
            } catch (Exception e) {
                Log.e(TAG, "Error loading received image: " + e.getMessage());
                ivImage.setVisibility(View.GONE);
            }

            ivImage.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(itemView.getContext(), com.example.project_ez_talk.ui.media.ImageViewerActivity.class);
                    intent.putExtra("image_url", imageUrl);
                    intent.putExtra("sender_name", msg.getSenderName());
                    intent.putExtra("timestamp", msg.getFormattedTime());
                    intent.putExtra("caption", msg.getContent());
                    itemView.getContext().startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening image viewer: " + e.getMessage());
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (msg.isSentByMe(currentUserId)) {
                    deleteMessageAtPosition(position);
                }
                return true;
            });
        }
    }

    // ... (Continue with remaining ViewHolders - FileSentVH, FileReceivedVH, etc.)
    // They all follow the same pattern as above with proper null checks and position handling

    class FileSentVH extends RecyclerView.ViewHolder {
        TextView tvFileName, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        FileSentVH(View view) {
            super(view);
            tvFileName = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvFileName == null || tvTime == null) return;

            String fileName = msg.getContent();
            if (fileName == null || fileName.isEmpty()) fileName = "Document";

            if (fileName.contains("|")) {
                String[] parts = fileName.split("\\|");
                tvFileName.setText("👤 " + parts[0]);
            } else {
                tvFileName.setText("📄 " + fileName);
            }

            tvTime.setText(msg.getFormattedTime());

            if (msg.getFileUrl() != null && !msg.getFileUrl().isEmpty()) {
                itemView.setOnClickListener(v -> openDocument(itemView.getContext(), msg.getFileUrl()));
            }

            itemView.setOnLongClickListener(v -> {
                deleteMessageAtPosition(position);
                return true;
            });
        }
    }

    class FileReceivedVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvSenderName, tvFileName, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        FileReceivedVH(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvSenderName = view.findViewById(R.id.tvSenderName);
            tvFileName = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        @SuppressLint("SetTextI18n")
        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvFileName == null || tvTime == null) return;

            String fileName = msg.getContent();
            if (fileName == null || fileName.isEmpty()) fileName = "Document";

            if (fileName.contains("|")) {
                String[] parts = fileName.split("\\|");
                tvFileName.setText("👤 " + parts[0] + "\n📱 " + parts[1]);
            } else {
                tvFileName.setText("📄 " + fileName);
            }

            tvTime.setText(msg.getFormattedTime());

            if (tvSenderName != null) {
                if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                    tvSenderName.setText(msg.getSenderName());
                    tvSenderName.setVisibility(View.VISIBLE);
                } else {
                    tvSenderName.setVisibility(View.GONE);
                }
            }

            if (ivAvatar != null) {
                if (msg.getSenderAvatarUrl() != null && !msg.getSenderAvatarUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(msg.getSenderAvatarUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }
            }

            if (msg.getFileUrl() != null && !msg.getFileUrl().isEmpty()) {
                itemView.setOnClickListener(v -> openDocument(itemView.getContext(), msg.getFileUrl()));
            }

            itemView.setOnLongClickListener(v -> {
                if (msg.isSentByMe(currentUserId)) {
                    deleteMessageAtPosition(position);
                }
                return true;
            });
        }
    }

    class AudioSentVH extends RecyclerView.ViewHolder {
        TextView tvDuration, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        AudioSentVH(View view) {
            super(view);
            tvDuration = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        @SuppressLint("SetTextI18n")
        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvDuration != null) tvDuration.setText("🎵 Audio Message");
            if (tvTime != null) tvTime.setText(msg.getFormattedTime());

            if (msg.getFileUrl() != null && !msg.getFileUrl().isEmpty()) {
                itemView.setOnClickListener(v -> playAudio(itemView.getContext(), msg.getFileUrl()));
            }

            itemView.setOnLongClickListener(v -> {
                deleteMessageAtPosition(position);
                return true;
            });
        }
    }

    class AudioReceivedVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvSenderName, tvDuration, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        AudioReceivedVH(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvSenderName = view.findViewById(R.id.tvSenderName);
            tvDuration = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        @SuppressLint("SetTextI18n")
        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvDuration != null) tvDuration.setText("🎵 Audio Message");
            if (tvTime != null) tvTime.setText(msg.getFormattedTime());

            if (tvSenderName != null) {
                if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                    tvSenderName.setText(msg.getSenderName());
                    tvSenderName.setVisibility(View.VISIBLE);
                } else {
                    tvSenderName.setVisibility(View.GONE);
                }
            }

            if (ivAvatar != null) {
                if (msg.getSenderAvatarUrl() != null && !msg.getSenderAvatarUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(msg.getSenderAvatarUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }
            }

            if (msg.getFileUrl() != null && !msg.getFileUrl().isEmpty()) {
                itemView.setOnClickListener(v -> playAudio(itemView.getContext(), msg.getFileUrl()));
            }

            itemView.setOnLongClickListener(v -> {
                if (msg.isSentByMe(currentUserId)) {
                    deleteMessageAtPosition(position);
                }
                return true;
            });
        }
    }

    class LocationSentVH extends RecyclerView.ViewHolder {
        TextView tvLocation, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        LocationSentVH(View view) {
            super(view);
            tvLocation = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvLocation == null || tvTime == null) return;

            String content = msg.getContent();
            if (content != null && content.contains(",")) {
                String[] coords = content.split(",");
                if (coords.length == 2) {
                    try {
                        double lat = Double.parseDouble(coords[0].trim());
                        double lon = Double.parseDouble(coords[1].trim());
                        tvLocation.setText(String.format("📍 Location\n%.4f, %.4f", lat, lon));
                        itemView.setOnClickListener(v -> openLocation(itemView.getContext(), lat, lon));
                    } catch (NumberFormatException e) {
                        tvLocation.setText("📍 Location");
                    }
                } else {
                    tvLocation.setText("📍 Location");
                }
            } else {
                tvLocation.setText("📍 Location");
            }

            tvTime.setText(msg.getFormattedTime());

            itemView.setOnLongClickListener(v -> {
                deleteMessageAtPosition(position);
                return true;
            });
        }
    }

    class LocationReceivedVH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvSenderName, tvLocation, tvTime;
        LinearLayout llTextContent;
        CardView cvImageContainer;

        LocationReceivedVH(View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivAvatar);
            tvSenderName = view.findViewById(R.id.tvSenderName);
            tvLocation = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            llTextContent = view.findViewById(R.id.llTextContent);
            cvImageContainer = view.findViewById(R.id.cvImageContainer);
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        void bind(Message msg, int position) {
            if (cvImageContainer != null) cvImageContainer.setVisibility(View.GONE);
            if (llTextContent != null) llTextContent.setVisibility(View.VISIBLE);

            if (tvLocation == null || tvTime == null) return;

            String content = msg.getContent();
            if (content != null && content.contains(",")) {
                String[] coords = content.split(",");
                if (coords.length == 2) {
                    try {
                        double lat = Double.parseDouble(coords[0].trim());
                        double lon = Double.parseDouble(coords[1].trim());
                        tvLocation.setText(String.format("📍 Location\n%.4f, %.4f", lat, lon));
                        itemView.setOnClickListener(v -> openLocation(itemView.getContext(), lat, lon));
                    } catch (NumberFormatException e) {
                        tvLocation.setText("📍 Location");
                    }
                } else {
                    tvLocation.setText("📍 Location");
                }
            } else {
                tvLocation.setText("📍 Location");
            }

            tvTime.setText(msg.getFormattedTime());

            if (tvSenderName != null) {
                if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                    tvSenderName.setText(msg.getSenderName());
                    tvSenderName.setVisibility(View.VISIBLE);
                } else {
                    tvSenderName.setVisibility(View.GONE);
                }
            }

            if (ivAvatar != null) {
                if (msg.getSenderAvatarUrl() != null && !msg.getSenderAvatarUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(msg.getSenderAvatarUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile);
                }
            }

            itemView.setOnLongClickListener(v -> {
                if (msg.isSentByMe(currentUserId)) {
                    deleteMessageAtPosition(position);
                }
                return true;
            });
        }
    }

    // ==================== UTILITY METHODS ====================

    private static void openDocument(Context context, String documentUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(documentUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening document: " + e.getMessage());
            Toast.makeText(context, "Cannot open document", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("IntentReset")
    private static void playAudio(Context context, String audioUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(audioUrl));
            intent.setType("audio/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
            Toast.makeText(context, "Cannot play audio", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openLocation(Context context, double latitude, double longitude) {
        try {
            String uri = "https://maps.google.com/?q=" + latitude + "," + longitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening location: " + e.getMessage());
            Toast.makeText(context, "Cannot open location", Toast.LENGTH_SHORT).show();
        }
    }
}