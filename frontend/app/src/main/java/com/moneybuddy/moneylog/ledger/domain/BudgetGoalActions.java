package com.moneybuddy.moneylog.ledger.domain;

import android.content.Context;

import com.moneybuddy.moneylog.ledger.dto.response.BudgetGoalDto;
import com.moneybuddy.moneylog.ledger.repository.BudgetRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetGoalActions {

    public interface OnGoal {
        void onLoaded(BudgetGoalDto dto);
        void onError(Throwable t);
    }

    public static void get(Context ctx, String token, String ym, OnGoal cb) {
        new BudgetRepository(ctx, token).getGoal(ym).enqueue(new Callback<BudgetGoalDto>() {
            @Override public void onResponse(Call<BudgetGoalDto> call, Response<BudgetGoalDto> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onLoaded(r.body());
                else cb.onError(new RuntimeException("getGoal fail: " + r.code()));
            }
            @Override public void onFailure(Call<BudgetGoalDto> call, Throwable t) { cb.onError(t); }
        });
    }

    public static void put(Context ctx, String token, String ym, long amount, OnGoal cb) {
        new BudgetRepository(ctx, token).putGoal(ym, Math.max(0, amount)).enqueue(new Callback<BudgetGoalDto>() {
            @Override public void onResponse(Call<BudgetGoalDto> call, Response<BudgetGoalDto> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onLoaded(r.body());
                else cb.onError(new RuntimeException("putGoal fail: " + r.code()));
            }
            @Override public void onFailure(Call<BudgetGoalDto> call, Throwable t) { cb.onError(t); }
        });
    }
}
