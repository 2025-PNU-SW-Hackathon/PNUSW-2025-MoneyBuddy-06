package com.moneybuddy.moneylog.data.dto.ledger;

import java.util.List;

public class DaySummaryDto {
    public String date;          // "2025-08-14"
    public long totalExpense;    // 양수
    public long totalIncome;     // 양수
    public List<LedgerEntryDto> entries;
}
