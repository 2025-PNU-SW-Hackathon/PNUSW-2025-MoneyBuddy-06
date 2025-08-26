package com.moneybuddy.moneylog.common;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public final class TokenManager {
    private static volatile TokenManager INSTANCE;
    private final SharedPreferences prefs;

    private static final String PREFS = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    // ✅ private 생성자 (외부 new 금지)
    private TokenManager(Context ctx) {
        Context app = ctx.getApplicationContext();
        this.prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ✅ 싱글턴 접근자
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

    // 토큰 읽기/쓰기 API
    @Nullable
    public String getToken() { return prefs.getString(KEY_ACCESS_TOKEN, null); }

    // 기존 코드 호환용 별칭 (new 코드에선 getToken() 사용 권장)
    public String get() { return getToken(); }

    public void setToken(@Nullable String token) {
        if (token == null) prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
        else prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public void clear() { prefs.edit().remove(KEY_ACCESS_TOKEN).apply(); }
}
