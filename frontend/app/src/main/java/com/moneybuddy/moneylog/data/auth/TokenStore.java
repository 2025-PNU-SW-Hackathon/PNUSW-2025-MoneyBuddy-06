package com.moneybuddy.moneylog.data.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private static final String PREF = "auth_pref";
    private static final String KEY = "jwt";
    private final SharedPreferences sp;

    public TokenStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
    public void save(String jwt){ sp.edit().putString(KEY, jwt).apply(); }
    public String get(){ return sp.getString(KEY, null); }
    public void clear(){ sp.edit().remove(KEY).apply(); }
}
