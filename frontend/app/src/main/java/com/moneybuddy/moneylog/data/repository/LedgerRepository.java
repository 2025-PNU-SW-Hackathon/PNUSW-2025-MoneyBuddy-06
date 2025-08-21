package com.moneybuddy.moneylog.data.repository;

import android.content.Context;

import com.moneybuddy.moneylog.data.dto.ledger.LedgerCreateRequest;
import com.moneybuddy.moneylog.data.dto.ledger.LedgerDayResponse;
import com.moneybuddy.moneylog.data.dto.ledger.LedgerMonthResponse;
import com.moneybuddy.moneylog.data.remote.LedgerApi;
import com.moneybuddy.moneylog.data.remote.RetrofitProvider;

import retrofit2.Call;

public class LedgerRepository {
    private final LedgerApi api;

    public LedgerRepository(Context ctx, String token) {
        api = RetrofitProvider.get(ctx, token).create(LedgerApi.class);
    }

    public Call<LedgerApi.WrappedEntry> create(LedgerCreateRequest req) {
        // amount는 항상 양수 전송 (DTO에서 보정)
        return api.create(req);
    }

    public Call<LedgerApi.WrappedEntry> update(long id, LedgerCreateRequest req) {
        req.amount = Math.abs(req.amount);
        return api.update(id, req);
    }

    public Call<Void> delete(long id) {
        return api.delete(id); // 캐스팅 필요 없음
    }


    public Call<LedgerMonthResponse> getMonth(String ym) {
        return api.getMonth(ym);
    }

    public Call<LedgerDayResponse> getDay(String date) {
        return api.getDay(date);
    }
}
