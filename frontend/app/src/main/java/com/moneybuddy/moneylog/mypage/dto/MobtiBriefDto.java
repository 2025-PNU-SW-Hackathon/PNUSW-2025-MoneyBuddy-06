package com.moneybuddy.moneylog.mypage.dto;

import com.google.gson.annotations.SerializedName;

public class MobtiBriefDto {

    @SerializedName("code")
    private String code;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("summary")
    private String summary;

    public String getCode() {
        return code;
    }

    public String getNickname() {
        return nickname;
    }

    public String getSummary() {
        return summary;
    }
}