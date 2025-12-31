package com.example.project_ez_talk.model;

/**
 * CallLog - Model class for storing call history
 * Used in CallsFragment to display list of calls
 */
public class CallLog {

    private String callId;              // Unique call identifier
    private String callerId;            // ID of call initiator
    private String receiverId;          // ID of call receiver
    private String callerName;          // Display name of caller
    private String receiverName;        // Display name of receiver
    private String callerAvatar;        // Avatar URL of caller
    private String receiverAvatar;      // Avatar URL of receiver
    private String status;              // Call status: "answered", "missed", "rejected", "completed"
    private String callType;            // Type of call: "voice" or "video"
    private long startTime;             // When the call started (timestamp)
    private long duration;              // Call duration in seconds
    private long timestamp;             // When this log was created

    // ==================== CONSTRUCTORS ====================

    public CallLog() {
        // Empty constructor for Firebase deserialization
    }

    public CallLog(String callerId, String receiverId, String callType) {
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callType = callType;
        this.status = "initiated";
        this.startTime = System.currentTimeMillis();
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== GETTERS ====================

    public String getCallId() {
        return callId;
    }

    public String getCallerId() {
        return callerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getCallerName() {
        return callerName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getCallerAvatar() {
        return callerAvatar;
    }

    public String getReceiverAvatar() {
        return receiverAvatar;
    }

    public String getStatus() {
        return status;
    }

    public String getCallType() {
        return callType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ==================== SETTERS ====================

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setCallerAvatar(String callerAvatar) {
        this.callerAvatar = callerAvatar;
    }

    public void setReceiverAvatar(String receiverAvatar) {
        this.receiverAvatar = receiverAvatar;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ==================== UTILITY ====================

    @Override
    public String toString() {
        return "CallLog{" +
                "callId='" + callId + '\'' +
                ", callerId='" + callerId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", callType='" + callType + '\'' +
                ", status='" + status + '\'' +
                ", duration=" + duration +
                '}';
    }

    /**
     * Check if this call was missed (incoming call that was not answered)
     */
    public boolean isMissedCall(String currentUserId) {
        return "missed".equals(status) && !callerId.equals(currentUserId);
    }

    /**
     * Check if call was answered/completed
     */
    public boolean wasAnswered() {
        return "answered".equals(status) || "completed".equals(status);
    }
}