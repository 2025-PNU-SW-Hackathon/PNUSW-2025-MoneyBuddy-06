package com.moneybuddy.moneylog.challenge.dto;

public class ChallengeCreateRequest {
    private String title;
    private String description;
    private String category;
    private String type;
    private String goalPeriod;
    private String goalType;
    private int goalValue;
    private Boolean isShared;

    public ChallengeCreateRequest(String title, String description, String category, String type, String goalPeriod, String goalType, int goalValue, Boolean isShared) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.goalPeriod = goalPeriod;
        this.goalType = goalType;
        this.goalValue = goalValue;
        this.isShared = isShared;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getGoalPeriod() { return goalPeriod; }
    public String getGoalType() { return goalType; }
    public int getGoalValue() { return goalValue; }
    public Boolean getIsShared() { return isShared; }
}
