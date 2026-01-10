package com.example.project_ez_talk.model;

public class FriendRequest {
    private String requestId;
    private String senderId;
    private String senderName;
    private String senderProfilePicture;
    private String receiverId;
    private String receiverName;
    private String receiverEmail;
    private String status; // "pending", "accepted", "rejected"
    private long timestamp;
    private long respondedAt;

    // Empty constructor for Firebase
    public FriendRequest() {
    }

    public FriendRequest(String senderId, String senderName, String senderProfilePicture,
                         String receiverId, String receiverName, String receiverEmail) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePicture = senderProfilePicture;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.receiverEmail = receiverEmail;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfilePicture() {
        return senderProfilePicture;
    }

    public void setSenderProfilePicture(String senderProfilePicture) {
        this.senderProfilePicture = senderProfilePicture;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(long respondedAt) {
        this.respondedAt = respondedAt;
    }
}