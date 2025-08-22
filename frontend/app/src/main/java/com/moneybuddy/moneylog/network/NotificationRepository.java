package com.moneybuddy.moneylog.network;

import android.content.Context;

import com.moneybuddy.moneylog.model.ListResponse;

import retrofit2.Callback;

public class NotificationRepository {

    protected NotificationApi api;

    public NotificationRepository(Context ctx) {
        this.api = RetrofitProvider.get(ctx).create(NotificationApi.class);
    }

    protected NotificationRepository() {}

    // ✅ 래핑 DTO 기준으로 콜백 타입 변경
    public void fetchList(Long cursor, int size, Callback<ListResponse> cb) {
        api.getNotifications(cursor, size).enqueue(cb);
    }

    public void markRead(long id, Callback<Void> cb) {
        api.markRead(id).enqueue(cb);
    }

    public void markAllRead(Callback<Void> cb) {
        api.markAllRead().enqueue(cb);
    }

    public void unreadCount(Callback<Integer> cb) {
        api.getUnreadCount().enqueue(cb);
    }
}
