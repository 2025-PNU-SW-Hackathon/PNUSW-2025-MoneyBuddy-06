package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;

import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.ledger.dto.request.LedgerCreateRequest;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerDayResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;
import com.moneybuddy.moneylog.ledger.network.LedgerApi;

import retrofit2.Call;

public class LedgerRepository {
    private final LedgerApi api;

    public LedgerRepository(Context ctx, String token) {
        api = RetrofitClient.getService(ctx, LedgerApi.class);
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
