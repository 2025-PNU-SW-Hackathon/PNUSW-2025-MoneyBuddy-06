package com.moneybuddy.moneylog.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.moneybuddy.moneylog.challenge.network.ChallengeApiService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://172.21.170.228:8080/api/v1/";
    private static Retrofit retrofit = null;

    public static ChallengeApiService getApiService(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        SharedPreferences prefs = context.getSharedPreferences("user_auth", Context.MODE_PRIVATE);
                        String token = prefs.getString("access_token", null);
                        Request original = chain.request();
                        if (token != null && !token.isEmpty()) {
                            Request.Builder builder = original.newBuilder().header("Authorization", "Bearer " + token);
                            return chain.proceed(builder.build());
                        }
                        return chain.proceed(original);
                    }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ChallengeApiService.class);
    }
}