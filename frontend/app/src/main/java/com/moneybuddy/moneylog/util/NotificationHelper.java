package com.moneybuddy.moneylog.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.notification.activity.NotificationActivity;
import com.moneybuddy.moneylog.common.MoneyLogApp;

public final class NotificationHelper {
    private NotificationHelper() {}

    public static void showSimple(Context ctx, String title, String message) {
        Intent intent = new Intent(ctx, NotificationActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                ctx, 1001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, MoneyLogApp.CH_NOTI_GENERAL)
                .setSmallIcon(R.drawable.ic_stat_moneylog) // 작은 아이콘 준비 필요
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) System.currentTimeMillis(), b.build());
    }
}
