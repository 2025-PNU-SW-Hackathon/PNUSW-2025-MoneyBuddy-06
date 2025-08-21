package com.moneybuddy.moneylog.data.network;

import androidx.annotation.Nullable;

import com.moneybuddy.moneylog.data.auth.TokenStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @Nullable private final TokenStore tokenStore;
    public AuthInterceptor(@Nullable TokenStore tokenStore){ this.tokenStore = tokenStore; }

    @Override public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String token = tokenStore == null ? null : tokenStore.get();
        if (token != null && !token.isEmpty()) {
            req = req.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }
        return chain.proceed(req);
    }
}
