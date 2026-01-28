package com.moneybuddy.moneylog.common;

import com.moneybuddy.moneylog.signup.dto.UserSignupRequest;
import com.moneybuddy.moneylog.signup.dto.UserSignupResponse;

import com.moneybuddy.moneylog.finance.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.finance.dto.response.KnowledgeResponse;
import com.moneybuddy.moneylog.finance.dto.response.QuizResponse;
import com.moneybuddy.moneylog.finance.dto.response.QuizResultResponse;
import com.moneybuddy.moneylog.finance.dto.response.YouthPolicyResponse;

import java.util.List;
import com.moneybuddy.moneylog.mypage.dto.ChangePasswordRequest;
import com.moneybuddy.moneylog.mypage.dto.UserDeleteRequest;
import com.moneybuddy.moneylog.mypage.dto.MobtiBriefDto;
import com.moneybuddy.moneylog.mypage.dto.UserExpResponse;
import com.moneybuddy.moneylog.mypage.dto.PushSettingRequest;
import com.moneybuddy.moneylog.mypage.dto.PushSettingResponse;


import com.moneybuddy.moneylog.mobti.dto.response.MobtiFullDto;
import com.moneybuddy.moneylog.mypage.dto.MobtiBriefDto;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HTTP;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

/**
 * 공통 API 세트 (회원가입/퀴즈/카드뉴스/정책)
 * Authorization 헤더는 OkHttp 인터셉터에서 자동 부착됩니다.
 */
public interface ApiService {
    // 내 MoBTI 요약 정보 조회
    @GET("api/v1/mobti/me/summary")
    Call<MobtiBriefDto> getMyMobtiSummary();

    //MOBTI 상세
    @GET("api/v1/mobti/me/details")
    Call<MobtiFullDto> getMyMobtiDetails();

    // 카드뉴스 (오늘의 카드뉴스 목록)
    @GET("api/v1/knowledge/cardnews")
    Call<List<KnowledgeResponse>> getTodayCardNews();
    // 나의 레벨 및 경험치 조회
    @GET("api/v1/challenges/exp")
    Call<UserExpResponse> getMyExp();

    // 오늘의 퀴즈 조회
    @GET("api/v1/quiz/today")
    Call<QuizResponse> getTodayQuiz();

    // 비밀번호 변경
    @PATCH("api/v1/users/password")
    Call<Void> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );

    // 퀴즈 정답 제출
    @POST("api/v1/quiz/answer")
    Call<QuizResultResponse> submitAnswer(@Body QuizAnswerRequest request);
    // 현재 알림 설정 상태를 서버에서 가져옴
    @GET("api/v1/users/notifications/push")
    Call<PushSettingResponse> getPushSetting(@Header("Authorization") String token);

    // 회원가입
    @POST("api/v1/users/signup")
    Call<UserSignupResponse> signup(@Body UserSignupRequest request);
    // 변경된 알림 설정 상태를 서버에 전송
    @PATCH("api/v1/users/notifications/push")
    Call<Void> updatePushSetting(@Header("Authorization") String token, @Body PushSettingRequest request);

    // 청년 정책 전체 조회
    @GET("api/v1/youth-policy")
    Call<List<YouthPolicyResponse>> getAllYouthPolicies();

    // 로그아웃 (서버 토큰 무효화)
    @POST("api/v1/auth/logout")
    Call<Void> logout(@Header("Authorization") String token);

    // 회원 탈퇴
    @HTTP(method = "DELETE", path = "api/v1/users/delete", hasBody = true)
    Call<Void> deleteUser(
            @Header("Authorization") String token,
            @Body UserDeleteRequest request
    );
}