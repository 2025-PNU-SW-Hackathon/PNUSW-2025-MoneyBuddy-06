package com.moneybuddy.moneylog.notification.network;

import android.content.Context;
import android.os.Build;

import com.moneybuddy.moneylog.common.RetrofitClient;

import java.util.Map;

import retrofit2.Callback;

public class PushTrackingRepository {

    private final PushTrackingApi api;
    private final Context ctx;

    public PushTrackingRepository(Context appCtx) {
        this.ctx = appCtx.getApplicationContext();
        this.api = RetrofitClient.getClient().create(PushTrackingApi.class);
    }

    public void registerToken(String token, Callback<Void> cb) {
        String version = "unknown";
        try {
            version = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (Exception ignored) {}
        PushTrackingApi.PushTokenBody body =
                new PushTrackingApi.PushTokenBody(token, version, Build.MODEL);
        if (cb == null) {
            api.registerToken(body).enqueue(new Noop<Void>());
        } else {
            api.registerToken(body).enqueue(cb);
        }
    }

    public void trackDelivered(long id, Map<String,String> payload) {
        api.trackDelivered(id, new PushTrackingApi.PushEventBody(System.currentTimeMillis(), payload))
                .enqueue(new Noop<>());
    }

    public void trackOpened(long id, Map<String,String> payload) {
        api.trackOpened(id, new PushTrackingApi.PushEventBody(System.currentTimeMillis(), payload))
                .enqueue(new Noop<>());
    }

    /** 빈 콜백 */
    private static class Noop<T> implements Callback<T> {
        @Override public void onResponse(retrofit2.Call<T> call, retrofit2.Response<T> response) {}
        @Override public void onFailure(retrofit2.Call<T> call, Throwable t) {}
    }
}
