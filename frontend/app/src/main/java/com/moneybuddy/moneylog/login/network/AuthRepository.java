package com.moneybuddy.moneylog.login.network;

import android.content.Context;

import com.moneybuddy.moneylog.login.dto.LoginRequest;
import com.moneybuddy.moneylog.login.dto.LoginResponse;
import com.moneybuddy.moneylog.util.ErrorParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    public interface LoginCallback {
        void onSuccess(LoginResponse data);
        void onError(String message);
    }

    public void login(Context ctx, String email, String password, LoginCallback cb) {

        com.moneybuddy.moneylog.login.network.AuthApi api =
                com.moneybuddy.moneylog.common.RetrofitClient.getService(ctx, com.moneybuddy.moneylog.login.network.AuthApi.class);

        api.login(new LoginRequest(email, password)).enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override public void onResponse(retrofit2.Call<LoginResponse> c, retrofit2.Response<LoginResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    cb.onSuccess(r.body());
                } else {
                    cb.onError(ErrorParser.message(r));
                }
            }
            @Override public void onFailure(retrofit2.Call<LoginResponse> c, Throwable t) {
                cb.onError("서버에 연결할 수 없습니다.");
            }
        });
    }
}