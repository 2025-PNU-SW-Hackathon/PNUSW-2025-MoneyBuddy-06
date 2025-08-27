package com.moneybuddy.moneylog.ledger.domain;

import android.content.Context;

import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;
import com.moneybuddy.moneylog.ledger.repository.LedgerRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonthLoadHelper {
    public interface OnMonthLoaded {
        void onLoaded(LedgerMonthResponse res);
        void onError(Throwable t);
    }

    public static void load(Context ctx, String token, String ym, OnMonthLoaded cb) {
        new LedgerRepository(ctx, token).getMonth(ym)
                .enqueue(new Callback<LedgerMonthResponse>() {
                    @Override public void onResponse(Call<LedgerMonthResponse> call, Response<LedgerMonthResponse> r) {
                        if (r.isSuccessful() && r.body() != null) cb.onLoaded(r.body());
                        else cb.onError(new RuntimeException("loadMonth fail: " + r.code()));
                    }
                    @Override public void onFailure(Call<LedgerMonthResponse> call, Throwable t) { cb.onError(t); }
                });
    }
}
