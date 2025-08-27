package com.moneybuddy.moneylog.finance.dto.request;

public class QuizAnswerRequest {
    private Long quizId;
    private boolean userAnswer;

    public QuizAnswerRequest(Long quizId, boolean userAnswer) {
        this.quizId = quizId;
        this.userAnswer = userAnswer;
    }
}