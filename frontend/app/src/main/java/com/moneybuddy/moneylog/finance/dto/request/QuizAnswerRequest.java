package com.moneybuddy.moneylog.finance.dto.request;

public class QuizAnswerRequest {
    private Long quizId;
    private boolean answer;

    public QuizAnswerRequest(Long quizId, boolean answer) {
        this.quizId = quizId;
        this.answer = answer;
    }
}