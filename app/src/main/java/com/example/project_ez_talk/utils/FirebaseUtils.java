package com.example.project_ez_talk.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseUtils {

    public static String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : "";
    }

    public static String getCurrentUserDisplayName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            return currentUser.getDisplayName();
        }
        return "Anonymous";
    }

    public static String getChatId(String currentUserId, String chatUserId) {
        // Create a consistent chat ID by sorting the two user IDs
        if (currentUserId.compareTo(chatUserId) < 0) {
            return currentUserId + "_" + chatUserId;
        } else {
            return chatUserId + "_" + currentUserId;
        }
    }

}