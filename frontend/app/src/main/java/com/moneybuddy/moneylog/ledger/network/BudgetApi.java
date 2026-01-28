package com.moneybuddy.moneylog.ledger.network;

import com.moneybuddy.moneylog.ledger.dto.response.BudgetGoalDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface BudgetApi {
    @PUT("budget-goal")
    Call<BudgetGoalDto> upsert(@Query("ym") String yearMonth, @Body BudgetGoalDto body);

    @GET("budget-goal")
    Call<BudgetGoalDto> get(@Query("ym") String yearMonth);
}
