package com.example.project_ez_talk.model;

public class CallLog {
    private String callId;
    private String callerId;
    private String callerName;
    private String receiverId;
    private String receiverName;
    private String callType; // "voice" or "video"
    private String status; // "ringing", "answered", "rejected", "missed", "ended"
    private long startTime;
    private long endTime;
    private long duration;

    // Required empty constructor for Firebase
    public CallLog() {
    }

    public CallLog(String callId, String callerId, String receiverId, String callType) {
        this.callId = callId;
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callType = callType;
        this.status = "ringing";
        this.startTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }

    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }

    public String getCallerName() { return callerName; }
    public void setCallerName(String callerName) { this.callerName = callerName; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}