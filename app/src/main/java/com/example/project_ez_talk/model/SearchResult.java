package com.example.project_ez_talk.model;

public class SearchResult {
    private String title;          // User's name
    private String subtitle;       // User's email
    private String type;           // "contact"
    private String avatarUrl;
    private String time;
    private String userId;         // ← NEW: Firebase UID

    public SearchResult() {}

    public SearchResult(String title, String subtitle, String type, String avatarUrl, String time, String userId) {
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
        this.avatarUrl = avatarUrl;
        this.time = time;
        this.userId = userId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getType() { return type; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getTime() { return time; }
    public String getUserId() { return userId; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setType(String type) { this.type = type; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setTime(String time) { this.time = time; }
    public void setUserId(String userId) { this.userId = userId; }
}