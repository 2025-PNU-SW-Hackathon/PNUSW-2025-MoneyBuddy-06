package com.moneybuddy.moneylog.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengeStatusRequest {
    private Long challengeId;
    private boolean isTodayCompleted;
}
