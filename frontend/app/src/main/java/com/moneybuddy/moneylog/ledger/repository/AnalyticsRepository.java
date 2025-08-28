package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;

import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.network.AnalyticsApi;

import retrofit2.Call;

public class AnalyticsRepository {

    private final AnalyticsApi api;

    public AnalyticsRepository(Context ctx) {
        // RetrofitClient 가 토큰/베이스URL/Interceptor 를 내부에서 처리한다고 가정
        this.api = RetrofitClient.getService(ctx, AnalyticsApi.class);
    }

    /** 호환용: 기존 (Context, token) 시그니처 유지. 전달된 token은 사용하지 않습니다. */
    @Deprecated
    public AnalyticsRepository(Context ctx, String token) {
        this(ctx);
    }

    /** GET /analytics/category-ratio?ym=YYYY-MM */
    public Call<CategoryRatioResponse> getCategoryRatio(String ym) {
        return api.categoryRatio(ym);
    }
}
