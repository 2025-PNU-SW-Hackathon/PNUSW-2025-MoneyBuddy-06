package com.moneybuddy.moneylog.ledger.network;

import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AnalyticsApi {
    // 베이스 URL 뒤에 붙는 상대 경로라면 앞의 슬래시를 빼는 것을 권장합니다.
    @GET("analytics/category-ratio")
    Call<CategoryRatioResponse> categoryRatio(@Query("ym") String yearMonth);
}
