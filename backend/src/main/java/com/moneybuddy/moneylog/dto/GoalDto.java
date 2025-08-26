package com.moneybuddy.moneylog.dto;

import java.math.BigDecimal;

public record GoalDto(
        String yearMonth,
        BigDecimal amount
) {}
