package com.moneybuddy.moneylog.challenge.dto;

import java.util.List;

public class ChallengeFilterRequest {
    private List<String> categories;
    public ChallengeFilterRequest(List<String> categories) {
        this.categories = categories;
    }
}