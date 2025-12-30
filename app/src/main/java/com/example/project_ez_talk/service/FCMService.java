package com.example.project_ez_talk.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.call.incoming.IncomingCallActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "call_notifications";
    private static final String CHANNEL_NAME = "Call Notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            String callType = remoteMessage.getData().get("call_type");
            String callerId = remoteMessage.getData().get("caller_id");
            String callerName = remoteMessage.getData().get("caller_name");
            String callerAvatar = remoteMessage.getData().get("caller_avatar");
            
            if ("video".equals(callType) || "voice".equals(callType)) {
                handleIncomingCall(callType, callerId, callerName, callerAvatar);
            }
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody()
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        
        // Send token to your server or save it locally
        sendRegistrationToServer(token);
    }

    private void handleIncomingCall(String callType, String callerId, String callerName, String callerAvatar) {
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_ID, callerId);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, callerName);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_AVATAR, callerAvatar);
        intent.putExtra(IncomingCallActivity.EXTRA_CALL_TYPE, callType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showNotification(String title, String body) {
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for incoming calls");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Send token to your backend server
        // Store it in Firestore associated with the current user
        Log.d(TAG, "Sending token to server: " + token);
    }
}
