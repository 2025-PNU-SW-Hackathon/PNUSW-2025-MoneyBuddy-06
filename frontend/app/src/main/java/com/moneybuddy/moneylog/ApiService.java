package com.moneybuddy.moneylog;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
public interface ApiService {
    @POST("api/users/save")
    Call<Void> sendMobtiResult(@Header("Authorization") String token, @Body MobtiResultData data);
}
