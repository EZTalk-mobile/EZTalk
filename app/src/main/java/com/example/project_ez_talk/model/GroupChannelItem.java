package com.example.project_ez_talk.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GroupChannelItem {
    private String id;
    private String name;
    private String avatarUrl;
    private String lastMessage;
    private long timestamp;
    private String type; // "group" or "channel"
    private int memberCount;

    public GroupChannelItem() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public int getMemberCount() { return memberCount; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setType(String type) { this.type = type; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getFormattedTime() {
        if (timestamp == 0) return "";

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60_000) {
            return "just now";
        } else if (diff < 3_600_000) {
            int minutes = (int) (diff / 60_000);
            return minutes + "m ago";
        } else if (diff < 86_400_000) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } else if (diff < 172_800_000) {
            return "Yesterday";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    public boolean isGroup() {
        return "group".equals(type);
    }

    public boolean isChannel() {
        return "channel".equals(type);
    }
}