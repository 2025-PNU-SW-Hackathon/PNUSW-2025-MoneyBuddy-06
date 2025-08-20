// dto/ChallengeRewardResponse.java
package com.moneybuddy.moneylog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChallengeRewardResponse {
    private Long challengeId;
    private String title;
    private boolean rewarded;
    private String rewardMessage;
}
