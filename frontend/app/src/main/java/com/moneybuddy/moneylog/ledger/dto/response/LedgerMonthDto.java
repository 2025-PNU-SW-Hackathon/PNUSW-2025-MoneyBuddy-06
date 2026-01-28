package com.moneybuddy.moneylog.ledger.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LedgerMonthDto {
    @SerializedName("yearMonth")    public String yearMonth;     // "2025-08"
    @SerializedName("totalExpense") public long totalExpense;    // 지출 합(양수)
    @SerializedName("totalIncome")  public long totalIncome;     // 수입 합
    @SerializedName("balance")      public long balance;         // income - expense
    @SerializedName("entries")      public List<LedgerEntryDto> entries;
}
