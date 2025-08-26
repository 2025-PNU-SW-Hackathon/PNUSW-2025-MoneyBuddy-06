package com.moneybuddy.moneylog.finance.dto.response;

import com.google.gson.annotations.SerializedName;

public class QuizResponse {
    @SerializedName("quizId")
    private Long quizId;

    @SerializedName("question")
    private String question;

    public Long getQuizId() {
        return quizId;
    }

    public String getQuestion() {
        return question;
    }
}