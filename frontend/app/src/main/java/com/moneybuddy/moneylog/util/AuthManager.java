package com.moneybuddy.moneylog.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF = "auth_pref";
    private static final String KEY_ACCESS = "access";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";

    private final SharedPreferences sp;

    public AuthManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveLogin(String token, Long userId, String email) {
        sp.edit()
                .putString(KEY_ACCESS, token)
                .putLong(KEY_UID, userId == null ? -1L : userId)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public String getAccessToken() { return sp.getString(KEY_ACCESS, null); }
    public Long getUserId() { long v = sp.getLong(KEY_UID, -1L); return v == -1L ? null : v; }
    public String getEmail() { return sp.getString(KEY_EMAIL, null); }
    public void clear() { sp.edit().clear().apply(); }
}
