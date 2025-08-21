package com.moneybuddy.moneylog.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChallengeProgressResponse {
    private Long challengeId;
    private String title;
    private String category;
    private String goalPeriod;
    private int goalValue;
    private boolean completed;
    private boolean success;
}
