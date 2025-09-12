package com.moneybuddy.moneylog.challenge.dto;

import com.google.gson.annotations.SerializedName;

public class ChallengeStatusRequest {

    @SerializedName("challengeId")
    private Long challengeId;

    @SerializedName("todayCompleted")
    private boolean isTodayCompleted;

    // Getter
    public Long getChallengeId() {
        return challengeId;
    }

    // Setter
    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public boolean isTodayCompleted() {
        return isTodayCompleted;
    }

    public void setTodayCompleted(boolean todayCompleted) {
        isTodayCompleted = todayCompleted;
    }
}