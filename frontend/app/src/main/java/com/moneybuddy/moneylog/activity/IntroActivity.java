package com.moneybuddy.moneylog.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.moneybuddy.moneylog.common.TokenManager;

public class IntroActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 안드로이드 12 이상을 위한 스플래시 스크린 API -> 8로 수정하기
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 데이터 로딩 등 비동기 작업 시 스플래시 스크린이 계속 보임
        splashScreen.setKeepOnScreenCondition(() -> true);

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