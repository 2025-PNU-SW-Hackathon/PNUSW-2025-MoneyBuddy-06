package com.moneybuddy.moneylog.intro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.login.activity.LoginActivity;
import com.moneybuddy.moneylog.main.fragment.MainMenuHomeFragment;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        TokenManager tokenManager = TokenManager.getInstance(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String userToken = tokenManager.getToken(); // 또는 getToken()

            if (userToken == null || userToken.isEmpty()) {
                moveToActivity(LoginActivity.class);
            } else {
                moveToActivity(MainMenuHomeFragment.class); // 또는 MainMenuActivity.class
            }
        }, 1500);
    }

    private void moveToActivity(Class<?> activityClass) {
        startActivity(new Intent(IntroActivity.this, activityClass));
        finish();
    }
}
