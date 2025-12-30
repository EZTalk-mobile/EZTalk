package com.example.project_ez_talk.webrtc;


/**
 * ✅ DataModel for WebRTC signaling
 * Used to send Offer, Answer, ICE candidates, and call control signals
 */
public class DataModel {

    // Fields
    private String target;
    private String sender;
    private String data;
    private DataModelType type;
    // ✅ Constructor
    public DataModel(String target, String sender, String data, DataModelType type) {
        this.target = target;
        this.sender = sender;
        this.data = data;
        this.type = type;
    }

    // ✅ Getters
    public String getTarget() {
        return target;
    }

    public String getSender() {
        return sender;
    }

    public String getData() {
        return data;
    }

    public DataModelType getType() {
        return type;
    }

    // ✅ Setters
    public void setTarget(String target) {
        this.target = target;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setType(DataModelType type) {
        this.type = type;
    }

    // ✅ Validation method
    public boolean isValid() {
        return target != null && !target.isEmpty() &&
                sender != null && !sender.isEmpty() &&
                type != null;
    }

    // ✅ toString for debugging
    @Override
    public String toString() {
        return "DataModel{" +
                "target='" + target + '\'' +
                ", sender='" + sender + '\'' +
                ", type=" + type +
                ", data='" + (data != null ? data.substring(0, Math.min(50, data.length())) : "null") + '\'' +
                '}';
    }

} // ✅ CLOSING BRACE - Make sure this exists!