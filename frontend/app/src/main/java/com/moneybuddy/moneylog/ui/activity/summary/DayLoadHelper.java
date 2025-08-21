package com.moneybuddy.moneylog.ui.activity.summary;

import android.content.Context;

import com.moneybuddy.moneylog.data.dto.ledger.LedgerDayResponse;
import com.moneybuddy.moneylog.data.repository.LedgerRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayLoadHelper {
    public interface OnDayLoaded {
        void onLoaded(LedgerDayResponse res);
        void onError(Throwable t);
    }

    public static void load(Context ctx, String token, String date, OnDayLoaded cb) {
        new LedgerRepository(ctx, token).getDay(date)
                .enqueue(new Callback<LedgerDayResponse>() {
                    @Override public void onResponse(Call<LedgerDayResponse> call, Response<LedgerDayResponse> r) {
                        if (r.isSuccessful() && r.body() != null) cb.onLoaded(r.body());
                        else cb.onError(new RuntimeException("loadDay fail: " + r.code()));
                    }
                    @Override public void onFailure(Call<LedgerDayResponse> call, Throwable t) { cb.onError(t); }
                });
    }
}
