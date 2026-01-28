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
    private int goalValue;
    private boolean isAccountLinked;
    private boolean isShared;

    private LocalDateTime joinedAt;
    private boolean completed;
    private boolean rewarded;
}
