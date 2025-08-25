package com.moneybuddy.moneylog.mobti.dto.response;

import java.util.List;

public class MobtiFullDto {
    private String code;
    private String nickname;
    private String summary;
    private List<String> detailTraits;
    private List<String> spendingTendency;
    private List<String> socialStyle;

    public String getCode() { return code; }
    public String getNickname() { return nickname; }
    public String getSummary() { return summary; }
    public List<String> getDetailTraits() { return detailTraits; }
    public List<String> getSpendingTendency() { return spendingTendency; }
    public List<String> getSocialStyle() { return socialStyle; }
}
