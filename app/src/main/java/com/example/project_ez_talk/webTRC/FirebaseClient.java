package com.example.project_ez_talk.webTRC;

import androidx.annotation.NonNull;


import com.example.project_ez_talk.webTRC.DataModel;
import com.example.project_ez_talk.webTRC.NewEventCallBack;
import com.example.project_ez_talk.webTRC.SuccessCallBack;
import com.example.project_ez_talk.webTRC.ErrorCallBack;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Objects;

public class FirebaseClient {

    private final Gson gson = new Gson();
    private static final String DATABASE_URL = "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";
    private final DatabaseReference dbRef;
    private String currentUsername;
    private static final String LATEST_EVENT_FIELD_NAME = "latest_event";

    public FirebaseClient() {
        // Use the correct database URL for the Europe West region
        dbRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference();
    }

    public void login(String username, SuccessCallBack callBack){
        android.util.Log.d("FirebaseClient", "=== login() ===");
        android.util.Log.d("FirebaseClient", "Username: " + username);
        android.util.Log.d("FirebaseClient", "Database URL: " + DATABASE_URL);

        // Create a user presence node with timestamp
        java.util.HashMap<String, Object> userData = new java.util.HashMap<>();
        userData.put("status", "online");
        userData.put("timestamp", System.currentTimeMillis());

        dbRef.child(username).setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                android.util.Log.d("FirebaseClient", "✅ Login successful - user node created in database: " + username);
                currentUsername = username;
                callBack.onSuccess();
            } else {
                android.util.Log.e("FirebaseClient", "❌ Login FAILED: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                // Still call success callback to not block the flow, but log the error
                currentUsername = username;
                callBack.onSuccess();
            }
        });
    }

    public void setUpVideoCall(String username, SuccessCallBack callBack) {
        android.util.Log.d("FirebaseClient", "=== setUpVideoCall() ===");
        android.util.Log.d("FirebaseClient", "Username: " + username);

        // Create a user presence node with timestamp
        java.util.HashMap<String, Object> userData = new java.util.HashMap<>();
        userData.put("status", "online");
        userData.put("timestamp", System.currentTimeMillis());

        dbRef.child(username).setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                android.util.Log.d("FirebaseClient", "✅ SetUpVideoCall successful - user node created: " + username);
                currentUsername = username;
                callBack.onSuccess();
            } else {
                android.util.Log.e("FirebaseClient", "❌ SetUpVideoCall FAILED: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                // Still call success callback to not block the flow
                currentUsername = username;
                callBack.onSuccess();
            }
        });
    }

    public void sendMessageToOtherUser(DataModel dataModel, ErrorCallBack errorCallBack){
        android.util.Log.d("FirebaseClient", "=== sendMessageToOtherUser() ===");

        // Validate dataModel and target before proceeding
        if (dataModel == null) {
            android.util.Log.e("FirebaseClient", "ERROR: dataModel is null");
            errorCallBack.onError();
            return;
        }

        android.util.Log.d("FirebaseClient", "DataModel - Target: " + dataModel.getTarget() + ", Sender: " + dataModel.getSender() + ", Type: " + dataModel.getType());

        if (dataModel.getTarget() == null || dataModel.getTarget().isEmpty()) {
            android.util.Log.e("FirebaseClient", "ERROR: Target is null or empty");
            errorCallBack.onError();
            return;
        }

        android.util.Log.d("FirebaseClient", "Checking if target user exists in database: " + dataModel.getTarget());

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d("FirebaseClient", "Database snapshot received");
                android.util.Log.d("FirebaseClient", "Checking child: " + dataModel.getTarget());

                if (snapshot.child(dataModel.getTarget()).exists()){
                    android.util.Log.d("FirebaseClient", "✅ Target user exists, sending signal...");
                    //send the signal to other user
                    String jsonData = gson.toJson(dataModel);
                    android.util.Log.d("FirebaseClient", "JSON data: " + jsonData);

                    String dbPath = dataModel.getTarget() + "/" + LATEST_EVENT_FIELD_NAME;
                    android.util.Log.d("FirebaseClient", "Writing to database path: " + dbPath);

                    dbRef.child(dataModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
                            .setValue(jsonData)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("FirebaseClient", "✅ Message sent successfully to path: " + dbPath);
                                android.util.Log.d("FirebaseClient", "Check Firebase Console at: " + DATABASE_URL + "/" + dbPath);
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("FirebaseClient", "❌ ERROR sending message to path " + dbPath + ": " + e.getMessage());
                                errorCallBack.onError();
                            });

                }else {
                    android.util.Log.e("FirebaseClient", "❌ ERROR: Target user does not exist in database: " + dataModel.getTarget());
                    errorCallBack.onError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("FirebaseClient", "❌ ERROR: Database error: " + error.getMessage());
                errorCallBack.onError();
            }
        });
    }

    public void observeIncomingLatestEvent(NewEventCallBack callBack){
        // Validate currentUsername before observing
        if (currentUsername == null || currentUsername.isEmpty()) {
            return;
        }

        dbRef.child(currentUsername).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try{
                            // Check if snapshot has a value before processing
                            if (snapshot.exists() && snapshot.getValue() != null) {
                                String data = snapshot.getValue().toString();
                                DataModel dataModel = gson.fromJson(data, DataModel.class);
                                callBack.onNewEventReceived(dataModel);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


    }
}