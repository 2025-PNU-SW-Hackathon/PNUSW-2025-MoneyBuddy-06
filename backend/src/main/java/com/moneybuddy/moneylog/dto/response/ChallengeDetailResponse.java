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
    private String description;     // 챌린지 설명
    private String goalPeriod;      // ex. 4주간
    private String goalType;        // ex. "금액", "횟수"
    private int goalValue;          // ex. 100000, 3
    private int currentParticipants;
    private boolean isJoined;       // 이미 참여한 유저인지
}
