package com.moneybuddy.moneylog;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MoneyLogApp extends Application {
    @Override public void onCreate() {
        super.onCreate();

        // 이미 다른 경로로 초기화됐으면 생략
        if (!FirebaseApp.getApps(this).isEmpty()) return;

        // ⚠️ 필수값 체크 (없으면 초기화 스킵해서 크래시 방지)
        if (isEmpty(BuildConfig.FB_APP_ID) || isEmpty(BuildConfig.FB_API_KEY) || isEmpty(BuildConfig.FB_PROJECT_ID)) {
            Log.w("MoneyLogApp", "Firebase skipped: missing BuildConfig values");
            return;
        }

        try {
            FirebaseOptions opts = new FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FB_API_KEY)
                    .setApplicationId(BuildConfig.FB_APP_ID)
                    .setProjectId(BuildConfig.FB_PROJECT_ID)
                    .setGcmSenderId(BuildConfig.FB_SENDER_ID)
                    .setStorageBucket(BuildConfig.FB_STORAGE)
                    .build();
            FirebaseApp.initializeApp(this, opts);
        } catch (Exception e) {
            Log.e("MoneyLogApp", "Firebase init failed", e);  // ← 크래시 대신 로그만
        }
    }

    private boolean isEmpty(String s) { return s == null || s.isEmpty(); }
}
