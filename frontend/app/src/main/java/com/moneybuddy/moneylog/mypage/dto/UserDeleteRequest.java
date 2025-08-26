package com.moneybuddy.moneylog.mypage.dto;

import com.google.gson.annotations.SerializedName;

public class UserDeleteRequest {

    @SerializedName("password")
    private String password;

    public UserDeleteRequest(String password) {
        this.password = password;
    }
}