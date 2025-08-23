package com.moneybuddy.moneylog.ledger.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.moneybuddy.moneylog.common.ResultCallback;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.common.RetrofitProvider;
import com.moneybuddy.moneylog.ledger.network.ReceiptApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 영수증 OCR 업로드 레포지토리
 * - 오버로드 2종 제공:
 *   1) Call 반환형: uploadOcr(Uri)  → 호출부에서 enqueue 사용
 *   2) 콜백형    : uploadOcr(Uri, ResultCallback<LedgerEntryDto>)  → 내부에서 enqueue
 */
public class ReceiptRepository {

    private final Context appCtx;
    private final ReceiptApi api;

    public ReceiptRepository(Context ctx, String jwtToken) {
        this.appCtx = ctx.getApplicationContext();
        this.api = RetrofitProvider.get(appCtx, jwtToken).create(ReceiptApi.class);
    }

    /** ① Call 반환형: 호출부에서 enqueue 하기 위함 (ReceiptOcrLauncher에서 사용) */
    public Call<LedgerEntryDto> uploadOcr(Uri imageUri) throws Exception {
        // 1) Uri -> temp file
        ContentResolver cr = appCtx.getContentResolver();
        String fileName = "receipt_" + System.currentTimeMillis() + ".jpg";
        File temp = new File(appCtx.getCacheDir(), fileName);
        try (InputStream in = cr.openInputStream(imageUri);
             FileOutputStream out = new FileOutputStream(temp)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
        }

        // 2) multipart
        RequestBody rb = RequestBody.create(temp, MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", temp.getName(), rb);

        // 3) Retrofit Call 반환
        return api.uploadReceipt(part);
    }

    /** ② 콜백형: 내부에서 enqueue까지 처리 */
    public void uploadOcr(Uri imageUri, ResultCallback<LedgerEntryDto> cb) {
        try {
            uploadOcr(imageUri).enqueue(new Callback<LedgerEntryDto>() {
                @Override public void onResponse(Call<LedgerEntryDto> call, Response<LedgerEntryDto> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        cb.onSuccess(res.body());
                    } else {
                        cb.onError(new Exception("OCR 실패: HTTP " + res.code()));
                    }
                }
                @Override public void onFailure(Call<LedgerEntryDto> call, Throwable t) {
                    cb.onError(t);
                }
            });
        } catch (Exception e) {
            cb.onError(e);
        }
    }
}
