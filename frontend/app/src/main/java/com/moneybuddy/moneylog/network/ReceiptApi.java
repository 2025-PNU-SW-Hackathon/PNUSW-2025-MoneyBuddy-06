package com.moneybuddy.moneylog.network;

import com.moneybuddy.moneylog.dto.ledger.LedgerEntryDto;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ReceiptApi {
    @Multipart
    @POST("/api/receipt/ocr")
    Call<LedgerEntryDto> uploadReceipt(@Part MultipartBody.Part file);
}
