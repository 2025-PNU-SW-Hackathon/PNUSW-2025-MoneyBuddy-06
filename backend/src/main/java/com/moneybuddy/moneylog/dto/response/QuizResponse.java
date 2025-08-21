package com.moneybuddy.moneylog.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {

    private Long quizId;
    private String question;
    private Boolean alreadyAnswered;
}
