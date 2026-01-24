package com.moneybuddy.moneylog.ledger.domain;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;
import com.moneybuddy.moneylog.ledger.repository.LedgerRepository;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonthLoadHelper {

    public interface OnMonthLoaded {
        void onLoaded(LedgerMonthResponse res);
        void onError(Throwable t);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static void postLoaded(@NonNull OnMonthLoaded cb, @NonNull LedgerMonthResponse d) {
        if (Looper.myLooper() == Looper.getMainLooper()) cb.onLoaded(d);
        else MAIN.post(() -> cb.onLoaded(d));
    }
    private static void postError(@NonNull OnMonthLoaded cb, @NonNull Throwable t) {
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

    /** ym: "yyyy-MM" 형식 (예: "2025-08") */
    public static void load(@NonNull Context ctx,
                            @NonNull String token,
                            @NonNull String ym,
                            @NonNull OnMonthLoaded cb) {
        if (TextUtils.isEmpty(token)) {
            postError(cb, new IllegalStateException("missing token"));
            return;
        }
        if (!isValidYm(ym)) {
            postError(cb, new IllegalArgumentException("ym must be yyyy-MM, but was: " + ym));
            return;
        }

        LedgerRepository repo = new LedgerRepository(ctx, token);

        // ✅ 레포에 getMonth(String ym) 이 있는 경우
        Call<LedgerMonthResponse> call = repo.getMonth(ym);

        // 🔁 레포가 getMonth(int year, int month) 만 있다면 위 한 줄 대신 아래 두 줄 사용
        // int year = Integer.parseInt(ym.substring(0, 4));
        // int month = Integer.parseInt(ym.substring(5, 7));
        // Call<LedgerMonthResponse> call = repo.getMonth(year, month);

        call.enqueue(new Callback<LedgerMonthResponse>() {
            @Override public void onResponse(Call<LedgerMonthResponse> call, Response<LedgerMonthResponse> r) {
                if (!r.isSuccessful()) { postError(cb, new IOException(httpErrorString(r))); return; }
                LedgerMonthResponse body = r.body();
                if (body == null) { postError(cb, new NullPointerException("empty body")); return; }
                postLoaded(cb, body);
            }
            @Override public void onFailure(Call<LedgerMonthResponse> call, Throwable t) {
                if (call.isCanceled()) return;
                postError(cb, t);
            }
        });
    }

    /** year/month 버전도 필요하면 */
    public static void load(@NonNull Context ctx,
                            @NonNull String token,
                            int year, int month,
                            @NonNull OnMonthLoaded cb) {
        if (month < 1 || month > 12) {
            postError(cb, new IllegalArgumentException("month must be 1..12"));
            return;
        }
        String ym = String.format("%04d-%02d", year, month);
        load(ctx, token, ym, cb);
    }

    private static boolean isValidYm(String ym) {
        if (ym == null || ym.length() != 7) return false; // "yyyy-MM"
        return ym.matches("^\\d{4}-(0[1-9]|1[0-2])$");
    }
}
