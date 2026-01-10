package com.example.project_ez_talk.model;

import android.annotation.SuppressLint;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

<<<<<<< HEAD
/**
 * ✅ COMPLETE Message model with VIDEO + AUDIO support
 */
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
public class Message {

    public enum MessageType {
        TEXT, IMAGE, FILE, AUDIO, VIDEO, LOCATION, CONTACT
    }

    private String messageId;
    private String senderId;
    private String groupId;
    private String content;
<<<<<<< HEAD
    private String text;
    private String fileUrl;
    private String messageType = "TEXT";
    private long timestamp;
    private long duration; // Duration in milliseconds for audio/video
=======
    private String text; // Alternative field for content
    private String fileUrl;
    private String messageType = "TEXT";
    private long timestamp;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    private String senderName;
    private String senderAvatarUrl;
    private boolean isDeleted = false;

    // Required empty constructor for Firestore
    public Message() {
    }

    public Message(String senderId, String groupId, String content, MessageType type) {
        this.senderId = senderId;
        this.groupId = groupId;
        this.content = content;
        this.text = content;
        this.messageType = type.name();
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== GETTERS ====================

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getContent() {
        return content != null ? content : text;
    }

    public String getText() {
        return text != null ? text : content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getMessageType() {
        return messageType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderName() {
        return senderName != null ? senderName : "Unknown";
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    // ==================== SETTERS ====================

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setContent(String content) {
        this.content = content;
        this.text = content;
    }

    public void setText(String text) {
        this.text = text;
        this.content = text;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

<<<<<<< HEAD
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

<<<<<<< HEAD
    // ==================== SUPPRESS FIRESTORE WARNINGS ====================

    public void setTextMessage(String textMessage) {}
    public void setVideoMessage(String videoMessage) {}
    public void setAudioMessage(String audioMessage) {}
    public void setImageMessage(String imageMessage) {}
    public void setLocationMessage(String locationMessage) {}
    public void setFileMessage(String fileMessage) {}
    public void setType(String type) {}
    public void setReceiverId(String receiverId) {}
    public void setThumbnailUrl(String thumbnailUrl) {}
    public void setStatus(String status) {}
=======
    // ==================== ✅ ADD THESE TO SUPPRESS WARNINGS ====================

    // Firestore might have these fields - add dummy setters to suppress warnings
    public void setTextMessage(String textMessage) {
        // Ignore - this is just to suppress Firestore warnings
    }

    public void setVideoMessage(String videoMessage) {
        // Ignore - this is just to suppress Firestore warnings
    }

    public void setAudioMessage(String audioMessage) {
        // Ignore - this is just to suppress Firestore warnings
    }

    public void setImageMessage(String imageMessage) {
        // Ignore - this is just to suppress Firestore warnings
    }

    public void setLocationMessage(String locationMessage) {
        // Ignore - this is just to suppress Firestore warnings
    }

    public void setFileMessage(String fileMessage) {
        // Ignore - this is just to suppress Firestore warnings
    }

    public void setType(String type) {
        // Ignore - we use messageType instead
    }

    public void setReceiverId(String receiverId) {
        // Ignore - we use groupId instead
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        // Ignore - optional field
    }

    public void setStatus(String status) {
        // Ignore - optional field
    }
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    // ==================== UTILITY METHODS ====================

    /**
     * Check if message was sent by the current user
     */
    public boolean isSentByMe(String currentUserId) {
        return currentUserId != null && currentUserId.equals(this.senderId);
    }

    /**
     * Get formatted time string
     */
    @SuppressLint("SimpleDateFormat")
    @Exclude
    public String getFormattedTime() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = new Date(timestamp);
            return dateFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get formatted date string
     */
    @SuppressLint("SimpleDateFormat")
    @Exclude
    public String getFormattedDate() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = new Date(timestamp);
            return dateFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get the message type as enum
     */
    @Exclude
    public MessageType getTypeEnum() {
        try {
            if (messageType == null) {
                return MessageType.TEXT;
            }
            return MessageType.valueOf(messageType);
        } catch (Exception e) {
            return MessageType.TEXT;
        }
    }

    /**
     * Get message type display name
     */
    @Exclude
    public String getTypeDisplayName() {
        switch (getTypeEnum()) {
            case IMAGE:
                return "Image";
            case AUDIO:
                return "Audio";
<<<<<<< HEAD
            case VIDEO:
                return "Video";
=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
            case FILE:
                return "File";
            case LOCATION:
                return "Location";
<<<<<<< HEAD
=======
            case VIDEO:
                return "Video";
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
            case TEXT:
            default:
                return "Text";
        }
    }

<<<<<<< HEAD
    /**
     * ✅ Check if message is a media type
     */
    @Exclude
    public boolean isMediaMessage() {
        MessageType type = getTypeEnum();
        return type == MessageType.IMAGE || type == MessageType.VIDEO ||
                type == MessageType.AUDIO || type == MessageType.FILE;
    }

    /**
     * ✅ Get appropriate emoji for message type
     */
    @Exclude
    public String getMessageEmoji() {
        switch (getTypeEnum()) {
            case IMAGE:
                return "🖼️";
            case AUDIO:
                return "🎵";
            case VIDEO:
                return "🎬";
            case FILE:
                return "📄";
            case LOCATION:
                return "📍";
            case TEXT:
            default:
                return "💬";
        }
    }

=======
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", content='" + content + '\'' +
                ", messageType='" + messageType + '\'' +
                ", timestamp=" + timestamp +
                ", senderName='" + senderName + '\'' +
                '}';
    }
}