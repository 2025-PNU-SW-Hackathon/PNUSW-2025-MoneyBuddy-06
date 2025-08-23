package com.moneybuddy.moneylog.common;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "moneybuddy_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private SharedPreferences prefs;

    // 생성자
    public TokenManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 로그인 성공했을 때 토큰 저장
    public void saveAccessToken(String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    // 저장된 토큰 꺼내오기
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    // 로그아웃할 때 토큰 삭제
    public void clearAccessToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
    }
}