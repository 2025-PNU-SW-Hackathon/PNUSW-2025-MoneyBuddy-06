package com.moneybuddy.moneylog.ledger.dto.response;

import java.util.List;

public class DaySummaryDto {
    public String date;          // "2025-08-14"
    public long totalExpense;    // 양수
    public long totalIncome;     // 양수
    public List<LedgerEntryDto> entries;
}
