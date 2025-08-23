package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChallengeStatusResponse {
    private boolean success;   // API 처리 성공 여부
    private String message;    // 안내 메시지
    private int currentDay;    // 현재까지 몇 일 성공했는지
}
