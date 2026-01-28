package com.moneybuddy.moneylog.dto;

import java.math.BigDecimal;

public record CategoryRatioItemDto(
        String category,
        BigDecimal expense,  // 해당 카테고리 지출 합(양수)
        BigDecimal ratioPercent  // 기준 대비 비율
) {}
