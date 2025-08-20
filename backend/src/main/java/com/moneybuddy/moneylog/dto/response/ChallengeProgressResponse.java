package com.moneybuddy.moneylog.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChallengeProgressResponse {

    private Long challengeId;
    private String title;
    private String description;
    private String type;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;
    private Boolean isAccountLinked;
    private Boolean isShared;

    private Boolean completed;       // 진행 완료 여부
    private LocalDateTime joinedAt;  // 참여 시작일
    private Boolean rewarded;
}
