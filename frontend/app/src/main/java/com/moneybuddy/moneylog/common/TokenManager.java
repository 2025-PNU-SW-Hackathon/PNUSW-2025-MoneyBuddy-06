package com.moneybuddy.moneylog.common;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public final class TokenManager {

    private static final String PREFS = "auth_prefs";            // 신규 저장소
    private static final String LEGACY_PREFS = "moneybuddy_prefs"; // 레거시 저장소(자동 마이그레이션)
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static volatile TokenManager INSTANCE;
    private final SharedPreferences prefs;

    // 외부 new 금지
    private TokenManager(Context ctx) {
        Context app = ctx.getApplicationContext();
        this.prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        migrateFromLegacy(app);
    }

    // 싱글턴 접근자
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

    // ---- 마이그레이션: 예전 PREF에 토큰이 있으면 옮기기 ----
    private void migrateFromLegacy(Context appCtx) {
        if (prefs.getString(KEY_ACCESS_TOKEN, null) != null) return;
        SharedPreferences legacy = appCtx.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE);
        String old = legacy.getString(KEY_ACCESS_TOKEN, null);
        if (old != null) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, old).apply();
            legacy.edit().remove(KEY_ACCESS_TOKEN).apply();
        }
    }

    // ---- 기본 API ----
    @Nullable
    public String getToken() { return prefs.getString(KEY_ACCESS_TOKEN, null); }

    public void setToken(@Nullable String token) {
        if (token == null) prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
        else prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public void clear() { prefs.edit().remove(KEY_ACCESS_TOKEN).apply(); }

    public boolean isLoggedIn() { return getToken() != null && !getToken().isEmpty(); }

    // ---- 호환용 별칭(기존 코드와 병행 사용 가능) ----
    public String get() { return getToken(); }
    public String getAccessToken() { return getToken(); }
    public void saveAccessToken(String token) { setToken(token); }
    public void clearAccessToken() { clear(); }
}
