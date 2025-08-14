package com.moneybuddy.moneylog.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MonthSummaryDto(
        String yearMonth,  // "2025-08"
        BigDecimal totalExpense,  // 지출 합(양수)
        BigDecimal totalIncome,
        BigDecimal balance,  // income - expense
        List<LedgerEntryDto> entries
) {}
