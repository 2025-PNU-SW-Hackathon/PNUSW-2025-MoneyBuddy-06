package com.moneybuddy.moneylog.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuizAnswerRequest {
    private Long quizId;     // 배정된 퀴즈 id
    private Boolean answer;  // 사용자가 고른 답 (true/false)
}
