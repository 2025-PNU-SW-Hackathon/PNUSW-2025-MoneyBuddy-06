package com.moneybuddy.moneylog.network;

import android.content.Context;

import com.moneybuddy.moneylog.model.Notice;

import java.util.List;

import retrofit2.Callback;

public class NotificationRepository {

    protected NotificationApi api;

    public NotificationRepository(Context ctx) {
        this.api = RetrofitProvider.get(ctx).create(NotificationApi.class);
    }

    protected NotificationRepository() {}

    public void fetchList(Long cursor, int size, Callback<List<Notice>> cb) {
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
