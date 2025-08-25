package com.moneybuddy.moneylog.dto;

import java.util.List;

public class ChallengeFilterRequest {
    private List<String> categories;
    public ChallengeFilterRequest(List<String> categories) {
        this.categories = categories;
    }
}