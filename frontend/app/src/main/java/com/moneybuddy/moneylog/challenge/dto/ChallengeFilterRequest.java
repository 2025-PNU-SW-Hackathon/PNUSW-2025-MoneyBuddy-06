package com.moneybuddy.moneylog.challenge.dto;

import java.util.List;

public class ChallengeFilterRequest {
    private String type;          // ★ 추가: 지출/저축/습관
    private List<String> category;

    public ChallengeFilterRequest(String type, List<String> category) {
        this.type = type;
        this.category = category;
    }
}