package com.moneybuddy.moneylog.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeResponse {
    private Long id;
    private String title;
    private String description;
    private String period;
    private String mobtiType;
    private String category;
}
