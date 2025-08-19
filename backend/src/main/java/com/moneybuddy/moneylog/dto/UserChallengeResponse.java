package com.moneybuddy.moneylog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserChallengeResponse {
    private Long challengeId;
    private String title;
    private String description;
    private String type;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;
    private Boolean isAccountLinked;

    private Boolean completed;
    private Boolean verified;
    private Boolean rewarded;
    private LocalDateTime joinedAt;
}
