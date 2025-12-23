
package com.example.project_ez_talk.model;

public class Channel {
    private String id;
    private String name;
    private String avatarUrl;
    private int subscriberCount;
    private long lastMessageTimestamp;
    private String description;

    // Required empty constructor for Firestore
    public Channel() {}

    // Getters (THESE WERE MISSING — THIS FIXES YOUR ERROR!)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public String getDescription() {
        return description;
    }

    // Setters (Firestore uses these)
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}