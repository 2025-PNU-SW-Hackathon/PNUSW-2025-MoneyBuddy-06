package com.moneybuddy.moneylog.dto.ledger;

import java.util.List;

public class LedgerDayResponse {
    public String date;         // "YYYY-MM-DD"
    public Long totalExpense;   // 지출 합(양수)
    public Long totalIncome;    // 수입 합(양수)
    public List<LedgerEntryDto> entries;
}
