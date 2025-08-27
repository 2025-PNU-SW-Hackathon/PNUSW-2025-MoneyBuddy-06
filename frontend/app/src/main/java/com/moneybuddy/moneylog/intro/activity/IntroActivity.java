package com.moneybuddy.moneylog.intro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.login.activity.LoginActivity;
import com.moneybuddy.moneylog.main.activity.MainMenuActivity;

public class IntroActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 800L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        final TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String userToken = (tokenManager != null) ? tokenManager.getToken() : null;
            boolean loggedIn = userToken != null && !userToken.trim().isEmpty();

            Class<?> dest = loggedIn ? MainMenuActivity.class : LoginActivity.class;
            startActivity(new Intent(IntroActivity.this, dest));
            finish(); // 인트로로 뒤로가기 방지
        }, SPLASH_DELAY_MS);
    }
}
