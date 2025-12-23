package com.example.project_ez_talk.model;

public class Contact {
    private String id;
    private String name;
    private String phone;
    private String avatarUrl;
    private String status;
    private boolean online;
    private boolean pendingRequest;
    private boolean selected;

    // Additional fields to match your Firebase structure
    private String senderProfilePicture;  // For friend requests
    private String receiverEmail;
    private String senderName;
    private long timestamp;
    private int respondedAt;
    private long lastSeen;  // ← ADD THIS - it's being saved to Firestore

    public Contact() {}

    public Contact(String id, String name, String phone, boolean selected) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.selected = selected;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        // Check both field names since Firebase uses different names
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            return avatarUrl;
        }
        // Fallback to senderProfilePicture for friend requests
        return senderProfilePicture;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isPendingRequest() {
        return pendingRequest;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getSenderProfilePicture() {
        return senderProfilePicture;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getRespondedAt() {
        return respondedAt;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setPendingRequest(boolean pendingRequest) {
        this.pendingRequest = pendingRequest;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setSenderProfilePicture(String senderProfilePicture) {
        this.senderProfilePicture = senderProfilePicture;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRespondedAt(int respondedAt) {
        this.respondedAt = respondedAt;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", online=" + online +
                ", status='" + status + '\'' +
                '}';
    }
}