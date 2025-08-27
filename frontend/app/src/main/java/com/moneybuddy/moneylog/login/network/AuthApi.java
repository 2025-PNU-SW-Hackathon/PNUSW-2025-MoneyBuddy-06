package com.moneybuddy.moneylog.login.network;
import com.moneybuddy.moneylog.login.dto.LoginRequest;
import com.moneybuddy.moneylog.login.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/v1/users/login")
    Call<LoginResponse> login(@Body LoginRequest body);
}
