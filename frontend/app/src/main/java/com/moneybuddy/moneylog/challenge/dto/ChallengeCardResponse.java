package com.moneybuddy.moneylog.challenge.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChallengeCardResponse implements Serializable {
    private Long challengeId;
    private String title;
    private String description;
    private String type;
    private String category;
    private String goalPeriod;
    private String goalType;
    private int goalValue;

    @SerializedName("systemGenerated")
    private Boolean isSystemGenerated;

    @SerializedName("accountLinked")
    private Boolean isAccountLinked;

    private Long createdBy;

    @SerializedName("mine")
    private Boolean isMine;

    @SerializedName("joined")
    private Boolean isJoined;

    private String joinedAt;
    private int currentParticipants;
    private Boolean completed;
    private Boolean success;
    private Boolean rewarded;

    private String mobtiType;

    public ChallengeCardResponse(RecommendedChallengeResponse recommended) {
        this.challengeId = recommended.getId();
        this.title = recommended.getTitle();
        this.description = recommended.getDescription();
        this.type = recommended.getType();
        this.category = recommended.getCategory();
        this.goalPeriod = recommended.getGoalPeriod();
        this.goalType = recommended.getGoalType();
        this.goalValue = recommended.getGoalValue();
        this.isAccountLinked = recommended.isAccountLinked();
        this.mobtiType = recommended.getMobtiType();

        this.isMine = false;
        this.isJoined = false;
        this.joinedAt = null;
        this.currentParticipants = 0;
        this.completed = false;
        this.success = false;
        this.rewarded = false;
        this.createdBy = null;
        this.isSystemGenerated = null;
    }

    public Long getChallengeId() { return challengeId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getGoalPeriod() { return goalPeriod; }
    public String getGoalType() { return goalType; }
    public int getGoalValue() { return goalValue; }
    public Boolean isAccountLinked() { return isAccountLinked; }
    public Boolean isMine() { return isMine; }
    public Boolean isJoined() { return isJoined; }
    public Boolean getSuccess() { return success; }

    public long getDaysSinceJoined() {
        if (joinedAt == null || joinedAt.isEmpty()) {
            return 0;
        }
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            Date joinedDate = parser.parse(joinedAt);
            Date today = new Date();
            long diffInMillis = today.getTime() - joinedDate.getTime();
            return TimeUnit.MILLISECONDS.toDays(diffInMillis);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getGoalPeriodInDays() {
        if (goalPeriod == null || goalPeriod.trim().isEmpty()) {
            return 0;
        }

        int numericValue;
        try {
            String numberOnly = goalPeriod.replaceAll("[^0-9]", "");
            if (numberOnly.isEmpty()) {
                return 0;
            }
            numericValue = Integer.parseInt(numberOnly);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }

        if (goalPeriod.contains("달") || goalPeriod.contains("month")) {
            return numericValue * 30;
        } else if (goalPeriod.contains("주") || goalPeriod.contains("week")) {
            return numericValue * 7;
        } else {
            return numericValue;
        }
    }
}

