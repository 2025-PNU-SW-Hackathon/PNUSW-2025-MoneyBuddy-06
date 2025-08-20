package com.moneybuddy.moneylog.dto.response;

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
    private String category;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;
    private Boolean isAccountLinked;
    private Boolean isShared;

    private LocalDateTime joinedAt;
    private Boolean completed;
    private Boolean rewarded;
}
