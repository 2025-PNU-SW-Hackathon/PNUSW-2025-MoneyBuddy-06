package com.moneybuddy.moneylog.intro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.main.activity.MainActivity;

public class IntroActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // TokenManager 초기화
        tokenManager = new TokenManager(this);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String userToken = tokenManager.getToken();
            
            if (userToken == null || userToken.isEmpty()) {
                moveToActivity(LoginActivity.class);
            } else {
                moveToActivity(MainActivity.class);
            }
        }, 1500);
    }

    // 지정된 액티비티로 화면 전환, 현재 액티비티 종료
    private void moveToActivity(Class<?> activityClass) {
        Intent intent = new Intent(IntroActivity.this, activityClass);
        startActivity(intent);
        finish();
    }
}