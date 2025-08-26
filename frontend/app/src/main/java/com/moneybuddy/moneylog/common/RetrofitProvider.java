package com.moneybuddy.moneylog.common;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moneybuddy.moneylog.login.network.AuthApi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitProvider {
    private static volatile Retrofit retrofit;

    public static Retrofit get(Context ctx) {
        if (retrofit == null) {
            synchronized (RetrofitProvider.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(log)
                            // .addInterceptor(new TokenInterceptor(new AuthManager(ctx))) // 로그인 이후 요청에 토큰 자동 부착하려면 사용
                            .build();

                    Gson gson = new GsonBuilder().setLenient().create();

                    retrofit = new Retrofit.Builder()
                            // 🔧 실제 서버 URL (끝에 /)
                            .baseUrl("http://10.0.2.2:8080/")
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static AuthApi authApi(Context ctx) {
        return get(ctx).create(AuthApi.class);
    }
}
