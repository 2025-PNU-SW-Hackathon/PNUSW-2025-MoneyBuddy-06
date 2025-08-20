package com.moneybuddy.moneylog.dto;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {

    @SerializedName("currentPassword")
    private String currentPassword;

    @SerializedName("newPassword")
    private String newPassword;

    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}