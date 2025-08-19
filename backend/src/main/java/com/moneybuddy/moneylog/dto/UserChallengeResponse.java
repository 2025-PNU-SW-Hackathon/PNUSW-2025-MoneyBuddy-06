package com.moneybuddy.moneylog.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserChallengeResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String category;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;
    private Boolean isAccountLinked;
    private Boolean isShared;
    private Long createdBy;
}
