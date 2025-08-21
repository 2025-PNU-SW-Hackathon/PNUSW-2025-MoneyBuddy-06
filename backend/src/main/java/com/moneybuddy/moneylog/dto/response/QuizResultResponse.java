package com.moneybuddy.moneylog.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultResponse {

    private Boolean isCorrect;
    private String explanation;
    private Boolean scoreUpdated;
    private Integer newScore;
}
