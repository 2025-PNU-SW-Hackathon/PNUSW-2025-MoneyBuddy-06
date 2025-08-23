package com.moneybuddy.moneylog.common;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.moneybuddy.moneylog.BuildConfig;

public class MoneyLogApp extends Application {
    @Override public void onCreate() {
        super.onCreate();

        if (!FirebaseApp.getApps(this).isEmpty()) return;

        //필수값 체크
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
