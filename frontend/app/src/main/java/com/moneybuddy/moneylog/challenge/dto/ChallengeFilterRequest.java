package com.moneybuddy.moneylog.challenge.dto;

import java.util.List;

public class ChallengeFilterRequest {
    private List<String> category;
    public ChallengeFilterRequest(List<String> categories) {
        this.category = categories;
    }
}