package com.moneybuddy.moneylog.ledger.network;

import com.moneybuddy.moneylog.ledger.dto.request.AutoLedgerRequest;
import com.moneybuddy.moneylog.ledger.dto.request.LedgerCreateRequest;
import com.moneybuddy.moneylog.ledger.dto.response.AutoLedgerResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerDayResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LedgerApi {

    // ───────── 조회 ─────────
    // 예: GET /api/ledger/day?date=2025-08-28
    @GET("/api/ledger/day")
    Call<LedgerDayResponse> getDay(@Query("date") String yyyyMMdd);

    // 예: GET /api/ledger/month?ym=2025-08
    @GET("/api/ledger/month")
    Call<LedgerMonthResponse> getMonth(@Query("ym") String ym);

    // 서버가 year/month 쿼리를 받는다면 아래 시그니처를 사용
    // @GET("/api/ledger/month")
    // Call<LedgerMonthResponse> getMonth(@Query("year") int year, @Query("month") int month);

    // ───────── 생성/수정/삭제 ─────────
    // 생성: 서버 스펙이 Long(생성된 id) 반환
    @POST("/api/ledger")
    Call<Long> create(@Body LedgerCreateRequest body);

    // 수정: 서버 스펙이 Void(바디 없음) 반환
    @PUT("/api/ledger/{id}")
    Call<Void> update(@Path("id") long id, @Body LedgerCreateRequest body);

    // 삭제: 204 No Content
    @DELETE("/api/ledger/{id}")
    Call<Void> delete(@Path("id") long id);

    // ───────── 자동작성(영수증/알림) ─────────
    @POST("/api/ledger/auto")
    Call<AutoLedgerResponse> postAuto(@Body AutoLedgerRequest body);
}
