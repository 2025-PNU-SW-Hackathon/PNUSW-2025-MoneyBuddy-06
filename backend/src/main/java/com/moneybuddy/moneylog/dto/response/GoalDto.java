package com.moneybuddy.moneylog.dto.response;

import java.math.BigDecimal;

public record GoalDto(
        String yearMonth,
        BigDecimal amount
) {}
