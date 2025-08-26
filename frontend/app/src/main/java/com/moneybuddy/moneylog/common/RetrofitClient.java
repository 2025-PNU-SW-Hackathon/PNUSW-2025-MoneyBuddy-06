package com.moneybuddy.moneylog.common;

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

/**
 * 앱 전역에서 하나의 Retrofit 인스턴스를 제공.
 * - DEBUG: 에뮬레이터 호스트 http://10.0.2.2:8080/
 * - RELEASE: 실제 API URL 로 교체
 *
 * TokenManager 싱글턴을 통해 Authorization 헤더를 자동 첨부합니다.
 */
public final class RetrofitClient {

    private static volatile Retrofit instance;

    // ⚠️ DEBUG/RELEASE 분기: 필요에 맞게 바꾸세요.
    private static final String BASE_URL_DEBUG   = "http://10.0.2.2:8080/";
    private static final String BASE_URL_RELEASE = "https://api.moneylog.app/"; // 예시

    private RetrofitClient() {}

    /** Retrofit 인스턴스 */
    public static Retrofit get(Context ctx) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    Context appCtx = ctx.getApplicationContext();

                    // Gson
                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    // 로깅 (DEBUG 일 때만 BODY)
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    // Authorization 헤더 자동 첨부 인터셉터
                    Interceptor auth = chain -> {
                        Request original = chain.request();
                        String token = TokenManager.getInstance(appCtx).getToken(); // null일 수 있음
                        if (token != null && !token.isEmpty()) {
                            Request withAuth = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(withAuth);
                        }
                        return chain.proceed(original);
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

    /** ApiService 바로 얻기 (권장 진입점) */
    public static ApiService getApiService(Context ctx) {
        return get(ctx).create(ApiService.class);
    }
}
