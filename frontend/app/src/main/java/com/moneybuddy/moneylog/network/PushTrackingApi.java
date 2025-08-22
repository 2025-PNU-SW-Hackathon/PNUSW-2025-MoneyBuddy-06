package com.moneybuddy.moneylog.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PushTrackingApi {

    // FCM 토큰 등록
    @POST("/api/devices/push-token")
    Call<Void> registerToken(@Body PushTokenBody body);

    // 도착 기록
    @POST("/api/notifications/{id}/delivered")
    Call<Void> trackDelivered(@Path("id") long id, @Body PushEventBody body);

    // 열림 기록
    @POST("/api/notifications/{id}/opened")
    Call<Void> trackOpened(@Path("id") long id, @Body PushEventBody body);

    class PushTokenBody {
        public String token;
        public String platform = "android";
        public String appVersion;
        public String deviceModel;
        public PushTokenBody(String t, String v, String m) { token=t; appVersion=v; deviceModel=m; }
    }

    class PushEventBody {
        public long occurredAt;          // epoch millis
        public Map<String, String> payload; // 원본 data 그대로
        public PushEventBody(long ts, Map<String,String> p){ occurredAt=ts; payload=p; }
    }
}
