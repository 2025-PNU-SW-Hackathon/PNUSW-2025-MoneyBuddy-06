package com.moneybuddy.moneylog.common;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.moneybuddy.moneylog.BuildConfig;

public class MoneyLogApp extends Application {

    public static final String CH_NOTI_GENERAL = "ml_general"; // 시스템 알림 채널 ID

    @Override public void onCreate() {
        super.onCreate();

        // 1) 알림 채널은 항상 먼저/항상 생성 (Oreo+)
        createNotificationChannels();

        // 2) Firebase는 환경값이 있을 때만 초기화 (이미 초기화 되어 있으면 스킵)
        initFirebaseIfNeeded();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CH_NOTI_GENERAL,
                    "일반 알림",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            ch.setDescription("머니로그 일반 알림 채널");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private void initFirebaseIfNeeded() {
        try {
            if (!FirebaseApp.getApps(this).isEmpty()) {
                // 이미 초기화됨 (google-services.json 또는 이전 초기화)
                return;
            }

            // 필수값 없으면 스킵
            if (isEmpty(BuildConfig.FB_APP_ID) || isEmpty(BuildConfig.FB_API_KEY) || isEmpty(BuildConfig.FB_PROJECT_ID)) {
                Log.w("MoneyLogApp", "Firebase skipped: missing BuildConfig values");
                return;
            }

            FirebaseOptions opts = new FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FB_API_KEY)
                    .setApplicationId(BuildConfig.FB_APP_ID)
                    .setProjectId(BuildConfig.FB_PROJECT_ID)
                    .setGcmSenderId(BuildConfig.FB_SENDER_ID)
                    .setStorageBucket(BuildConfig.FB_STORAGE)
                    .build();

            FirebaseApp.initializeApp(this, opts);
        } catch (Exception e) {
            Log.e("MoneyLogApp", "Firebase init failed", e);  // 크래시 방지
        }
    }

    private boolean isEmpty(String s) { return s == null || s.isEmpty(); }
}
