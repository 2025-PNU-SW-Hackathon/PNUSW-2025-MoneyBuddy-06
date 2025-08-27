package com.moneybuddy.moneylog.ledger.loader;

import android.content.Context;

import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.repository.AnalyticsRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRatioLoader {

    public interface OnLoaded {
        void onLoaded(CategoryRatioResponse res);
        void onError(Throwable t);
    }

    public static void load(Context ctx, String ym, OnLoaded cb) {
        new AnalyticsRepository(ctx).getCategoryRatio(ym)
                .enqueue(new Callback<CategoryRatioResponse>() {
                    @Override public void onResponse(Call<CategoryRatioResponse> call, Response<CategoryRatioResponse> r) {
                        if (r.isSuccessful() && r.body() != null) cb.onLoaded(r.body());
                        else cb.onError(new RuntimeException("category-ratio fail: " + r.code()));
                    }
                    @Override public void onFailure(Call<CategoryRatioResponse> call, Throwable t) { cb.onError(t); }
                });
    }

    // UI 퍼센트 계산: spent / (goal or spent)
    public static double progressPercent(CategoryRatioResponse dto) {
        double denom = (dto.baseline != null && dto.baseline.equals("GOAL") && dto.goalAmount != null && dto.goalAmount > 0)
                ? dto.goalAmount
                : Math.max(1, dto.spent);
        return Math.min(1.0, dto.spent / denom);
    }
}
