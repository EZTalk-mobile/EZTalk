package com.example.project_ez_talk.model;

public class SearchResult {
    private String title;          // User's name
    private String subtitle;       // User's email
    private String type;           // "user", "group", "channel"
    private String avatarUrl;
    private String time;
    private String userId;         // Firebase UID

    // ✅ NEW: For UserProfileDialog
    private String bio;            // User bio/status
    private boolean isOnline;      // Online status

    public SearchResult() {}

    public SearchResult(String title, String subtitle, String type, String avatarUrl, String time, String userId) {
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
        this.avatarUrl = avatarUrl;
        this.time = time;
        this.userId = userId;
        this.bio = "Hey there! I'm using EZ Talk";  // Default
        this.isOnline = false;  // Default
    }

    // ==================== GETTERS ====================

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getType() {
        return type;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getTime() {
        return time;
    }

    public String getUserId() {
        return userId;
    }

    // ✅ NEW GETTERS
    public String getBio() {
        return bio;
    }

    public boolean isOnline() {
        return isOnline;
    }

    // ==================== SETTERS ====================

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // ✅ NEW SETTERS
    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
