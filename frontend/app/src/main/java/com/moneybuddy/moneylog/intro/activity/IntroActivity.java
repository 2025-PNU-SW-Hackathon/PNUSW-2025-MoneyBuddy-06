package com.moneybuddy.moneylog.intro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.OptIn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.login.activity.LoginActivity;
import com.moneybuddy.moneylog.main.activity.MainMenuActivity;
import com.moneybuddy.moneylog.login.network.AuthRepository;
import com.moneybuddy.moneylog.login.network.MobtiCheckCallback;
import com.moneybuddy.moneylog.mobti.activity.MobtiActivity;

public class IntroActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 800L;

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        final TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String userToken = (tokenManager != null) ? tokenManager.getToken() : null;
            boolean loggedIn = userToken != null && !userToken.trim().isEmpty();

            if (loggedIn) {
                // 로그인 상태이면 MoBTI 상태 확인
                AuthRepository repo = new AuthRepository();
                repo.checkMobtiStatus(this, new MobtiCheckCallback() {
                    @Override
                    public void onMobtiExists() {
                        // MoBTI 결과가 있으면 홈 화면으로 이동
                        startActivity(new Intent(IntroActivity.this, MainMenuActivity.class));
                        finish();
                    }

                    @Override
                    public void onMobtiNotExists() {
                        // MoBTI 결과가 없으면 검사 화면으로 이동
                        startActivity(new Intent(IntroActivity.this, MobtiActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        // 오류 발생 시 홈 화면으로 이동
                        Toast.makeText(IntroActivity.this, message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(IntroActivity.this, MainMenuActivity.class));
                        finish();
                    }
                });
            } else {
                // 로그인 상태가 아니면 로그인 화면으로 이동
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                finish();
            }
        }, SPLASH_DELAY_MS);
    }
}
