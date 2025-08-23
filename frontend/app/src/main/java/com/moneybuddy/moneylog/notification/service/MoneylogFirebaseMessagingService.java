package com.moneybuddy.moneylog.notification.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.notification.activity.PushOpenActivity;
import com.moneybuddy.moneylog.notification.network.PushTrackingRepository;

import java.util.Map;

public class MoneylogFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "ml_push_default";
    private static final String CHANNEL_NAME = "MoneyLog 알림";
    private static final int REQ_OPEN = 10;

    private PushTrackingRepository repo;

    @Override public void onCreate() {
        super.onCreate();
        repo = new PushTrackingRepository(getApplicationContext());
        ensureChannel();
    }

    /** 앱이 토큰을 새로 받았을 때 서버로 전송 */
    @Override public void onNewToken(String token) {
        repo.registerToken(token, null); // 실패해도 앱 흐름 막지 않음
    }

    /** 데이터 메시지 수신(포그라운드 + 백그라운드 data-only) */
    @Override public void onMessageReceived(RemoteMessage msg) {
        Map<String, String> data = msg.getData();
        long nid = parseLong(data.get("notificationId"));
        // 1) 도착 기록 (가능한 한 빨리)
        repo.trackDelivered(nid, data);

        // 2) 포그라운드일 때는 수동으로 heads-up 표시
        //    (notification payload인 경우 시스템이 대신 띄우므로 여기선 생략 가능)
        String title = firstNonEmpty(
                data.get("title"),
                msg.getNotification() != null ? msg.getNotification().getTitle() : null,
                "머니로그 알림"
        );
        String body = firstNonEmpty(
                data.get("body"),
                msg.getNotification() != null ? msg.getNotification().getBody() : null,
                ""
        );
        showLocalNotification(nid, title, body, data);
    }

    private void showLocalNotification(long nid, String title, String body, Map<String, String> data) {
        // 클릭 시 열릴 중간 Activity (open 트래킹 + 라우팅)
        Intent open = new Intent(this, PushOpenActivity.class);
        for (Map.Entry<String, String> e : data.entrySet()) {
            open.putExtra(e.getKey(), e.getValue());
        }
        open.putExtra("notificationId", String.valueOf(nid));

        PendingIntent pi = PendingIntent.getActivity(
                this, REQ_OPEN, open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_moneylog) // 상태바용 24x24 단색 아이콘
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) (nid != 0 ? nid : System.currentTimeMillis()), b.build());
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                ch.enableLights(true);
                ch.setLightColor(Color.GREEN);
                ch.enableVibration(true);
                nm.createNotificationChannel(ch);
            }
        }
    }

    private static long parseLong(@Nullable String s) {
        try { return s == null ? 0 : Long.parseLong(s); } catch (Exception e) { return 0; }
    }

    @SafeVarargs
    private static <T> T firstNonEmpty(T... vals) {
        for (T v : vals) { if (v instanceof String) { if (v != null && !((String) v).isEmpty()) return v; }
        else if (v != null) return v; }
        return null;
    }
}
