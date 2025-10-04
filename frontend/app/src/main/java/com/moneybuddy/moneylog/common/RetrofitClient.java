package com.moneybuddy.moneylog.common;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moneybuddy.moneylog.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static volatile Retrofit instance;

    // 필요 시 교체
    private static final String BASE_URL_DEBUG   = "http://172.21.170.228:8080/";
    private static final String BASE_URL_RELEASE = "http://172.21.170.228:8080/";

    private RetrofitClient() {}

    public static Retrofit get(Context ctx) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    Context app = ctx.getApplicationContext();

                    Gson gson = new GsonBuilder().setLenient().create();

                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    TokenManager tokenManager = TokenManager.getInstance(app);
                    AuthInterceptor authInterceptor = new AuthInterceptor(tokenManager);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(log)
                            .addInterceptor(authInterceptor)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();

                    String baseUrl = BuildConfig.DEBUG ? BASE_URL_DEBUG : BASE_URL_RELEASE;

                    instance = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();
                }
            }
        }
        return instance;
    }

    public static <T> T getService(Context ctx, Class<T> apiClass) {
        return get(ctx).create(apiClass);
    }

    public static ApiService api(Context ctx) {
        return getService(ctx, ApiService.class);
    }

    public static com.moneybuddy.moneylog.login.network.AuthApi auth(Context ctx) {
        return getService(ctx, com.moneybuddy.moneylog.login.network.AuthApi.class);
    }
}
