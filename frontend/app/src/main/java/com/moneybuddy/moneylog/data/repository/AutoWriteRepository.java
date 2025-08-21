package com.moneybuddy.moneylog.data.repository;

import android.content.Context;

import com.moneybuddy.moneylog.data.dto.auto.AutoLedgerRequest;
import com.moneybuddy.moneylog.data.dto.auto.AutoLedgerResponse;
import com.moneybuddy.moneylog.data.remote.LedgerApi;
import com.moneybuddy.moneylog.data.remote.RetrofitProvider;

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
