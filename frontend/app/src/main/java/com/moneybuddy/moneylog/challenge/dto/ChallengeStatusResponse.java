package com.moneybuddy.moneylog.challenge.dto;

import com.google.gson.annotations.SerializedName;

public class ChallengeStatusResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("currentDay")
    private int currentDay;


    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getCurrentDay() {
        return currentDay;
    }

}