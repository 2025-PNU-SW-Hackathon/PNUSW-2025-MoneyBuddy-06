package com.moneybuddy.moneylog.launcher;

import android.content.Context;
import android.net.Uri;

import com.moneybuddy.moneylog.dto.ledger.LedgerEntryDto;
import com.moneybuddy.moneylog.repository.ReceiptRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptOcrLauncher {

    public interface OnOcrReady {
        void onSuccess(LedgerEntryDto dto);   // ← OcrResultDto → LedgerEntryDto 로 변경
        void onError(Throwable t);
    }

    public static void uploadAndBind(Context ctx, String token, Uri imageUri, OnOcrReady cb) {
        try {
            ReceiptRepository repo = new ReceiptRepository(ctx, token);
            // repo.uploadOcr(...) 는 Call<LedgerEntryDto> 를 반환
            repo.uploadOcr(imageUri).enqueue(new Callback<LedgerEntryDto>() {
                @Override
                public void onResponse(Call<LedgerEntryDto> call, Response<LedgerEntryDto> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        cb.onSuccess(res.body());
                    } else {
                        cb.onError(new RuntimeException("OCR 실패: HTTP " + res.code()));
                    }
                }
                @Override
                public void onFailure(Call<LedgerEntryDto> call, Throwable t) {
                    cb.onError(t);
                }
            });
        } catch (Exception e) {
            cb.onError(e);
        }
    }
}
