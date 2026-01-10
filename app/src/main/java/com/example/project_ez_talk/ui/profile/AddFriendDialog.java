package com.example.project_ez_talk.ui.profile;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.project_ez_talk.R;  // ✅ This is the correct R
import com.example.project_ez_talk.model.FriendRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
public class AddFriendDialog extends DialogFragment {
    private static final String TAG = "AddFriendDialog";

    private EditText etUsername;
    private Button btnAdd;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isProcessing = false;
    private Toast currentToast;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Dialog created");

        try {
            View view = inflater.inflate(R.layout.dialog_add_friend,container, false);

            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            etUsername = view.findViewById(R.id.etUsername);
            btnAdd = view.findViewById(R.id.btnAdd);
            Button btnCancel = view.findViewById(R.id.btnCancel);

            if (etUsername == null || btnAdd == null || btnCancel == null) {
                Log.e(TAG, "onCreateView: View references are NULL!");
                showToast("Layout error: views not found");
                return view;
            }

            Log.d(TAG, "onCreateView: All views found successfully");

            btnCancel.setOnClickListener(v -> {
                Log.d(TAG, "Cancel clicked");
                dismiss();
            });

            btnAdd.setOnClickListener(v -> {
                Log.d(TAG, "Add Friend clicked, isProcessing: " + isProcessing);
                if (!isProcessing) {
                    handleAddFriend();
                } else {
                    showToast("Please wait...");
                }
            });

            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: Error", e);
            showToast("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Make dialog full screen
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    @SuppressLint("SetTextI18n")
    private void handleAddFriend() {
        Log.d(TAG, "handleAddFriend: Started");

        String searchQuery = etUsername.getText().toString().trim();
        Log.d(TAG, "handleAddFriend: searchQuery = " + searchQuery);

        if (searchQuery.isEmpty()) {
            showToast("Please enter username or email");
            return;
        }

        if (auth.getCurrentUser() == null) {
            showToast("You are not logged in");
            Log.e(TAG, "handleAddFriend: User not authenticated");
            return;
        }

        isProcessing = true;
        btnAdd.setEnabled(false);
        btnAdd.setText("Sending...");
        Log.d(TAG, "handleAddFriend: Button disabled, searching for user");

        String currentUserId = auth.getCurrentUser().getUid();
        Log.d(TAG, "handleAddFriend: currentUserId = " + currentUserId);

        // If it looks like an email, search by email first
        if (searchQuery.contains("@")) {
            Log.d(TAG, "handleAddFriend: Input looks like email, searching by email");
            searchByEmail(currentUserId, searchQuery.toLowerCase());
        } else {
            // Search by name (case-insensitive, partial match)
            Log.d(TAG, "handleAddFriend: Searching by name");
            searchByName(currentUserId, searchQuery.toLowerCase());
        }
    }

    private void searchByName(String currentUserId, String nameQuery) {
        Log.d(TAG, "searchByName: Searching for " + nameQuery);

        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "searchByName: Retrieved " + querySnapshot.size() + " users");

                    // Manual client-side filtering for case-insensitive search
                    for (var doc : querySnapshot.getDocuments()) {
                        String docName = doc.getString("name");
                        if (docName != null && docName.toLowerCase().contains(nameQuery)) {
                                String recipientId = doc.getId();
                            String recipientName = docName;
                            String recipientEmail = doc.getString("email");

                            Log.d(TAG, "searchByName: Found user: " + recipientName + " with email: " + recipientEmail);
                            sendFriendRequest(currentUserId, recipientId, recipientName, recipientEmail);
                            return;
                        }
                    }

                    Log.d(TAG, "searchByName: No user found with name containing " + nameQuery);
                    resetButton();
                    showToast("User not found");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "searchByName: Failed", e);
                    resetButton();
                    showToast("Error searching users: " + e.getMessage());
                });
    }

    private void searchByEmail(String currentUserId, String email) {
        Log.d(TAG, "searchByEmail: Searching for " + email);

        // Convert to lowercase for case-insensitive search
        String emailLower = email.toLowerCase().trim();

        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "searchByEmail: Retrieved " + querySnapshot.size() + " users total");

                    boolean found = false;
                    int count = 0;

                    // Manual client-side search for case-insensitive matching
                    for (var doc : querySnapshot.getDocuments()) {
                        count++;
                        String docId = doc.getId();
                        String docEmail = doc.getString("email");
                        String docName = doc.getString("name");

                        Log.d(TAG, "searchByEmail: User #" + count + " - ID: " + docId + ", Name: " + docName + ", Email: " + docEmail);

                        if (docEmail != null) {
                            String docEmailLower = docEmail.toLowerCase().trim();
                            Log.d(TAG, "searchByEmail: Comparing '" + emailLower + "' (length: " + emailLower.length() + ") with '" + docEmailLower + "' (length: " + docEmailLower.length() + ")");

                            if (docEmailLower.equals(emailLower)) {
                                Log.d(TAG, "searchByEmail: ✓ MATCH FOUND! User: " + docName);
                                sendFriendRequest(currentUserId, docId, docName, docEmail);
                                found = true;
                                break;
                            } else {
                                Log.d(TAG, "searchByEmail: ✗ No match");
                            }
                        } else {
                            Log.d(TAG, "searchByEmail: Email field is NULL for user: " + docName);
                        }
                    }

                    if (!found) {
                        Log.d(TAG, "searchByEmail: No match found after checking " + count + " users for email: " + emailLower);
                        resetButton();
                        showToast("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "searchByEmail: Failed", e);
                    resetButton();
                    showToast("Error searching by email: " + e.getMessage());
                });
    }

    private void sendFriendRequest(String senderId, String recipientId, String recipientName, String recipientEmail) {
        Log.d(TAG, "sendFriendRequest: Started");

        if (senderId.equals(recipientId)) {
            Log.d(TAG, "sendFriendRequest: Cannot add yourself");
            resetButton();
            showToast("You cannot add yourself");
            return;
        }

        db.collection("users")
                .document(senderId)
                .get()
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "sendFriendRequest: Got sender document");

                    if (doc.exists()) {
                        String senderName = doc.getString("name");
                        String senderProfilePicture = doc.getString("profilePicture");
                        checkExistingFriendship(senderId, recipientId, senderName, senderProfilePicture, recipientName, recipientEmail);
                    } else {
                        Log.e(TAG, "sendFriendRequest: Sender document not found");
                        resetButton();
                        showToast("Your profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "sendFriendRequest: Failed to get sender", e);
                    resetButton();
                    showToast("Error: " + e.getMessage());
                });
    }

    private void checkExistingFriendship(String senderId, String recipientId, String senderName,
                                         String senderProfilePicture, String recipientName, String recipientEmail) {
        Log.d(TAG, "checkExistingFriendship: Checking if already friends");

        db.collection("friendships")
                .whereEqualTo("user1", senderId)
                .whereEqualTo("user2", recipientId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "checkExistingFriendship: Already friends");
                        resetButton();
                        showToast("Already friends");
                        return;
                    }
                    checkExistingRequest(senderId, recipientId, senderName, senderProfilePicture, recipientName, recipientEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "checkExistingFriendship: Failed", e);
                    resetButton();
                    showToast("Error checking friendship: " + e.getMessage());
                });
    }

    private void checkExistingRequest(String senderId, String recipientId, String senderName,
                                      String senderProfilePicture, String recipientName, String recipientEmail) {
        Log.d(TAG, "checkExistingRequest: Checking if request already sent");

        db.collection("friendRequests")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", recipientId)
                .whereEqualTo("status", "pending")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "checkExistingRequest: Request already sent");
                        resetButton();
                        showToast("Request already sent");
                        return;
                    }
                    createFriendRequest(senderId, recipientId, senderName, senderProfilePicture, recipientName, recipientEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "checkExistingRequest: Failed", e);
                    resetButton();
                    showToast("Error checking request: " + e.getMessage());
                });
    }

    private void createFriendRequest(String senderId, String recipientId, String senderName,
                                     String senderProfilePicture, String recipientName, String recipientEmail) {
        Log.d(TAG, "createFriendRequest: Creating request from " + senderName + " to " + recipientName);

        FriendRequest request = new FriendRequest(
                senderId,
                senderName,
                senderProfilePicture,
                recipientId,
                recipientName,
                recipientEmail
        );

        db.collection("friendRequests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "createFriendRequest: Success, request ID: " + documentReference.getId());
                    resetButton();
                    showToast("Friend request sent to " + recipientName);
                    etUsername.setText("");
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createFriendRequest: Failed", e);
                    resetButton();
                    showToast("Error sending request: " + e.getMessage());
                });
    }

    private void resetButton() {
        isProcessing = false;
        if (btnAdd != null) {
            btnAdd.setEnabled(true);
            btnAdd.setText(R.string.add_friend);
        }
    }
}