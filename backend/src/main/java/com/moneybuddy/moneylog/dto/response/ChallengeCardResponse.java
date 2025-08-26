package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ChallengeCardResponse {
    private Long challengeId;
    private String title;
    private String description;
    private String type;
    private String category;
    private String goalPeriod;
    private String goalType;
    private int goalValue;

    private String isSystemGenerated;
    private Boolean isAccountLinked;
    private String createdBy;
    private Boolean isMine;

    private Boolean isJoined;
    private LocalDateTime joinedAt;
    private int currentParticipants;
    private Boolean completed;
    private Boolean success;
    private Boolean rewarded;

    private String mobtiType;

}
