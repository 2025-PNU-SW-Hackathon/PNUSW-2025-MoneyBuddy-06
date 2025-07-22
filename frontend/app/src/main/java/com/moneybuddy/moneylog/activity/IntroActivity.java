package com.moneybuddy.moneylog.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
            String userToken = sharedPreferences.getString("user_token", null);

            if (userToken == null || userToken.isEmpty()) {
                moveToActivity(LoginActivity.class);
            } else {
                moveToActivity(MainActivity.class);
            }
        }, 1500);
    }

    private void moveToActivity(Class<?> activityClass) {
        Intent intent = new Intent(IntroActivity.this, activityClass);
        startActivity(intent);
        finish();
    }
}