package com.moneybuddy.moneylog.dto.response;

import com.google.gson.annotations.SerializedName;

public class QuizResultResponse {
    @SerializedName("correct")
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