package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SharedChallengeResponse {
    private Long challengeId;
    private String title;
    private String goalPeriod;
    private int goalValue;
    private boolean isMine;
}
