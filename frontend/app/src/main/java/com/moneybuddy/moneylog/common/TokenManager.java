package com.moneybuddy.moneylog.common;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

public final class TokenManager {

    private static final String PREFS_NAME = "auth";
    private static final String KEY_ACCESS_TOKEN = "token";
    // ▼▼▼ AuthManager에서 사용하던 키 추가 ▼▼▼
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "user_email";

    private static volatile TokenManager INSTANCE;
    private final SharedPreferences prefs;

    public TokenManager(Context ctx) {
        // SharedPreferences 인스턴스 생성
        this.prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 싱글턴 인스턴스를 가져오는 메서드
    public static TokenManager getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (TokenManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TokenManager(ctx);
                }
            }
        }
        return INSTANCE;
    }

    // ▼▼▼ AuthManager의 saveLogin 기능을 대체하는 메서드 ▼▼▼
    public void saveLoginSession(String token, Long userId, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        if (userId != null) {
            editor.putLong(KEY_USER_ID, userId);
        }
        if (email != null) {
            editor.putString(KEY_EMAIL, email);
        }
        editor.apply();
    }

    // ▼▼▼ AuthManager의 clear 기능을 대체하는 메서드 (모든 정보 삭제) ▼▼▼
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    // ---- 토큰 관련 메서드 ----
    @Nullable
    public String getToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void setToken(@Nullable String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    // ---- 사용자 정보 관련 메서드 ----
    @Nullable
    public Long getUserId() {
        long id = prefs.getLong(KEY_USER_ID, -1L);
        return id == -1L ? null : id;
    }

    @Nullable
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // ---- 로그인 상태 확인 메서드 ----
    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }
}