package com.example.project_ez_talk.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.project_ez_talk.ui.call.incoming.IntegratedIncomingCallActivity;
import com.example.project_ez_talk.ui.call.video.IntegratedVideoCallActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing runtime permissions
 */
public class PermissionHelper {

    public static final int PERMISSION_REQUEST_CODE_CALL = 100;
    public static final int PERMISSION_REQUEST_CODE_CAMERA = 101;
    public static final int PERMISSION_REQUEST_CODE_AUDIO = 102;
    public static final int PERMISSION_REQUEST_CODE_STORAGE = 103;
    public static final int PERMISSION_REQUEST_CODE_CAMERA_AUDIO = 200;

    /**
     * Check if all call permissions are granted
     */
    public static boolean hasCallPermissions(Activity activity) {
        return hasPermission(activity, Manifest.permission.CAMERA) &&
                hasPermission(activity, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * Check if camera permission is granted
     */
    public static boolean hasCameraPermission(Activity activity) {
        return hasPermission(activity, Manifest.permission.CAMERA);
    }

    /**
     * Check if audio permission is granted
     */
    public static boolean hasAudioPermission(Activity activity) {
        return hasPermission(activity, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * Check if storage permissions are granted
     */
    public static boolean hasStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            return hasPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) &&
                    hasPermission(activity, Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            // Android 12 and below
            return hasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Check if a specific permission is granted
     */
    private static boolean hasPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request call permissions (Camera + Audio)
     */
    public static void requestCallPermissions(Activity activity) {
        List<String> permissions = new ArrayList<>();

        if (!hasPermission(activity, Manifest.permission.CAMERA)) {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (!hasPermission(activity, Manifest.permission.RECORD_AUDIO)) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE_CALL
            );
        }
    }

    /**
     * Request camera permission only
     */
    public static void requestCameraPermission(Activity activity) {
        if (!hasCameraPermission(activity)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE_CAMERA
            );
        }
    }

    /**
     * Request audio permission only
     */
    public static void requestAudioPermission(Activity activity) {
        if (!hasAudioPermission(activity)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE_AUDIO
            );
        }
    }

    /**
     * Request storage permissions
     */
    public static void requestStoragePermissions(Activity activity) {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (!hasPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (!hasPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            // Android 12 and below
            if (!hasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE_STORAGE
            );
        }
    }

    /**
     * Check if permission request result is granted
     */
    public static boolean isPermissionGranted(int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public static void requestCameraAndAudioPermission(IntegratedVideoCallActivity integratedVideoCallActivity) {

    }

    public static void requestCameraAndAudioPermission(IntegratedIncomingCallActivity integratedIncomingCallActivity) {

    }

    /**
     * Interface for permission callbacks
     */
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }
}