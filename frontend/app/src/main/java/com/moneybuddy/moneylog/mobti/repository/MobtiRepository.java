package com.moneybuddy.moneylog.mobti.repository;

import android.content.Context;

import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.mobti.api.MobtiApi;
import com.moneybuddy.moneylog.mobti.dto.request.MobtiSubmitRequest;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiFullDto;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiResultDto;

import retrofit2.Call;

public class MobtiRepository {

    private final MobtiApi api;

    public MobtiRepository(Context ctx) {
        this.api = RetrofitClient.get(ctx).create(MobtiApi.class);
    }

    public Call<MobtiResultDto> submit(MobtiSubmitRequest body) { return api.submit(body); }
    public Call<MobtiBriefDto> mySummary() { return api.mySummary(); }
    public Call<MobtiFullDto> myDetails() { return api.myDetails(); }
}
