package com.moneybuddy.moneylog.mypage.dto;

import com.google.gson.annotations.SerializedName;

public class PushSettingResponse {

    @SerializedName("enabled")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }
}