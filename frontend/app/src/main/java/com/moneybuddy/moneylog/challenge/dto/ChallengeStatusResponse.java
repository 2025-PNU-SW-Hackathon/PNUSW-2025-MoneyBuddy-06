package com.moneybuddy.moneylog.challenge.dto;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class ChallengeStatusResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("currentDay")
    private int currentDay;

    @SerializedName("todaySuccess")
    private boolean isTodaySuccess; // 오늘 성공 여부

    @SerializedName("finalSuccess")
    private boolean isFinalSuccess;   // 최종 챌린지 성공 여부


    public String getMessage() {
        return message;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public boolean isTodaySuccess() {
        return isTodaySuccess;
    }

    public boolean isFinalSuccess() {
        return isFinalSuccess;
    }
}