package com.moneybuddy.moneylog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedChallengeResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String category;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;
    private String isShared;
    private Boolean isSystemGenerated;
    private Boolean isAccountLinked;
    private String mobtiType;
}
