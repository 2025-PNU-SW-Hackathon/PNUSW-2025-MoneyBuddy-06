package com.moneybuddy.moneylog.mypage.dto;

import com.google.gson.annotations.SerializedName;

public class UserExpResponse {

    @SerializedName("level")
    private int level;

    @SerializedName("experience")
    private int experience;

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }
}