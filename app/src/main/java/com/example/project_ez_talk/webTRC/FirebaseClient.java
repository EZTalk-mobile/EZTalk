package com.example.project_ez_talk.webTRC;


//import com.example.project_ez_talk.webTRC.DataModel;
//import com.example.project_ez_talk.webTRC.NewEventCallBack;
import android.util.Log;

import com.example.project_ez_talk.webTRC.SuccessCallBack;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
        import com.google.gson.Gson;

public class FirebaseClient {

    private final Gson gson = new Gson();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance("https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app").getReference();
    private String currentUsername;
    private static final String LATEST_EVENT_FIELD_NAME = "latest_event";

    public void setUpVideoCall(String username, SuccessCallBack callBack){
        dbRef.child(username).setValue("nkdddddddd")
                .addOnSuccessListener(unused -> {
                    Log.d("VCALL", "WRITE SUCCESS");
                    currentUsername = username;
                    callBack.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("VCALL", "WRITE FAILED", e);
                })
                .addOnCanceledListener(() -> {
                    Log.e("VCALL", "WRITE CANCELED");
                });
    }


//    public void sendMessageToOtherUser(DataModel dataModel, ErrorCallBack errorCallBack){
//        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.child(dataModel.getTarget()).exists()){
//                    //send the signal to other user
//                    dbRef.child(dataModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
//                            .setValue(gson.toJson(dataModel));
//
//                }else {
//                    errorCallBack.onError();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                errorCallBack.onError();
//            }
//        });
//    }
//
//    public void observeIncomingLatestEvent(NewEventCallBack callBack){
//        dbRef.child(currentUsername).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        try{
//                            String data= Objects.requireNonNull(snapshot.getValue()).toString();
//                            DataModel dataModel = gson.fromJson(data,DataModel.class);
//                            callBack.onNewEventReceived(dataModel);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                }
//        );
//
//
//    }
}