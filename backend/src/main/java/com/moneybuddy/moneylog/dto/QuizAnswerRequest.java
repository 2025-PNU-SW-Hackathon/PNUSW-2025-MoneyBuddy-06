package com.moneybuddy.moneylog.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerRequest {

    private Long quizId;
    private Boolean answer;
}
