package com.moneybuddy.moneylog.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DaySummaryDto(
        String date,
        BigDecimal totalExpense,
        BigDecimal totalIncome,
        List<LedgerEntryDto> entries
) {}
