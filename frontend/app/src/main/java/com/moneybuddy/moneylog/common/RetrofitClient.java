package com.moneybuddy.moneylog.common;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

// 앱 전체에서 Retrofit 인스턴스를 하나만 생성하고 공유함
public class RetrofitClient {
    private static volatile Retrofit retrofit = null;
    private static final String BASE_URL = "http://server-address/"; // 서버 주소로 변경하기

    // 외부에서 직접 객체를 생성하는 것을 막음 (싱글턴 패턴)
    private RetrofitClient() {}

    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    // ApiService 인터페이스를 쉽게 사용할 수 있도록 도와주는 메소드
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}