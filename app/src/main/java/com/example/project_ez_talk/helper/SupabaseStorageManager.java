package com.example.project_ez_talk.helper;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseStorageManager {
    private static final String TAG = "SupabaseStorage";

    private static final String SUPABASE_URL = "https://ijcfvpodwmshmdecmxmk.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlqY2Z2cG9kd21zaG1kZWNteG1rIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjYwNTM5NzQsImV4cCI6MjA4MTYyOTk3NH0.35y9_9TMIMEltfYRFs06oOPJwpIGEUHZQasXYkch3IQ";

    private static final String BUCKET_PROFILE_IMAGES = "profile-images";
    private static final String BUCKET_CHAT_IMAGES = "chat-images";
    private static final String BUCKET_DOCUMENTS = "chat-documents";
    private static final String BUCKET_AUDIO = "chat-audio";

    private static OkHttpClient client = new OkHttpClient();
    private static Context appContext;
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Initialize with application context
     * MUST be called before any uploads!
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        Log.d(TAG, "✅ SupabaseStorageManager initialized");
    }

    /**
     * Callback interface for upload operations
     */
    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(String error);
    }

    /**
     * Upload profile image to Supabase
     */
    public static void uploadProfileImage(Uri imageUri, String userId, UploadCallback callback) {
        uploadToSupabase(imageUri, BUCKET_PROFILE_IMAGES, userId, "profile.jpg", callback);
    }

    /**
     * Upload chat image to Supabase
     */
    public static void uploadChatImage(Uri imageUri, String userId, UploadCallback callback) {
        String fileName = "chat_" + System.currentTimeMillis() + ".jpg";
        uploadToSupabase(imageUri, BUCKET_CHAT_IMAGES, userId, fileName, callback);
    }

    /**
     * Upload audio file to Supabase
     */
    public static void uploadAudio(Uri audioUri, String userId, UploadCallback callback) {
        String fileName = "audio_" + System.currentTimeMillis() + ".m4a";
        uploadToSupabase(audioUri, BUCKET_AUDIO, userId, fileName, callback);
    }

    /**
     * Upload document to Supabase
     */
    public static void uploadDocument(Uri docUri, String userId, UploadCallback callback) {
        String fileName = "doc_" + System.currentTimeMillis() + ".pdf";
        uploadToSupabase(docUri, BUCKET_DOCUMENTS, userId, fileName, callback);
    }

    /**
     * Generic upload method to Supabase (runs on background thread)
     * ✅ Callbacks always run on main thread
     * ✅ Uses PUT to allow overwriting existing files
     */
    private static void uploadToSupabase(Uri fileUri, String bucket, String userId,
                                         String fileName, UploadCallback callback) {
        // Run on background thread to avoid blocking UI
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting upload for: " + fileName);

                // Check if initialized
                if (appContext == null) {
                    Log.e(TAG, "SupabaseStorageManager not initialized. Call init() first!");
                    mainHandler.post(() -> callback.onError("SupabaseStorageManager not initialized"));
                    return;
                }

                // Read file bytes from Uri
                byte[] fileBytes = readFileBytes(fileUri);
                if (fileBytes == null) {
                    Log.e(TAG, "Failed to read file bytes");
                    mainHandler.post(() -> callback.onError("Failed to read file"));
                    return;
                }

                Log.d(TAG, "File read successfully, size: " + fileBytes.length + " bytes");

                // Create file path: userId/fileName
                String filePath = userId + "/" + fileName;

                // Build API URL
                String apiUrl = SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + filePath;

                Log.d(TAG, "Uploading to: " + apiUrl);

                // Create request
                RequestBody requestBody = RequestBody.create(fileBytes, MediaType.parse("application/octet-stream"));

                Request request = new Request.Builder()
                        .url(apiUrl)
                        .header("Authorization", "Bearer " + SUPABASE_KEY)
                        .put(requestBody)  // ✅ Changed from POST to PUT (allows upsert/overwrite)
                        .build();

                // Execute request
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    // Generate public URL
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + filePath;
                    Log.d(TAG, "✅ Upload successful: " + publicUrl);

                    // ✅ Call callback on main thread
                    mainHandler.post(() -> callback.onSuccess(publicUrl));
                } else {
                    String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Upload failed: " + errorMessage);

                    // ✅ Call callback on main thread
                    mainHandler.post(() -> callback.onError("Upload failed: " + response.code()));
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "Upload error: " + e.getMessage(), e);

                // ✅ Call callback on main thread
                mainHandler.post(() -> callback.onError("Upload error: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Read file bytes from Uri
     * Supports both content:// URIs and file:// URIs
     */
    private static byte[] readFileBytes(Uri uri) {
        try {
            if (appContext == null) {
                Log.e(TAG, "AppContext is null - SupabaseStorageManager not initialized!");
                return null;
            }

            Log.d(TAG, "Reading file from URI: " + uri);

            // Open input stream from content resolver
            InputStream inputStream = appContext.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }

            // Read all bytes from stream
            byte[] buffer = new byte[8192];  // 8KB buffer for better performance
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            byte[] fileBytes = outputStream.toByteArray();

            Log.d(TAG, "✅ Successfully read " + fileBytes.length + " bytes from file");
            return fileBytes;

        } catch (Exception e) {
            Log.e(TAG, "Error reading file: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Delete file from Supabase
     */
    public static void deleteFile(String bucket, String filePath, UploadCallback callback) {
        new Thread(() -> {
            try {
                if (appContext == null) {
                    mainHandler.post(() -> callback.onError("SupabaseStorageManager not initialized"));
                    return;
                }

                String apiUrl = SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + filePath;

                Request request = new Request.Builder()
                        .url(apiUrl)
                        .header("Authorization", "Bearer " + SUPABASE_KEY)
                        .delete()
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    Log.d(TAG, "File deleted successfully");
                    mainHandler.post(() -> callback.onSuccess("Deleted"));
                } else {
                    mainHandler.post(() -> callback.onError("Delete failed: " + response.code()));
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "Delete error: " + e.getMessage());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    /**
     * Get public URL for a file
     */
    public static String getPublicUrl(String bucket, String filePath) {
        return SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + filePath;
    }
}