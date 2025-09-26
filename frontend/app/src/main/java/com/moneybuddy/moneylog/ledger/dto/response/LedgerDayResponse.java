package com.moneybuddy.moneylog.ledger.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

/**
 * GET /api/ledger/day?date=YYYY-MM-DD 응답 DTO
 *
 * {
 *   "date": "2025-08-14",
 *   "totalExpense": 46200,     // 지출 합(양수)
 *   "totalIncome": 0,          // 수입 합(양수)
 *   "entries": [ LedgerEntryDto, ... ]
 * }
 */
public class LedgerDayResponse {

    @SerializedName("date")
    private String date;                  // "YYYY-MM-DD"

    // 서버에서 없을 수도 있다고 가정해 Long(nullable) 사용
    @SerializedName("totalExpense")
    private Long totalExpense;            // 지출 합(양수)

    @SerializedName("totalIncome")
    private Long totalIncome;             // 수입 합(양수)

    @SerializedName("entries")
    private List<LedgerEntryDto> entries; // 해당 날짜 항목

    // ===== getters =====
    public String getDate() { return date; }

    /** null-safe: 값 없으면 0 반환 */
    public long getTotalExpense() { return totalExpense == null ? 0L : totalExpense; }

    /** null-safe: 값 없으면 0 반환 */
    public long getTotalIncome() { return totalIncome == null ? 0L : totalIncome; }

    /** null-safe: null이면 빈 리스트 반환 */
    public List<LedgerEntryDto> getEntries() {
        return entries == null ? Collections.emptyList() : entries;
    }

    // ===== setters (필요하면 유지) =====
    public void setDate(String date) { this.date = date; }
    public void setTotalExpense(Long totalExpense) { this.totalExpense = totalExpense; }
    public void setTotalIncome(Long totalIncome) { this.totalIncome = totalIncome; }
    public void setEntries(List<LedgerEntryDto> entries) { this.entries = entries; }
}
