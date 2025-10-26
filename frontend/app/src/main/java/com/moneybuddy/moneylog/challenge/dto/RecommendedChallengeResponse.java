package com.moneybuddy.moneylog.challenge.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RecommendedChallengeResponse implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String category;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;

    @SerializedName("accountLinked")
    private Boolean isAccountLinked;

    private String mobtiType;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getGoalPeriod() { return goalPeriod; }
    public String getGoalType() { return goalType; }
    public Integer getGoalValue() { return goalValue; }
    public Boolean isAccountLinked() { return isAccountLinked; }
    public String getMobtiType() { return mobtiType; }
}