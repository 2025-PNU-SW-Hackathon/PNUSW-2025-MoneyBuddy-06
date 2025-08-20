package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.RecommendedChallengeResponse;
import com.moneybuddy.moneylog.dto.request.UserChallengeRequest;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/recommended")
    public List<RecommendedChallengeResponse> getRecommendedChallenges(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return challengeService.getRecommendedChallenges(userDetails.getUserId());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUserChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserChallengeRequest request
    ) {
        System.out.println("userDetails = " + userDetails);
        System.out.println("userId = " + userDetails.getUserId());
        challengeService.createUserChallenge(userDetails.getUserId(), request);
        return ResponseEntity.ok("챌린지 생성 완료!");
    }
}
