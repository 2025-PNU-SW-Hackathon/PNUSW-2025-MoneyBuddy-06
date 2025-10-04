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

    private boolean isSystemGenerated;
    private boolean isAccountLinked;
    private Long createdBy;


    private boolean isJoined;
    private boolean isShared;
    private boolean mine;

    private LocalDateTime joinedAt;
    private int currentParticipants;
    private boolean completed;
    private boolean success;
    private boolean rewarded;

    private String mobtiType;

}
