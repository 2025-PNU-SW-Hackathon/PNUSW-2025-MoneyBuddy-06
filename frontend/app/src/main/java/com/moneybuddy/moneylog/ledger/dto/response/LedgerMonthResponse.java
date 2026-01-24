package com.moneybuddy.moneylog.ledger.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LedgerMonthResponse {

    @SerializedName("yearMonth")
    private String yearMonth;

    @SerializedName("totalExpense")
    private long totalExpense;

    @SerializedName("totalIncome")
    private long totalIncome;

    @SerializedName("balance")
    private long balance;

    @SerializedName("entries")
    private List<LedgerEntryDto> entries;

    // ===== Getter =====
    public String getYearMonth() {
        return yearMonth;
    }

    public long getTotalExpense() {
        return totalExpense;
    }

    public long getTotalIncome() {
        return totalIncome;
    }

    public long getBalance() {
        return balance;
    }

    public List<LedgerEntryDto> getEntries() {
        return entries;
    }
}
