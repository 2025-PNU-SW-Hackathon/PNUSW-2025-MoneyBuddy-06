package com.moneybuddy.moneylog.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserChallengeRequest {
    private Long challengeId;
    private String type;
    private String category;
    private String title;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;
    private String description;
    private Boolean isAccountLinked;
    private Boolean isShared;
}
