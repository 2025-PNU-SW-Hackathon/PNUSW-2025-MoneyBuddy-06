package com.moneybuddy.moneylog.common;

import com.moneybuddy.moneylog.signup.dto.UserSignupRequest;
import com.moneybuddy.moneylog.signup.dto.UserSignupResponse;

import com.moneybuddy.moneylog.finance.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.finance.dto.response.KnowledgeResponse;
import com.moneybuddy.moneylog.finance.dto.response.QuizResponse;
import com.moneybuddy.moneylog.finance.dto.response.QuizResultResponse;
import com.moneybuddy.moneylog.finance.dto.response.YouthPolicyResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * 공통 API 세트 (회원가입/퀴즈/카드뉴스/정책)
 * Authorization 헤더는 OkHttp 인터셉터에서 자동 부착됩니다.
 */
public interface ApiService {

    // 카드뉴스 (오늘의 카드뉴스 목록)
    @GET("api/v1/knowledge/cardnews")
    Call<List<KnowledgeResponse>> getTodayCardNews();

    // 오늘의 퀴즈 조회
    @GET("api/v1/quiz/today")
    Call<QuizResponse> getTodayQuiz();

    // 퀴즈 정답 제출
    @POST("api/v1/quiz/answer")
    Call<QuizResultResponse> submitAnswer(@Body QuizAnswerRequest request);

    // 회원가입
    @POST("api/v1/users/signup")
    Call<UserSignupResponse> signup(@Body UserSignupRequest request);

    // 청년 정책 전체 조회
    @GET("api/v1/youth-policy")
    Call<List<YouthPolicyResponse>> getAllYouthPolicies();
}
