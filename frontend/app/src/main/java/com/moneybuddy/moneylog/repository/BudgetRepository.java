package com.moneybuddy.moneylog.repository;

import android.content.Context;

import com.moneybuddy.moneylog.dto.budget.BudgetGoalDto;
import com.moneybuddy.moneylog.network.LedgerApi;
import com.moneybuddy.moneylog.network.RetrofitProvider;

import retrofit2.Call;

public class BudgetRepository {
    private final LedgerApi api;

    public BudgetRepository(Context ctx, String token) {
        api = RetrofitProvider.get(ctx, token).create(LedgerApi.class);
    }

    public Call<BudgetGoalDto> getGoal(String ym) {
        return api.getBudgetGoal(ym);
    }

    public Call<BudgetGoalDto> putGoal(String ym, long amount) {
        BudgetGoalDto body = new BudgetGoalDto();
        body.yearMonth = ym;
        body.amount = Math.max(0, amount); // 항상 0 이상
        return api.putBudgetGoal(ym, body);
    }
}
