package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.ChallengeRewardRequest;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.ChallengeRewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeRewardController {

    private final ChallengeRewardService challengeRewardService;

    @PostMapping("/reward")
    public ResponseEntity<String> rewardChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChallengeRewardRequest request
    ) {
        challengeRewardService.rewardChallenge(userDetails.getUserId(), request.getChallengeId());
        return ResponseEntity.ok("보상 지급 완료!");
    }
}
