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
    private static final String BASE_URL_DEBUG   = "http://10.0.2.2:8080/";
    private static final String BASE_URL_RELEASE = "https://api.moneylog.app/";

    private RetrofitClient() {}

    /** 전역 Retrofit 인스턴스 */
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

                    // 토큰 자동 첨부
                    Interceptor auth = chain -> {
                        Request req = chain.request();
                        String token = TokenManager.getInstance(app).getToken();
                        if (token != null && !token.isEmpty()) {
                            req = req.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                        }
                        return chain.proceed(req);
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(log)
                            .addInterceptor(auth)
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

    /** 임의의 API 인터페이스 생성 (권장) */
    public static <T> T getService(Context ctx, Class<T> apiClass) {
        return get(ctx).create(apiClass);
    }

    /** 편의 메서드: 공통 ApiService */
    public static ApiService api(Context ctx) {
        return getService(ctx, ApiService.class);
    }

    /** 편의 메서드: 로그인/회원 관련 AuthApi (있다면) */
    public static com.moneybuddy.moneylog.login.network.AuthApi auth(Context ctx) {
        return getService(ctx, com.moneybuddy.moneylog.login.network.AuthApi.class);
    }
}
