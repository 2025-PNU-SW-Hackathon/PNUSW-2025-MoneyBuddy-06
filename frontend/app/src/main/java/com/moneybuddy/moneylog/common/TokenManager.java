package com.moneybuddy.moneylog.common;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    // SharedPreferences 파일 이름과 토큰을 저장할 때 사용할 키
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_USER_TOKEN = "user_token";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    // SharedPreferences 초기화
    public TokenManager(Context context) {
        // 머니로그 앱 내에서만 SharedPreferences 파일에 접근 가능
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

     // 사용자 토큰을 SharedPreferences에 저장
    public void saveToken(String token) {
        // 키-값 쌍으로 데이터 저장, apply()로 비동기 저장 처리
        editor.putString(KEY_USER_TOKEN, token);
        editor.apply();
    }

    // SharedPreferences에 저장된 사용자 토큰 불러옴
    public String getToken() {
        return sharedPreferences.getString(KEY_USER_TOKEN, null);
    }

    // SharedPreferences에 저장된 사용자 토큰 삭제
    public void clearToken() {
        editor.remove(KEY_USER_TOKEN);
        editor.apply();
    }
}