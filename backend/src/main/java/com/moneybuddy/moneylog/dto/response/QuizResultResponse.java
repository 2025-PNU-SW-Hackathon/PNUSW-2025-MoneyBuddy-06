package com.moneybuddy.moneylog.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuizResultResponse {
    Boolean isCorrect;
    String explanation;
    Boolean scoreUpdated;
    Integer newScore;
}
