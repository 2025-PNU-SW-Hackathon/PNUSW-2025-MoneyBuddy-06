package com.moneybuddy.moneylog.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitProvider {
    private static volatile Retrofit retrofit;

    // TODO: 실제 백엔드 URL 로 교체
    private static final String BASE_URL = "https://your-backend.example/";

    public static Retrofit get(Context ctx, String tokenProviderValue) {
        if (retrofit == null) {
            synchronized (RetrofitProvider.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // Authorization: Bearer <JWT>
                    Interceptor auth = chain -> {
                        Request origin = chain.request();
                        Request.Builder b = origin.newBuilder();
                        if (tokenProviderValue != null && !tokenProviderValue.isEmpty()) {
                            b.addHeader("Authorization", "Bearer " + tokenProviderValue);
                        }
                        return chain.proceed(b.build());
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(auth)
                            .addInterceptor(log)
                            .build();

                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            .create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }
}
