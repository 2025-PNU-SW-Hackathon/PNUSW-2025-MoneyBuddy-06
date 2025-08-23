package com.moneybuddy.moneylog.common;

import android.content.Context;
import com.moneybuddy.moneylog.common.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static volatile Retrofit retrofitInstance = null;
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    // 다른 곳에서 객체를 생성하지 못하도록 막음
    private RetrofitClient() {}

    // Retrofit 인스턴스 반환
    public static Retrofit getClient(Context context) {
        if (retrofitInstance == null) {
            synchronized (RetrofitClient.class) {
                if (retrofitInstance == null) {
                    TokenManager tokenManager = new TokenManager(context.getApplicationContext());

                    // OkHttpClient를 커스텀해서 모든 요청에 토큰을 자동으로 추가
                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                    httpClient.addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        String accessToken = tokenManager.getAccessToken();

                        // 토큰이 존재하면, 헤더에 'Authorization'을 추가해서 보냄
                        if (accessToken != null && !accessToken.isEmpty()) {
                            Request.Builder builder = originalRequest.newBuilder()
                                    .header("Authorization", "Bearer " + accessToken);
                            Request newRequest = builder.build();
                            return chain.proceed(newRequest);
                        }

                        // 토큰이 없으면 원래 요청 그대로 보냄
                        return chain.proceed(originalRequest);
                    });

                    // 커스텀한 OkHttpClient와 함께 Retrofit 인스턴스 생성
                    retrofitInstance = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(httpClient.build())
                            .build();
                }
            }
        }
        return retrofitInstance;
    }

    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }
}