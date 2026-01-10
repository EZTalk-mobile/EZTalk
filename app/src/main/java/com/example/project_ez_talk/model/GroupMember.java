package com.example.project_ez_talk.model;

public class GroupMember {
    private String userId;
    private String name;
    private String avatarUrl;
    private boolean isAdmin = false;
    private boolean isOnline = false;

    // Empty constructor for Firestore
    public GroupMember() {}

    // Constructor with parameters
    public GroupMember(String userId, String name, String avatarUrl, boolean isAdmin) {
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.isAdmin = isAdmin;
        this.isOnline = false;
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
}