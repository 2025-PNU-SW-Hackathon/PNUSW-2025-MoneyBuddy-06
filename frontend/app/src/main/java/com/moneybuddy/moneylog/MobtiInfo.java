package com.moneybuddy.moneylog;

public class MobtiInfo {
    String name;     // 예: "MPTI"
    String name2;    // 예: "가계부 마스터"
    String desc;
    int imageResId;

    public MobtiInfo(String name, String name2, String desc, int imageResId) {
        this.name = name;
        this.name2 = name2;
        this.desc = desc;
        this.imageResId = imageResId;
    }
}

