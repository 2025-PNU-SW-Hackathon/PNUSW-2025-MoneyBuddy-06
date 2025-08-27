package com.moneybuddy.moneylog.common;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @Nullable private final TokenManager tokenManager;
    public AuthInterceptor(@Nullable TokenManager tokenManager){ this.tokenManager = tokenManager; }

    @Override public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String token = tokenManager == null ? null : tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            req = req.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }
        return chain.proceed(req);
    }
}
