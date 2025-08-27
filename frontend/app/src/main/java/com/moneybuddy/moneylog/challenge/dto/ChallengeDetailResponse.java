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
}