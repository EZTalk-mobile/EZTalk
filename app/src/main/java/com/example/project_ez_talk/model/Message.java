package com.example.project_ez_talk.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {

    public enum MessageType {
        TEXT("text"),
        IMAGE("image"),
        VIDEO("video"),
        AUDIO("audio"),
        FILE("file"),
        LOCATION("location");

        private final String value;

        MessageType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        // Convert string from Firestore to enum
        public static MessageType fromString(String value) {
            if (value == null || value.isEmpty()) {
                return TEXT; // Default
            }
            for (MessageType type : MessageType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return TEXT; // Fallback
        }
    }

    private String messageId;
    private String senderId;
    private String receiverId;
    private String senderName;         // Added for received messages
    private String senderAvatarUrl;    // Added for received messages
    private String content;
    private String text;               // For compatibility with Firestore
    private String type;               // Store as String for Firestore
    private long timestamp;
    private String fileUrl;
    private String thumbnailUrl;
    private String status;             // sent, delivered, read
    private String messageType;        // Alternative field name from Firestore

    public Message() {}

    public Message(String senderId, String receiverId, String content, MessageType messageType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.text = content;           // Also set text field
        this.type = messageType.getValue();  // Store as string
        this.timestamp = System.currentTimeMillis();
        this.status = "sent";
    }

    // Getters & Setters
    public String getMessageId() {
        return messageId;
    }
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }
    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    public String getContent() {
        return content != null ? content : text;
    }
    public void setContent(String content) {
        this.content = content;
        this.text = content;  // Keep both in sync
    }

    public String getText() {
        return text != null ? text : content;
    }
    public void setText(String text) {
        this.text = text;
        this.content = text;  // Keep both in sync
    }

    // Type handling - store as String, convert to Enum when needed
    public String getType() {
        if (type != null) return type;
        if (messageType != null) return messageType;
        return MessageType.TEXT.getValue();
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getMessageType() {
        return getType();
    }
    public void setMessageType(String messageType) {
        this.messageType = messageType;
        this.type = messageType;
    }

    // Get as Enum
    public MessageType getTypeEnum() {
        String typeStr = getType();
        return MessageType.fromString(typeStr);
    }

    public void setTypeEnum(MessageType messageType) {
        this.type = messageType.getValue();
        this.messageType = messageType.getValue();
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getStatus() {
        return status != null ? status : "sent";
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public boolean isSentByMe(String currentUserId) {
        return senderId != null && senderId.equals(currentUserId);
    }

    // Helper methods for message type checking
    public boolean isTextMessage() {
        return getTypeEnum() == MessageType.TEXT;
    }

    public boolean isImageMessage() {
        return getTypeEnum() == MessageType.IMAGE;
    }

    public boolean isVideoMessage() {
        return getTypeEnum() == MessageType.VIDEO;
    }

    public boolean isAudioMessage() {
        return getTypeEnum() == MessageType.AUDIO;
    }

    public boolean isFileMessage() {
        return getTypeEnum() == MessageType.FILE;
    }

    public boolean isLocationMessage() {
        return getTypeEnum() == MessageType.LOCATION;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}