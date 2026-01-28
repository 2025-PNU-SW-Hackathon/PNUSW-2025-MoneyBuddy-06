package com.moneybuddy.moneylog.mobti.api;

import com.google.gson.JsonObject;
import com.moneybuddy.moneylog.mobti.dto.request.MobtiSubmitRequest;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiFullDto;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiResultDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MobtiApi {

    @POST("api/v1/mobti/submit")
    Call<MobtiResultDto> submit(@Body MobtiSubmitRequest body);

    @GET("api/v1/mobti/me/summary")
    Call<MobtiBriefDto> mySummary();

    @GET("api/v1/mobti/me/details")
    Call<MobtiFullDto> myDetails();

    @GET("api/mobti/me")
    Call<JsonObject> getMobti();
}
