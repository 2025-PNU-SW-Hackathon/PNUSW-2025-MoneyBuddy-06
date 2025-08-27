package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;

import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.ledger.dto.request.AutoLedgerRequest;
import com.moneybuddy.moneylog.ledger.dto.response.AutoLedgerResponse;
import com.moneybuddy.moneylog.ledger.network.LedgerApi;

import retrofit2.Call;

public class AutoWriteRepository {
    private final LedgerApi api;

    public AutoWriteRepository(Context ctx, String token) {
        api = RetrofitClient.getService(ctx, LedgerApi.class);
    }

    public Call<AutoLedgerResponse> sendMessage(String message, String receivedAtIso) {
        AutoLedgerRequest req = new AutoLedgerRequest(message, receivedAtIso);
        return api.postAuto(req);
    }
}
