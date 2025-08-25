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
        Retrofit retrofit = RetrofitClient.get(ctx.getApplicationContext());
        this.api = retrofit.create(NotificationApi.class);
    }

    // ======================
    // 목록/카운트 조회
    // ======================

    public void getNotifications(@Nullable Long cursor, @Nullable Integer size,
                                 Callback<ListResponse> cb) {
        api.getNotifications(cursor, size).enqueue(cb);
    }

    public void getUnreadCount(Callback<Integer> cb) {
        api.getUnreadCount().enqueue(cb);
    }

    // ======================
    // 읽음 처리
    // ======================

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

    /** 기존 호출부 호환용(에러 해결): 내부적으로 markRead로 위임 */
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

    // 클래스 안 어딘가에 추가
    public void markAllRead(retrofit2.Callback<Void> cb) {
        api.markAllRead().enqueue(cb);
    }

}
