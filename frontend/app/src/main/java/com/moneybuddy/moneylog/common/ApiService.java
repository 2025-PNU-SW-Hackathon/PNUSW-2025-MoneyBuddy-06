package com.moneybuddy.moneylog.common;

import com.moneybuddy.moneylog.finance.dto.response.KnowledgeResponse;
import com.moneybuddy.moneylog.finance.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.finance.dto.response.QuizResponse;
import com.moneybuddy.moneylog.finance.dto.response.QuizResultResponse;
import com.moneybuddy.moneylog.finance.dto.response.YouthPolicyResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.List;

public interface ApiService {
    // 카드뉴스 API
    @GET("api/v1/knowledge/cardnews")
    Call<List<KnowledgeResponse>> getTodayCardNews();

    // 오늘의 퀴즈 조회
    @GET("/api/v1/quiz/today")
    Call<QuizResponse> getTodayQuiz();

    // 퀴즈 정답 제출
    @POST("/api/v1/quiz/answer")
    Call<QuizResultResponse> submitAnswer(@Body QuizAnswerRequest request);

    // 청년 정책 목록 전체를 조회하는 API
    @GET("/api/v1/youth-policy")
    Call<List<YouthPolicyResponse>> getAllYouthPolicies();


}