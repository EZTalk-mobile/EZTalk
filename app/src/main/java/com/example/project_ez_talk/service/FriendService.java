package com.example.project_ez_talk.service;

import com.google.firebase.firestore.FirebaseFirestore;

public class FriendService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // This class handles all friend-related Firebase operations
    // The logic is implemented directly in AddFriendDialog for simplicity
    // You can extend this class with additional helper methods if needed

    public FirebaseFirestore getDatabase() {
        return db;
    }

    // Helper method to check if two users are friends
    public void areFriends(String userId1, String userId2, OnFriendsCheckListener listener) {
        db.collection("friendships")
                .whereEqualTo("user1", userId1)
                .whereEqualTo("user2", userId2)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listener.onResult(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    public interface OnFriendsCheckListener {
        void onResult(boolean areFriends);
        void onError(String error);
    }
}