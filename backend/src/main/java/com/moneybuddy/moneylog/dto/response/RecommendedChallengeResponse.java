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
    private int goalValue;
    private boolean isShared;
    private boolean isSystemGenerated;
    private boolean isAccountLinked;
    private String mobtiType;
}
