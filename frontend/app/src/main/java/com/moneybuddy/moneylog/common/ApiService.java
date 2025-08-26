package com.moneybuddy.moneylog.common;

import com.moneybuddy.moneylog.signup.dto.UserSignupRequest;
import com.moneybuddy.moneylog.signup.dto.UserSignupResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // 회원가입 요청
    @POST("/api/v1/users/signup")
    Call<UserSignupResponse> signup(@Body UserSignupRequest request);

    // 로그인, 금융정보 조회 등 다른 API들도 여기에 넣기
}
