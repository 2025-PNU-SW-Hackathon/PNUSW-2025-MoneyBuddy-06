package com.moneybuddy.moneylog.notification.network;

import android.content.Context;

import androidx.annotation.Nullable;

import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.notification.model.ListResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class NotificationRepository {

    private final NotificationApi api;

    public NotificationRepository(Context ctx) {
        this.api = com.moneybuddy.moneylog.common.RetrofitClient
                .getService(ctx, com.moneybuddy.moneylog.notification.network.NotificationApi.class);
    }



    // 목록/카운트 조회


    public void getNotifications(@Nullable Long cursor, @Nullable Integer size,
                                 Callback<ListResponse> cb) {
        api.getNotifications(cursor, size).enqueue(cb);
    }

    public void getUnreadCount(Callback<Integer> cb) {
        api.getUnreadCount().enqueue(cb);
    }


    // 읽음 처리


    public void markRead(long id, @Nullable Runnable onDone) {
        api.markRead(id).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (onDone != null) onDone.run();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                if (onDone != null) onDone.run();
            }
        });
    }


    public void markAsRead(long id) {
        markRead(id, null);
    }

    public void markAllRead(@Nullable Runnable onDone) {
        api.markAllRead().enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (onDone != null) onDone.run();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                if (onDone != null) onDone.run();
            }
        });
    }

    public void markAllRead(retrofit2.Callback<Void> cb) {
        api.markAllRead().enqueue(cb);
    }

}
