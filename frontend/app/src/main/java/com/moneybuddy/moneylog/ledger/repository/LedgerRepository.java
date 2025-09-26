package com.moneybuddy.moneylog.ledger.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.moneybuddy.moneylog.common.ResultCallback;
import com.moneybuddy.moneylog.common.RetrofitClient; // 공용 클라이언트(토큰/로깅/baseUrl) 사용
import com.moneybuddy.moneylog.ledger.dto.request.LedgerCreateRequest;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerDayResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;
import com.moneybuddy.moneylog.ledger.network.LedgerApi;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LedgerRepository {

    private final LedgerApi api;

    public LedgerRepository(Context ctx, @Nullable String token) {
        // RetrofitClient 내부에서 Authorization 헤더/로깅/baseUrl 을 일괄 설정한다고 가정
        this.api = RetrofitClient.getService(ctx, LedgerApi.class);
    }

    // ───────────── 조회 ─────────────
    public Call<LedgerDayResponse> getDay(String yyyyMMdd) {
        return api.getDay(yyyyMMdd);
    }

    /** ym: "yyyy-MM" 예) "2025-08" */
    public Call<LedgerMonthResponse> getMonth(String ym) {
        if (ym == null || !ym.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new IllegalArgumentException("yyyy-MM 형식이어야 합니다: " + ym);
        }
        return api.getMonth(ym);
    }

    // 서버가 year/month로 받는다면 이 오버로드 사용:
    // public Call<LedgerMonthResponse> getMonth(int year, int month) {
    //     return api.getMonth(year, month);
    // }

    // ───────────── 생성/수정/삭제 ─────────────
    public void create(LedgerCreateRequest body, ResultCallback<Long> cb) {
        if (cb == null) throw new IllegalArgumentException("cb is null");
        api.create(body).enqueue(new Callback<Long>() {
            @Override public void onResponse(Call<Long> call, Response<Long> resp) {
                // ✅ 200~299(201 포함) 모두 성공
                if (resp.isSuccessful()) {
                    cb.onSuccess(resp.body()); // 서버가 body를 비우면 null 전달
                } else {
                    cb.onError(new IOException(httpErrorString(resp)));
                }
            }
            @Override public void onFailure(Call<Long> call, Throwable t) {
                cb.onError(t);
            }
        });
    }

    public void update(long id, LedgerCreateRequest body, ResultCallback<Void> cb) {
        if (cb == null) throw new IllegalArgumentException("cb is null");
        api.update(id, body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(new IOException(httpErrorString(resp)));
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                cb.onError(t);
            }
        });
    }

    public void delete(long id, ResultCallback<Void> cb) {
        if (cb == null) throw new IllegalArgumentException("cb is null");
        api.delete(id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(new IOException(httpErrorString(resp)));
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                cb.onError(t);
            }
        });
    }

    // 직접 enqueue 하고 싶을 때 Call 반환 버전도 유지
    public Call<Long> create(LedgerCreateRequest body) { return api.create(body); }
    public Call<Void> update(long id, LedgerCreateRequest body) { return api.update(id, body); }
    public Call<Void> delete(long id) { return api.delete(id); }

    // ───────────── 유틸: 에러 바디까지 메시지로 합침 ─────────────
    private static String httpErrorString(Response<?> resp) {
        String code = "HTTP " + resp.code();
        try {
            ResponseBody eb = resp.errorBody();
            if (eb != null) {
                String body = eb.string();
                if (!TextUtils.isEmpty(body)) {
                    return code + " - " + body;
                }
            }
        } catch (Exception ignored) {}
        return code;
    }
}
