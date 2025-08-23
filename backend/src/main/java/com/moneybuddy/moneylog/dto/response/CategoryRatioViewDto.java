package com.moneybuddy.moneylog.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CategoryRatioViewDto(
        String yearMonth,
        BigDecimal goalAmount,   // null이면 목표 금액 없음
        BigDecimal spent,   // 이 달 지출 합(양수)
        boolean exceeded,   // 목표 금액 초과 여부
        String baseline,   // "GOAL" 또는 "SPENT" (분모로 사용한 기준)
        List<CategoryRatioItemDto> items
) {}
