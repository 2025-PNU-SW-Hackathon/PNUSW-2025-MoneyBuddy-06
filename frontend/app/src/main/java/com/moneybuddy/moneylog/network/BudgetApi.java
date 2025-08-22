package com.moneybuddy.moneylog.network;

import com.moneybuddy.moneylog.dto.budget.BudgetGoalDto;

import retrofit2.Call;
import retrofit2.http.*;

public interface BudgetApi {
    @PUT("/budget-goal")
    Call<BudgetGoalDto> upsert(@Query("ym") String yearMonth, @Body BudgetGoalDto body);

    @GET("/budget-goal")
    Call<BudgetGoalDto> get(@Query("ym") String yearMonth);
}
