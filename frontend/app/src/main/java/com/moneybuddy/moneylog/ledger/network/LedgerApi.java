package com.moneybuddy.moneylog.ledger.network;

import com.moneybuddy.moneylog.ledger.dto.request.AutoLedgerRequest;
import com.moneybuddy.moneylog.ledger.dto.request.LedgerCreateRequest;
import com.moneybuddy.moneylog.ledger.dto.response.AutoLedgerResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerDayResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;



public interface LedgerApi {
    @GET("api/ledger/day")
    Call<LedgerDayResponse> getDay(@Query("date") String yyyyMMdd);

    // 서버가 ym=yyyy-MM 으로 받는 경우
    @GET("api/ledger/month")
    Call<LedgerMonthResponse> getMonth(@Query("ym") String ym);

    // (대신 year/month 로 받으면 위 한 줄 대신 아래 사용)
    // @GET("ledger/month") Call<LedgerMonthResponse> getMonth(@Query("year") int y, @Query("month") int m);

    @POST("api/ledger")
    Call<Long> create(@Body LedgerCreateRequest body);

    @PUT("api/ledger/{id}")
    Call<Void> update(@Path("id") long id, @Body LedgerCreateRequest body);

    @POST("api/ledger/auto")   // ← 베이스 URL 뒤에 붙는 상대 경로. 서버 경로가 다르면 여길 맞추세요.
    Call<AutoLedgerResponse> postAuto(@Body AutoLedgerRequest body);
}
