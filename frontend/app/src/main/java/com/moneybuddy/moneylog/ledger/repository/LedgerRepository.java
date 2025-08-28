package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.moneybuddy.moneylog.common.ResultCallback;
import com.moneybuddy.moneylog.common.RetrofitClient; // ✅ 공용 클라이언트 사용
import com.moneybuddy.moneylog.ledger.dto.request.LedgerCreateRequest;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerDayResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;
import com.moneybuddy.moneylog.ledger.network.LedgerApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LedgerRepository {

    private final LedgerApi api;

    public LedgerRepository(Context ctx, @Nullable String token) {
        // ✅ 토큰/BASE_URL/로깅/cleartext 설정을 공용 RetrofitClient에서 통일
        this.api = RetrofitClient.getService(ctx, LedgerApi.class);
    }

    // ───────────── 조회 ─────────────
    public Call<LedgerDayResponse> getDay(String yyyyMMdd) {
        return api.getDay(yyyyMMdd); // GET ledger/day?date=2025-08-28
    }

    /** ym: "yyyy-MM" (예: "2025-08") */
    public Call<LedgerMonthResponse> getMonth(String ym) {
        if (ym == null || !ym.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new IllegalArgumentException("yyyy-MM 형식이어야 합니다: " + ym);
        }
        // 서버가 year/month 쿼리라면 아래 한 줄 대신 getMonth(year, month) 호출로 바꾸세요.
        return api.getMonth(ym); // GET ledger/month?ym=2025-08
    }

    // 서버가 year/month로 받는다면 이 오버로드 사용:
    // public Call<LedgerMonthResponse> getMonth(int year, int month) {
    //     return api.getMonth(year, month); // GET ledger/month?year=2025&month=8
    // }

    // ───────────── 저장/수정 ─────────────
    public void create(LedgerCreateRequest body, ResultCallback<Long> cb) {
        if (cb == null) throw new IllegalArgumentException("cb is null");
        api.create(body).enqueue(new Callback<Long>() {
            @Override public void onResponse(Call<Long> call, Response<Long> resp) {
                if (resp.isSuccessful()) cb.onSuccess(resp.body());
                else cb.onError(new IOException(httpErrorString(resp)));
            }
            @Override public void onFailure(Call<Long> call, Throwable t) { cb.onError(t); }
        });
    }

    public void update(long id, LedgerCreateRequest body, ResultCallback<Void> cb) {
        if (cb == null) throw new IllegalArgumentException("cb is null");
        api.update(id, body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) cb.onSuccess(null);
                else cb.onError(new IOException(httpErrorString(resp)));
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { cb.onError(t); }
        });
    }

    // 직접 enqueue 하고 싶을 때 Call 반환 버전도 유지
    public Call<Long> create(LedgerCreateRequest body) { return api.create(body); }
    public Call<Void> update(long id, LedgerCreateRequest body) { return api.update(id, body); }

    private static String httpErrorString(Response<?> resp) {
        String code = "HTTP " + resp.code();
        try {
            var eb = resp.errorBody();
            if (eb != null) {
                String body = eb.string();
                if (!TextUtils.isEmpty(body)) return code + " - " + body;
            }
        } catch (Exception ignored) { }
        return code;
    }
}
