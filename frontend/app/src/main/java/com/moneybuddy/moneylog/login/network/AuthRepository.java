package com.moneybuddy.moneylog.login.network;

import android.content.Context;

import com.moneybuddy.moneylog.common.ApiService;
import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.login.dto.LoginRequest;
import com.moneybuddy.moneylog.login.dto.LoginResponse;
import com.moneybuddy.moneylog.mypage.dto.MobtiBriefDto;
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
        AuthApi api = RetrofitClient.getService(ctx, AuthApi.class);

        api.login(new LoginRequest(email, password)).enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> c, Response<LoginResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    cb.onSuccess(r.body());
                } else {
                    cb.onError(ErrorParser.message(r));
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> c, Throwable t) {
                cb.onError("서버에 연결할 수 없습니다.");
            }
        });
    }

    public void checkMobtiStatus(Context context, MobtiCheckCallback callback) {
        ApiService apiService = RetrofitClient.api(context);

        apiService.getMyMobtiSummary().enqueue(new Callback<MobtiBriefDto>() {
            @Override
            public void onResponse(Call<MobtiBriefDto> call, Response<MobtiBriefDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MobtiBriefDto briefDto = response.body();
                    if ("UNDEFINED".equals(briefDto.getCode())) {
                        callback.onMobtiNotExists();
                    } else {
                        callback.onMobtiExists();
                    }
                } else {
                    callback.onError(ErrorParser.message(response));
                }
            }

            @Override
            public void onFailure(Call<MobtiBriefDto> call, Throwable t) {
                callback.onError("네트워크 연결을 확인해주세요.");
            }
        });
    }
}