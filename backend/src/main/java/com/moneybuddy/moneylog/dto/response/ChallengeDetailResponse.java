package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChallengeDetailResponse {
    private Long challengeId;
    private String title;
    private String description;
    private String goalPeriod;
    private String goalType;
    private int goalValue;
    private int currentParticipants;
    private boolean isJoined;
}
