package com.moneybuddy.moneylog.data.repository;

import android.content.Context;

import com.moneybuddy.moneylog.data.dto.analytics.CategoryRatioResponse;
import com.moneybuddy.moneylog.data.remote.LedgerApi;
import com.moneybuddy.moneylog.data.remote.RetrofitProvider;

import retrofit2.Call;

public class AnalyticsRepository {
    private final LedgerApi api;

    public AnalyticsRepository(Context ctx, String token) {
        api = RetrofitProvider.get(ctx, token).create(LedgerApi.class);
    }

    public Call<CategoryRatioResponse> getCategoryRatio(String ym) {
        return api.getCategoryRatio(ym);
    }
}
