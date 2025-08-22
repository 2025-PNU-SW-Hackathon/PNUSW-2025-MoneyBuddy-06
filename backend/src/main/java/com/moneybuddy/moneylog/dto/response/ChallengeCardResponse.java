package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ChallengeCardResponse {
    private Long challengeId;
    private String title;
    private String category;
    private String type;
    private String goalType;
    private String goalPeriod;
    private int goalValue;
    private Boolean isAccountLinked;
    private Boolean isMine;
    private Boolean completed;
    private Boolean success;
}
