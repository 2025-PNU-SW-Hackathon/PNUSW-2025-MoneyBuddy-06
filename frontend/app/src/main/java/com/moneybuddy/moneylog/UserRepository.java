package com.moneybuddy.moneylog;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import retrofit2.Response;
/**
 * 앱 전역 사용자 상태/토큰 저장소 (SharedPreferences 기반)
 *
 * 사용 예)
 *  - 로그인 성공 시:  UserRepository.getInstance(ctx).saveTokens(access, refresh);
 *  - API 인터셉터:   UserRepository.getInstance(ctx).getAccessToken();
 *  - 로그아웃:       UserRepository.getInstance(ctx).clear();
 */
public final class UserRepository {

    private static volatile UserRepository INSTANCE;

    private static final String PREFS_NAME = "user_repo_prefs";
    private static final String KEY_ACCESS_TOKEN  = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID       = "user_id"; // 필요 없으면 사용 안 해도 됨

    private final SharedPreferences prefs;

    private UserRepository(Context appContext) {
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** 앱 어디서든 안전하게 호출 가능한 싱글턴 */
    public static UserRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    Context appCtx = context.getApplicationContext();
                    INSTANCE = new UserRepository(appCtx);
                }
            }
        }
        return INSTANCE;
    }

    /** 로그인/토큰 재발급 성공 시 호출 */
    public void saveTokens(@Nullable String accessToken, @Nullable String refreshToken) {
        SharedPreferences.Editor e = prefs.edit();
        if (accessToken != null)  e.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) e.putString(KEY_REFRESH_TOKEN, refreshToken);
        e.apply();
    }

    /** 액세스 토큰만 갱신해야 할 때 */
    public void updateAccessToken(@Nullable String accessToken) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
    }

    @Nullable
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    @Nullable
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void setUserId(@Nullable String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    @Nullable
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /** 로그인되어 있는지 간단 체크 (토큰 null/빈문자열 제외) */
    public boolean isLoggedIn() {
        String t = getAccessToken();
        return t != null && !t.isEmpty();
    }

    /** 로그아웃/계정 전환 시 전부 제거 */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
