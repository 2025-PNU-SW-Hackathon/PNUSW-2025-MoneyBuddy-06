package com.moneybuddy.moneylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserStatusResponse {
    private Long userId;
    private int level;
    private int experience;
}
