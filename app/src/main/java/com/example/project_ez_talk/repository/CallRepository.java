package com.example.project_ez_talk.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

/**
 * CallRepository - Manager/Repository for call operations
 * Uses singleton pattern for application-wide access
 * Coordinates call initialization and lifecycle
 */
public class CallRepository {

    private static final String TAG = "CallRepository";
    @SuppressLint("StaticFieldLeak")
    private static CallRepository instance;

    private Context context;
    private String currentUserId;
    private boolean isInitialized = false;

    /**
     * Callback interface for initialization
     */
    public interface OnInitListener {
        void onSuccess();
        void onError();
    }

    /**
     * Private constructor for singleton pattern
     */
    private CallRepository() {
        Log.d(TAG, "✅ CallRepository created");
    }

    /**
     * Get singleton instance
     */
    public static synchronized CallRepository getInstance() {
        if (instance == null) {
            instance = new CallRepository();
            Log.d(TAG, "✅ CallRepository singleton instance created");
        }
        return instance;
    }

    /**
     * Initialize call repository
     * @param context Android context
     * @param userId Current user's Firebase UID
     * @param listener Callback for initialization result
     */
    public void init(Context context, String userId, OnInitListener listener) {
        try {
            this.context = context;
            this.currentUserId = userId;

            Log.d(TAG, "🚀 Initializing CallRepository");
            Log.d(TAG, "   User ID: " + userId);

            // Mark as initialized
            this.isInitialized = true;

            Log.d(TAG, "✅ CallRepository initialized successfully");

            // Callback on success
            if (listener != null) {
                listener.onSuccess();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing CallRepository: " + e.getMessage(), e);

            // Callback on error
            if (listener != null) {
                listener.onError();
            }
        }
    }

    /**
     * Check if repository is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Get context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Reset/cleanup the repository
     */
    public void reset() {
        this.context = null;
        this.currentUserId = null;
        this.isInitialized = false;
        Log.d(TAG, "🔄 CallRepository reset");
    }

    /**
     * Cleanup and destroy singleton
     */
    public static void destroy() {
        if (instance != null) {
            instance.reset();
            instance = null;
            Log.d(TAG, "✅ CallRepository destroyed");
        }
    }
}