package com.moneybuddy.moneylog.dto.response;

import com.moneybuddy.moneylog.domain.Quiz;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuizResponse {
    Long quizId;
    String question;

    public static QuizResponse from(Quiz q) {
        return QuizResponse.builder()
                .quizId(q.getId())
                .question(q.getQuestion())
                .build();
    }
}
