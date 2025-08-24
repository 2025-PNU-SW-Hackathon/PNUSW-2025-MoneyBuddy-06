package com.moneybuddy.moneylog.common;

import com.moneybuddy.moneylog.mypage.dto.ChangePasswordRequest;
import com.moneybuddy.moneylog.mypage.dto.UserDeleteRequest;
import com.moneybuddy.moneylog.mypage.dto.MobtiBriefDto;
import com.moneybuddy.moneylog.mypage.dto.UserExpResponse;
import com.moneybuddy.moneylog.mypage.dto.PushSettingRequest;
import com.moneybuddy.moneylog.mypage.dto.PushSettingResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HTTP;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface ApiService {
    // 내 MoBTI 요약 정보 조회
    @GET("mobti/me/summary")
    Call<MobtiBriefDto> getMyMobtiSummary();

    // 나의 레벨 및 경험치 조회
    @GET("challenges/exp")
    Call<UserExpResponse> getMyExp();

    // 비밀번호 변경
    @PATCH("users/password")
    Call<Void> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );

    // 현재 알림 설정 상태를 서버에서 가져옴
    @GET("users/notifications/push")
    Call<PushSettingResponse> getPushSetting(@Header("Authorization") String token);

    // 변경된 알림 설정 상태를 서버에 전송
    @PATCH("users/notifications/push")
    Call<Void> updatePushSetting(@Header("Authorization") String token, @Body PushSettingRequest request);

    // 로그아웃 (서버 토큰 무효화)
    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String token);

    // 회원 탈퇴
    @HTTP(method = "DELETE", path = "users/delete", hasBody = true)
    Call<Void> deleteUser(
            @Header("Authorization") String token,
            @Body UserDeleteRequest request
    );
}