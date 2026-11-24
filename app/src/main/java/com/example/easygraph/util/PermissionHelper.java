package com.example.easygraph.util;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Requests the runtime permissions the app needs (Bluetooth and notifications).
 * Must be created in onCreate(), before the activity is started.
 */
public class PermissionHelper {

    public interface Callback {
        void onResult(boolean allGranted);
    }

    private final AppCompatActivity activity;
    private final Callback callback;
    private final ActivityResultLauncher<String[]> launcher;

    public PermissionHelper(AppCompatActivity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> callback.onResult(!result.containsValue(false)));
    }

    /** Runtime permissions needed on the current Android version. */
    private List<String> getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            // Android 11 and below: Bluetooth discovery requires location
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        return permissions;
    }

    /** Asks only for the permissions that are still missing. */
    public void requestMissingPermissions() {
        List<String> missing = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }

        if (missing.isEmpty()) {
            callback.onResult(true);
        } else {
            launcher.launch(missing.toArray(new String[0]));
        }
    }
}
