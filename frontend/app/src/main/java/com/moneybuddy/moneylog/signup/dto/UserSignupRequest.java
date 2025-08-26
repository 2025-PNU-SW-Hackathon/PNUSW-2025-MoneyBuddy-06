package com.moneybuddy.moneylog.signup.dto;

import com.google.gson.annotations.SerializedName;

public class UserSignupRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public UserSignupRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}