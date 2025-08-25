package com.moneybuddy.moneylog.dto;

public class UserChallengeRequest {
    private Long challengeId;

    public UserChallengeRequest(Long challengeId) {
        this.challengeId = challengeId;
    }
    public Long getChallengeId() { return challengeId; }
}