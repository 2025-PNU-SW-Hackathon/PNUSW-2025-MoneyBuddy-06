package com.moneybuddy.moneylog.api;

import android.content.Context;

import com.moneybuddy.moneylog.util.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://SERVER_BASE_URL/"; // 실제 서버 주소로 변경하기
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = new TokenManager(context);

            // OkHttpClient에 인터셉터(요청 가로채기) 기능 추가
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                String accessToken = tokenManager.getAccessToken();

                // 토큰이 저장되어 있다면, 모든 요청 헤더에 "Authorization" 추가
                if (accessToken != null) {
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + accessToken)
                            .method(original.method(), original.body());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }

                // 토큰 없으면 그냥 원래 요청 내보내기
                return chain.proceed(original);
            });

            OkHttpClient client = httpClient.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}