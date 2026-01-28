package com.moneybuddy.moneylog.challenge.dto;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChallengeDetailResponse implements Serializable {
    private Long challengeId;
    private String title;
    private String goalPeriod;
    private String joinedAt;

    public ChallengeDetailResponse(long challengeId, String title, String goalPeriod, String joinedAt) {
        this.challengeId = challengeId;
        this.title = title;
        this.goalPeriod = goalPeriod;
        this.joinedAt = joinedAt;
    }

    public Long getChallengeId() { return challengeId; }
    public String getTitle() { return title; }
    public String getGoalPeriod() { return goalPeriod; }
    public String getJoinedAt() { return joinedAt; }

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