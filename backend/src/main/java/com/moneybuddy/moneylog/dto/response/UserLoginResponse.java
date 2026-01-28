package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class UserLoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String message;

    public UserLoginResponse(String message) {
        this.message = message;
    }

    public UserLoginResponse(String token, Long userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.message = "로그인 성공";
    }


}
