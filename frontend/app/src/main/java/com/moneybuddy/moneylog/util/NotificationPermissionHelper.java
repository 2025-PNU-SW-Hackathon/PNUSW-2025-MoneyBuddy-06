package com.moneybuddy.moneylog.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public final class NotificationPermissionHelper {

    private NotificationPermissionHelper() {}

    public static ActivityResultLauncher<String> register(
            ActivityResultCaller caller,
            Runnable onGranted,
            Runnable onDeniedOrNeverAsk
    ) {
        return caller.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted && onGranted != null) onGranted.run();
                    else if (onDeniedOrNeverAsk != null) onDeniedOrNeverAsk.run();
                }
        );
    }

    public static boolean hasPostNotifications(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
