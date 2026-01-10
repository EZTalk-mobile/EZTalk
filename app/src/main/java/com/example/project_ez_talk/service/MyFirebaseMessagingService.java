package com.example.project_ez_talk.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.MainActivity;
import com.example.project_ez_talk.ui.chat.detail.ChatDetailActivity;
import com.example.project_ez_talk.ui.chat.group.GroupChatActivity;
import com.example.project_ez_talk.ui.channel.ChannelDetailActivity;
import com.example.project_ez_talk.helper.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "default_channel";

    /**
     * Called when a message is received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "🔔 Message received from: " + remoteMessage.getFrom());

        // Handle notification
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "📢 Title: " + title);
            Log.d(TAG, "📝 Body: " + body);

            // Send notification to device
            sendNotification(title, body, remoteMessage.getData());
        }
    }

    /**
     * Called when new FCM token is generated
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔑 New FCM Token: " + token);

        // Save new token to Firestore
        NotificationHelper.saveFCMTokenToFirestore(this, token);
    }

    /**
     * Build and display notification
     */
    private void sendNotification(String title, String body, Map<String, String> data) {
        // Create notification channel (Android 8+)
        createNotificationChannel();

        // Extract data from notification
        String type = data.get("type");
        String chatId = data.get("chatId");
        String groupId = data.get("groupId");
        String channelId = data.get("channelId");
        String senderId = data.get("senderId");

        Log.d(TAG, "📊 Notification Type: " + type);

        // Create intent based on notification type
        Intent intent = getIntentForNotificationType(type, chatId, groupId, channelId, senderId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (title + body).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 500, 250, 500});

        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = (title + body).hashCode();
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "✅ Notification displayed with ID: " + notificationId);
        }
    }

    /**
     * Create intent based on notification type
     */
    private Intent getIntentForNotificationType(String type, String chatId, String groupId,
                                                String channelId, String senderId) {
        Intent intent = new Intent();

        if ("private_chat".equals(type) && chatId != null && senderId != null) {
            // Open ChatDetailActivity for private messages
            intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("user_id", senderId);
            intent.putExtra("chat_id", chatId);
            Log.d(TAG, "🔗 Opening private chat with user: " + senderId);
        }
        else if ("group_chat".equals(type) && groupId != null) {
            // Open GroupChatActivity for group messages
            intent = new Intent(this, GroupChatActivity.class);
            intent.putExtra("groupId", groupId);
            Log.d(TAG, "🔗 Opening group chat: " + groupId);
        }
        else if ("channel".equals(type) && channelId != null) {
            // Open ChannelDetailActivity for channel messages
            intent = new Intent(this, ChannelDetailActivity.class);
            intent.putExtra("channelId", channelId);
            Log.d(TAG, "🔗 Opening channel: " + channelId);
        }
        else {
            // Fallback to MainActivity
            intent = new Intent(this, MainActivity.class);
            Log.d(TAG, "🔗 Opening MainActivity (fallback)");
        }

        // Set flags to clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    /**
     * Create notification channel (required for Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "EZ Talk Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifications for messages from chats, groups, and channels");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .build()
            );

            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel created");
            }
        }
    }
}