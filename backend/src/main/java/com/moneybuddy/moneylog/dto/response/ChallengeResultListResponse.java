package com.moneybuddy.moneylog.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChallengeResultListResponse {
    private List<ChallengeResultResponse> successChallenges;
    private List<ChallengeResultResponse> failedChallenges;

    public ChallengeResultListResponse(List<ChallengeResultResponse> successList, List<ChallengeResultResponse> failedList) {
        this.successChallenges = successList;
        this.failedChallenges = failedList;
    }
}