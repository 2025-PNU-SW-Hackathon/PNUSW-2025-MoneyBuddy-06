package com.moneybuddy.moneylog.notification.network;

import com.moneybuddy.moneylog.notification.model.ListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {

    // ✅ items + nextCursor 래핑
    @GET("/api/notifications")
    Call<ListResponse> getNotifications(
            @Query("cursor") Long cursor,
            @Query("size") Integer size
    );

    @GET("/api/notifications/unread-count")
    Call<Integer> getUnreadCount();

    @PATCH("/api/notifications/{id}/read")
    Call<Void> markRead(@Path("id") long id);

    @POST("/api/notifications/mark-all-read")
    Call<Void> markAllRead();
}
