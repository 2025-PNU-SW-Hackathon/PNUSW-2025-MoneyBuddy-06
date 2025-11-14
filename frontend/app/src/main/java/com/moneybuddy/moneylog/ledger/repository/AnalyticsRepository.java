package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;

import com.moneybuddy.moneylog.common.ResultCallback;
import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.network.AnalyticsApi;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsRepository {

    private static volatile AnalyticsRepository INSTANCE;
    private final AnalyticsApi api;

    // ✅ 싱글턴 메서드 (getInstance)
    public static AnalyticsRepository getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AnalyticsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AnalyticsRepository(ctx.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ✅ 생성자
    public AnalyticsRepository(Context ctx) {
        // RetrofitClient 내부에서 토큰/베이스URL/Interceptor 처리됨
        this.api = RetrofitClient.getService(ctx, AnalyticsApi.class);
    }

    /** 호환용: 기존 (Context, token) 시그니처 유지. 전달된 token은 사용하지 않습니다. */
    @Deprecated
    public AnalyticsRepository(Context ctx, String token) {
        this(ctx);
    }

    // ✅ 실제 API 호출
    public void getCategoryRatio(String ym, ResultCallback<CategoryRatioResponse> cb) {
        api.categoryRatio(ym).enqueue(new Callback<CategoryRatioResponse>() {
            @Override
            public void onResponse(Call<CategoryRatioResponse> call, Response<CategoryRatioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    cb.onError(new IOException(httpErrorString(response)));
                }
            }

            @Override
            public void onFailure(Call<CategoryRatioResponse> call, Throwable t) {
                cb.onError(t);
            }
        });
    }

    // ✅ 에러 문자열 추출 (로그용)
    private static String httpErrorString(Response<?> resp) {
        String code = "HTTP " + resp.code();
        try {
            ResponseBody eb = resp.errorBody();
            if (eb != null) {
                String body = eb.string();
                if (body != null && !body.isEmpty()) {
                    return code + " - " + body;
                }
            }
        } catch (Exception ignored) {}
        return code;
    }
}
