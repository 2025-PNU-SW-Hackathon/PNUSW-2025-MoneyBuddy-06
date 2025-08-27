package com.moneybuddy.moneylog.ledger.network;

import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AnalyticsApi {
    @GET("/analytics/category-ratio")
    Call<CategoryRatioDto> categoryRatio(@Query("ym") String yearMonth);
}
