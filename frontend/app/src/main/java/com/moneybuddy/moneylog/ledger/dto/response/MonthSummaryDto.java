package com.moneybuddy.moneylog.ledger.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

/**
 * GET /api/ledger/month?ym=YYYY-MM 응답 DTO
 */
public class MonthSummaryDto {

    @SerializedName("yearMonth")
    public String yearMonth;     // "2025-08"

    @SerializedName("totalExpense")
    public long totalExpense;    // 양수

    @SerializedName("totalIncome")
    public long totalIncome;     // 양수

    @SerializedName("balance")
    public long balance;         // income - expense

    @SerializedName("entries")
    public List<LedgerEntryDto> entries;

    // ===== getters (호출부 호환용) =====
    public String getYearMonth() { return yearMonth; }
    public long getTotalExpense() { return totalExpense; }
    public long getTotalIncome() { return totalIncome; }
    public long getBalance() { return balance; }

    /** null-safe: entries가 null이면 빈 리스트 반환 */
    public List<LedgerEntryDto> getEntries() {
        return entries == null ? Collections.emptyList() : entries;
    }
}
