package com.moneybuddy.moneylog.network;

import com.moneybuddy.moneylog.dto.response.KnowledgeResponse;
import com.moneybuddy.moneylog.dto.response.YouthPolicyResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface FinanceApiService {
    // 카드뉴스 API
    @GET("api/v1/knowledge/cardnews")
    Call<List<KnowledgeResponse>> getTodayCardNews();

    // 청년 정책 목록 전체를 조회하는 API
    @GET("/api/v1/youth-policy")
    Call<List<YouthPolicyResponse>> getAllYouthPolicies();
}