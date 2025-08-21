package com.moneybuddy.moneylog.data.dto.ledger;

import java.util.List;

public class MonthSummaryDto {
    public String yearMonth;     // "2025-08"
    public long totalExpense;    // 양수
    public long totalIncome;     // 양수
    public long balance;         // income - expense
    public List<LedgerEntryDto> entries;
}
