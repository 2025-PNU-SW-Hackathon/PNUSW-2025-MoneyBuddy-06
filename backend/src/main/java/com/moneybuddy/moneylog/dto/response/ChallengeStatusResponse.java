package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChallengeStatusResponse {
    private String message;        // 안내 메시지
    private int currentDay;        // 현재까지 몇 일 성공했는지
    private boolean todaySuccess;  // 오늘 성공 여부
    private boolean finalSuccess;  // 최종 챌린지 성공 여부
}
