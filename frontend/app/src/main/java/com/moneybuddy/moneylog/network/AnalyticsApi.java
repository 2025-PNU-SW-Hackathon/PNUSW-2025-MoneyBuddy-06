package com.moneybuddy.moneylog.network;

import com.moneybuddy.moneylog.dto.analytics.CategoryRatioDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AnalyticsApi {
    @GET("/analytics/category-ratio")
    Call<CategoryRatioDto> categoryRatio(@Query("ym") String yearMonth);
}
