package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;

import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.ledger.dto.response.BudgetGoalDto;
import com.moneybuddy.moneylog.ledger.network.BudgetApi;

import retrofit2.Call;

public class BudgetRepository {
    private final BudgetApi api;

    public BudgetRepository(Context ctx, String token) {
        // RetrofitClient 가 토큰/베이스URL을 내부에서 처리한다고 가정
        this.api = RetrofitClient.getService(ctx, BudgetApi.class);
    }

    public Call<BudgetGoalDto> getGoal(String ym) {
        return api.get(ym);
    }

    public Call<BudgetGoalDto> putGoal(String ym, long amount) {
        BudgetGoalDto body = new BudgetGoalDto();
        body.yearMonth = ym;                 // "yyyy-MM"
        body.amount = Math.max(0, amount);   // 0 이상으로 보정
        return api.upsert(ym, body);
    }
}
