package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;

import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.network.LedgerApi;

import retrofit2.Call;

public class AnalyticsRepository {

    private final LedgerApi api;

    /** 권장: 토큰은 Interceptor가 자동 첨부하므로 Context만 받습니다. */
    public AnalyticsRepository(Context ctx) {
        this.api = RetrofitClient.getService(ctx, LedgerApi.class);
    }

    /** 호환용: 기존 (Context, token) 시그니처 유지. 전달된 token은 사용하지 않습니다. */
    @Deprecated
    public AnalyticsRepository(Context ctx, String token) {
        this(ctx); // RetrofitClient + TokenInterceptor 사용
    }

    /** GET /analytics/category-ratio?ym=YYYY-MM */
    public Call<CategoryRatioResponse> getCategoryRatio(String ym) {
        return api.getCategoryRatio(ym);
    }
}
