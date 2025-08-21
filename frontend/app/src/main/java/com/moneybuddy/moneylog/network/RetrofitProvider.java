package com.moneybuddy.moneylog.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitProvider {

    private static Retrofit instance;

    public static Retrofit get(Context ctx) {
        if (instance == null) {
            synchronized (RetrofitProvider.class) {
                if (instance == null) {
                    // 🔧 실제 서버 주소로 교체
                    String BASE_URL = "https://api.moneylog.app";

                    // Authorization 헤더 인터셉터 (프로젝트에 맞게 토큰 조회 부분만 교체)
                    Interceptor auth = chain -> {
                        Request orig = chain.request();
                        String token = com.moneybuddy.moneylog.UserRepository
                                .getInstance(ctx).getAccessToken(); // ← 프로젝트 메소드명에 맞게 조정
                        Request req = (token == null || token.isEmpty())
                                ? orig
                                : orig.newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(req);
                    };

                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(auth)
                            .addInterceptor(log)
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .build();

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    instance = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();
                }
            }
        }
        return instance;
    }

    private RetrofitProvider() {}
}
