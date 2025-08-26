package com.moneybuddy.moneylog.ledger.dto.response;

import java.util.List;

public class LedgerMonthResponse {
    public String yearMonth;    // "YYYY-MM"
    public long totalExpense;   // 지출 합(양수)
    public long totalIncome;    // 수입 합(양수)
    public long balance;        // income - expense
    public List<LedgerEntryDto> entries;
}
