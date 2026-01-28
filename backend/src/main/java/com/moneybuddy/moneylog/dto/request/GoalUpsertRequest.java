package com.moneybuddy.moneylog.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class GoalUpsertRequest {
    private BigDecimal amount; // 0 이상
}
