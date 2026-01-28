package com.moneybuddy.moneylog.dto.response;

import java.math.BigDecimal;

public record SumResult(
        BigDecimal expense, // 지출 합(양수)
        BigDecimal income // 수입 합
) {}
