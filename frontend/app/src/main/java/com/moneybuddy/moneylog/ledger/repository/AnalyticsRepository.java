package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;

import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.network.LedgerApi;
import com.moneybuddy.moneylog.common.RetrofitProvider;

import retrofit2.Call;

public class AnalyticsRepository {
    private final LedgerApi api;

    public AnalyticsRepository(Context ctx, String token) {
        api = RetrofitProvider.get(ctx).create(LedgerApi.class);
    }

    public Call<CategoryRatioResponse> getCategoryRatio(String ym) {
        return api.getCategoryRatio(ym);
    }
}
