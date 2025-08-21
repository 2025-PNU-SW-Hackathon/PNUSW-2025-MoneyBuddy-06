package com.moneybuddy.moneylog.data.remote;

import com.moneybuddy.moneylog.data.dto.analytics.CategoryRatioResponse;
import com.moneybuddy.moneylog.data.dto.auto.AutoLedgerRequest;
import com.moneybuddy.moneylog.data.dto.auto.AutoLedgerResponse;
import com.moneybuddy.moneylog.data.dto.budget.BudgetGoalDto;
import com.moneybuddy.moneylog.data.dto.ledger.LedgerCreateRequest;
import com.moneybuddy.moneylog.data.dto.ledger.LedgerDayResponse;
import com.moneybuddy.moneylog.data.dto.ledger.LedgerEntryDto;
import com.moneybuddy.moneylog.data.dto.ledger.LedgerMonthResponse;
import com.moneybuddy.moneylog.data.dto.ocr.OcrResultDto;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface LedgerApi {

    // 수동 작성/수정/삭제 (이미 존재한다고 하셔서 유지)
    @POST("/api/ledger")
    Call<WrappedEntry> create(@Body LedgerCreateRequest body);

    @PUT("/api/ledger/{id}")
    Call<WrappedEntry> update(@Path("id") long id, @Body LedgerCreateRequest body);

    @DELETE("/api/ledger/{id}")
    Call<Void> delete(@Path("id") long id);
    class WrappedEntry {
        public String status;
        public LedgerEntryDto entry;
    }

    // 월/일 요약
    @GET("/api/ledger/month")
    Call<LedgerMonthResponse> getMonth(@Query("ym") String yearMonth); // "YYYY-MM"

    @GET("/api/ledger/day")
    Call<LedgerDayResponse> getDay(@Query("date") String date); // "YYYY-MM-DD"

    // 자동 작성: 인앱 알림 텍스트 파싱
    @POST("/api/ledger/auto")
    Call<AutoLedgerResponse> postAuto(@Body AutoLedgerRequest body);

    // 영수증 OCR 업로드 (Multipart)
    @Multipart
    @POST("/api/receipt/ocr")
    Call<OcrResultDto> uploadReceipt(@Part MultipartBody.Part file);

    // 예산 목표 업서트/조회
    @PUT("/budget-goal")
    Call<BudgetGoalDto> putBudgetGoal(@Query("ym") String yearMonth, @Body BudgetGoalDto body);

    @GET("/budget-goal")
    Call<BudgetGoalDto> getBudgetGoal(@Query("ym") String yearMonth);

    // 카테고리 비율
    @GET("/analytics/category-ratio")
    Call<CategoryRatioResponse> getCategoryRatio(@Query("ym") String yearMonth);
}
