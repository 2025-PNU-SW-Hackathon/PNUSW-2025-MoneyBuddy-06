package com.moneybuddy.moneylog.network;

import com.moneybuddy.moneylog.dto.ChangePasswordRequest;
import com.moneybuddy.moneylog.dto.UserDeleteRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HTTP;
import retrofit2.http.PUT;

public interface ApiService {

    @HTTP(method = "DELETE", path = "api/v1/users/delete", hasBody = true)
    Call<Void> deleteUser(
            @Header("Authorization") String token,
            @Body UserDeleteRequest request
    );

    @PUT("api/v1/users/password")
    Call<Void> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );
}