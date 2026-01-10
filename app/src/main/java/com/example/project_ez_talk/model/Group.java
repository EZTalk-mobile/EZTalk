package com.example.project_ez_talk.model;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String id;
    private String name;
    private String icon;
    private String description;
    private String creatorId;
    private long createdAt;
    private Map<String, Boolean> members; // userId -> isMember
    private Map<String, String> memberRoles; // userId -> role (admin, member)
    private String lastMessage;
    private long lastMessageTime;

    // Required empty constructor for Firebase
    public Group() {
        members = new HashMap<>();
        memberRoles = new HashMap<>();
    }

    public Group(String id, String name, String creatorId) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.createdAt = System.currentTimeMillis();
        this.members = new HashMap<>();
        this.memberRoles = new HashMap<>();

        // Add creator as admin
        members.put(creatorId, true);
        memberRoles.put(creatorId, "admin");
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getGroupIcon() {
        return icon;
    }

    public void setGroupIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }

    public Map<String, String> getMemberRoles() {
        return memberRoles;
    }

    public void setMemberRoles(Map<String, String> memberRoles) {
        this.memberRoles = memberRoles;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getTime() {
        return lastMessageTime;
    }
}