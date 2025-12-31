package com.example.project_ez_talk.model;

import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Channel {
    private String id;
    private String name;
    private String description;
    private String avatarUrl;
    private String ownerId;
    private long createdAt;
    private boolean isPublic;
    private int subscriberCount;
    private String lastMessage;
    private long lastMessageTime;
    private Map<String, Boolean> admins;
    private Map<String, Boolean> subscribers;

    // Required empty constructor for Firestore
    public Channel() {
        this.admins = new HashMap<>();
        this.subscribers = new HashMap<>();
    }

    public Channel(String id, String name, String description, String avatarUrl, String ownerId, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.avatarUrl = avatarUrl;
        this.ownerId = ownerId;
        this.isPublic = isPublic;
        this.createdAt = System.currentTimeMillis();
        this.subscriberCount = 0;
        this.lastMessage = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.admins = new HashMap<>();
        this.subscribers = new HashMap<>();
    }

    // ==================== GETTERS ====================

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public Map<String, Boolean> getAdmins() {
        return admins != null ? admins : new HashMap<>();
    }

    public Map<String, Boolean> getSubscribers() {
        return subscribers != null ? subscribers : new HashMap<>();
    }

    // ==================== SETTERS ====================

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setAdmins(Map<String, Boolean> admins) {
        this.admins = admins;
    }

    public void setSubscribers(Map<String, Boolean> subscribers) {
        this.subscribers = subscribers;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if a user is an admin
     */
    @Exclude
    public boolean isAdmin(String userId) {
        return admins != null && admins.containsKey(userId) && Boolean.TRUE.equals(admins.get(userId));
    }

    /**
     * Check if a user is subscribed
     */
    @Exclude
    public boolean isSubscribed(String userId) {
        return subscribers != null && subscribers.containsKey(userId) && Boolean.TRUE.equals(subscribers.get(userId));
    }

    /**
     * Add an admin
     */
    @Exclude
    public void addAdmin(String userId) {
        if (admins == null) {
            admins = new HashMap<>();
        }
        admins.put(userId, true);
    }

    /**
     * Add a subscriber
     */
    @Exclude
    public void addSubscriber(String userId) {
        if (subscribers == null) {
            subscribers = new HashMap<>();
        }
        subscribers.put(userId, true);
        subscriberCount = subscribers.size();
    }

    /**
     * Remove a subscriber
     */
    @Exclude
    public void removeSubscriber(String userId) {
        if (subscribers != null) {
            subscribers.remove(userId);
            subscriberCount = subscribers.size();
        }
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isPublic=" + isPublic +
                ", subscriberCount=" + subscriberCount +
                '}';
    }
}