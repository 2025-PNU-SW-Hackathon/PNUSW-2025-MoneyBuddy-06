package com.moneybuddy.moneylog.finance.dto.response;

import com.google.gson.annotations.SerializedName;

public class QuizResultResponse {
    @SerializedName("isCorrect")
    private boolean isCorrect;

    @SerializedName("explanation")
    private String explanation;

    public boolean isCorrect() {
        return isCorrect;
    }

    public String getExplanation() {
        return explanation;
    }
}