package com.moneybuddy.moneylog.repository;

import android.content.Context;

import com.moneybuddy.moneylog.dto.auto.AutoLedgerRequest;
import com.moneybuddy.moneylog.dto.auto.AutoLedgerResponse;
import com.moneybuddy.moneylog.network.LedgerApi;
import com.moneybuddy.moneylog.network.RetrofitProvider;

import retrofit2.Call;

public class AutoWriteRepository {
    private final LedgerApi api;

    public AutoWriteRepository(Context ctx, String token) {
        api = RetrofitProvider.get(ctx, token).create(LedgerApi.class);
    }

    public Call<AutoLedgerResponse> sendMessage(String message, String receivedAtIso) {
        AutoLedgerRequest req = new AutoLedgerRequest(message, receivedAtIso);
        return api.postAuto(req);
    }
}
