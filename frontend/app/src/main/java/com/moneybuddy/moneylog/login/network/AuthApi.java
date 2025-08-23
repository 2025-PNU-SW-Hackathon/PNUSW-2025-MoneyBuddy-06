package com.moneybuddy.moneylog.login.network;
import com.moneybuddy.moneylog.login.dto.LoginRequest;
import com.moneybuddy.moneylog.login.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    // 🔧 이 한 줄만 서버 실제 경로로 바꾸면 됩니다.
    // 예) "/api/auth/login" 또는 "/login" 등
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);
}
