package com.moneybuddy.moneylog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class UserLoginResponse {
    private String status;
    private String message;
}
