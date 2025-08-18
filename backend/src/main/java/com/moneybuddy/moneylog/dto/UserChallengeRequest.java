package com.moneybuddy.moneylog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserChallengeRequest {
    private String type;         // 지출 / 저축 / 기타
    private String category;     // 식비, 카페 등
    private String title;
    private String goalPeriod;   // 1주 / 2주 / 1달
    private String goalType;     // "금액" or "횟수"
    private Integer goalValue;   // 목표 금액 or 목표 일수 (횟수)
    private String description;
    private Boolean isShared;
}
