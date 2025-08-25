package com.moneybuddy.moneylog.common;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moneybuddy.moneylog.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static volatile Retrofit instance;

    public static Retrofit get(Context ctx) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    // 🔧 실제 서버 주소 (슬래시 꼭!)
                    final String BASE_URL = "https://api.moneylog.app/";

                    // 앱 컨텍스트로 치환 (누수 방지)
                    Context appCtx = ctx.getApplicationContext();

                    // Authorization 헤더 인터셉터
                    Interceptor auth = chain -> {
                        Request orig = chain.request();
                        String token = com.moneybuddy.moneylog.UserRepository
                                .getInstance(appCtx)
                                .getAccessToken();  // 프로젝트 메소드에 맞게
                        Request.Builder b = orig.newBuilder()
                                .header("User-Agent", "MoneyLog-Android/" + BuildConfig.VERSION_NAME);
                        if (token != null && !token.isEmpty()) {
                            b.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(b.build());
                    };



                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(auth)
                            .addInterceptor(log)
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            // createdAt은 String으로 받고 어댑터에서 포맷팅하므로 커스텀 어댑터는 생략
                            .create();

                    instance = new Retrofit.Builder()
                            .baseUrl(BASE_URL) // 반드시 '/'로 끝나야 함
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();
                }
            }
        }
        return instance;
    }

    private RetrofitClient() {}
}
