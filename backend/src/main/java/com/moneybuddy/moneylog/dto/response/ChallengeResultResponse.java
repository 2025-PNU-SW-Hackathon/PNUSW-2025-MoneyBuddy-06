package com.moneybuddy.moneylog.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ChallengeResultResponse {
    private Long challengeId;
    private String title;
    private boolean success;
    private LocalDate lastSuccessDate;
    public ChallengeResultResponse(Long challengeId, String title, boolean success, LocalDate successDate) {
        this.challengeId = challengeId;
        this.title = title;
        this.success = success;
        this.lastSuccessDate = successDate;
    }
}
