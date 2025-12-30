package com.example.project_ez_talk.model;

import com.google.gson.annotations.SerializedName;

/**
 * ✅ CallData Model for Firebase Signaling
 * Used by FirebaseSignaling class (alternative implementation)
 * Alternative to DataModel - choose one or the other
 */
public class CallData {

    /**
     * Call type enumeration
     */
    public enum Type {
        OFFER,           // SDP offer
        ANSWER,          // SDP answer
        ICE_CANDIDATE,   // ICE candidate
        ACCEPT,          // Accept call
        REJECT,          // Reject call
        END,             // End call
        START_CALL       // Start call request
    }

    // Fields
    @SerializedName("targetId")
    private String targetId;

    @SerializedName("senderId")
    private String senderId;

    @SerializedName("type")
    private Type type;

    @SerializedName("callType")
    private String callType;  // "voice" or "video"

    @SerializedName("data")
    private String data;      // SDP or ICE candidate JSON

    @SerializedName("timestamp")
    private long timestamp;

    // ========== CONSTRUCTORS ==========

    /**
     * Empty constructor for Gson
     */
    public CallData() {
        this.timestamp = System.currentTimeMillis();
        this.callType = "voice";
    }

    /**
     * Constructor with all fields
     */
    public CallData(String targetId, String senderId, Type type, String data) {
        this.targetId = targetId;
        this.senderId = senderId;
        this.type = type;
        this.data = data;
        this.callType = "voice";
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor with call type
     */
    public CallData(String targetId, String senderId, Type type,
                    String data, String callType) {
        this.targetId = targetId;
        this.senderId = senderId;
        this.type = type;
        this.data = data;
        this.callType = callType;
        this.timestamp = System.currentTimeMillis();
    }

    // ========== GETTERS ==========

    public String getTargetId() {
        return targetId;
    }

    public String getSenderId() {
        return senderId;
    }

    public Type getType() {
        return type;
    }

    public String getCallType() {
        return callType;
    }

    public String getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ========== SETTERS ==========

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ========== VALIDATION ==========

    /**
     * ✅ Validate CallData
     * Checks if required fields are present and valid
     */
    public boolean isValid() {
        return targetId != null && !targetId.isEmpty() &&
                senderId != null && !senderId.isEmpty() &&
                type != null &&
                !targetId.equals(senderId); // Can't call yourself
    }

    /**
     * Check if this is a signaling message (SDP/ICE)
     */
    public boolean isSignalingMessage() {
        return type == Type.OFFER ||
                type == Type.ANSWER ||
                type == Type.ICE_CANDIDATE;
    }

    /**
     * Check if this is a call control message
     */
    public boolean isCallControl() {
        return type == Type.ACCEPT ||
                type == Type.REJECT ||
                type == Type.END ||
                type == Type.START_CALL;
    }

    // ========== UTILITY METHODS ==========

    /**
     * Get type as string
     */
    public String getTypeAsString() {
        return type != null ? type.name() : "UNKNOWN";
    }

    /**
     * Check if call type is voice
     */
    public boolean isVoiceCall() {
        return "voice".equalsIgnoreCase(callType);
    }

    /**
     * Check if call type is video
     */
    public boolean isVideoCall() {
        return "video".equalsIgnoreCase(callType);
    }

    /**
     * Get data length (for logging)
     */
    public int getDataLength() {
        return data != null ? data.length() : 0;
    }

    // ========== DEBUGGING ==========

    @Override
    public String toString() {
        return "CallData{" +
                "targetId='" + targetId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", type=" + type +
                ", callType='" + callType + '\'' +
                ", dataLength=" + getDataLength() +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Get detailed string for logging
     */
    public String toDetailedString() {
        return "CallData{" +
                "targetId='" + targetId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", type=" + type +
                ", callType='" + callType + '\'' +
                ", data='" + (data != null ? data.substring(0, Math.min(100, data.length())) : "null") +
                (data != null && data.length() > 100 ? "..." : "") + '\'' +
                ", timestamp=" + timestamp +
                ", valid=" + isValid() +
                '}';
    }
}