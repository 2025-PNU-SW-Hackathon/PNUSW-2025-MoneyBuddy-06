// util/TokenInterceptor.java
package com.moneybuddy.moneylog.common;

import androidx.annotation.Nullable;

import com.moneybuddy.moneylog.util.AuthManager;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {
    private final AuthManager auth;

    public TokenInterceptor(AuthManager auth) {
        this.auth = auth;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        @Nullable String token = auth.getAccessToken();
        Request req = chain.request();
        if (token != null && !token.isEmpty()) {
            req = req.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }
        return chain.proceed(req);
    }
}
