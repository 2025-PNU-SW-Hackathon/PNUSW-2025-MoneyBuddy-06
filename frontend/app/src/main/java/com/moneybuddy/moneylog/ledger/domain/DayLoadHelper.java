package com.moneybuddy.moneylog.ledger.domain;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.moneybuddy.moneylog.ledger.dto.response.LedgerDayResponse;
import com.moneybuddy.moneylog.ledger.repository.LedgerRepository;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayLoadHelper {

    public interface OnDayLoaded {
        void onLoaded(LedgerDayResponse res);
        void onError(Throwable t);
    }

    // 메인 스레드 콜백 보장
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static void postLoaded(@NonNull OnDayLoaded cb, @NonNull LedgerDayResponse d) {
        if (Looper.myLooper() == Looper.getMainLooper()) cb.onLoaded(d);
        else MAIN.post(() -> cb.onLoaded(d));
    }
    private static void postError(@NonNull OnDayLoaded cb, @NonNull Throwable t) {
        if (Looper.myLooper() == Looper.getMainLooper()) cb.onError(t);
        else MAIN.post(() -> cb.onError(t));
    }
    private static String httpErrorString(Response<?> r) {
        String code = "HTTP " + r.code();
        try {
            if (r.errorBody() != null) {
                String body = r.errorBody().string();
                if (!TextUtils.isEmpty(body)) return code + " - " + body;
            }
        } catch (Exception ignored) {}
        return code;
    }

    public static void load(Context ctx, String token, String date, OnDayLoaded cb) {
        if (cb == null) return;

        if (TextUtils.isEmpty(date)) {
            postError(cb, new IllegalArgumentException("date is empty"));
            return;
        }
        if (TextUtils.isEmpty(token)) {
            postError(cb, new IllegalStateException("missing token"));
            return;
        }

        LedgerRepository repo = new LedgerRepository(ctx, token);
        Call<LedgerDayResponse> call = repo.getDay(date);

        call.enqueue(new Callback<LedgerDayResponse>() {
            @Override public void onResponse(Call<LedgerDayResponse> call, Response<LedgerDayResponse> r) {
                if (!r.isSuccessful()) {
                    postError(cb, new IOException(httpErrorString(r)));
                    return;
                }
                LedgerDayResponse body = r.body();
                if (body == null) {
                    postError(cb, new NullPointerException("empty body"));
                    return;
                }
                postLoaded(cb, body);
            }

            @Override public void onFailure(Call<LedgerDayResponse> call, Throwable t) {
                if (call.isCanceled()) return; // 취소된 호출은 무시
                postError(cb, t);
            }
        });
    }
}
