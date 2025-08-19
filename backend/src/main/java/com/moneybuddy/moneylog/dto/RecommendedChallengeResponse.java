package com.moneybuddy.moneylog.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedChallengeResponse {
    private Long id;
    private String title;
    private String description;
    private String type;

    private String goalPeriod;
    private String goalType;
    private Integer goalValue;

    private Boolean isSystemGenerated;
    private Boolean isAccountLinked;

    private String mobtiType;
}
