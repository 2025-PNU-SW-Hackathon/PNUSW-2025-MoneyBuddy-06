package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.ChallengeResultListResponse;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.ChallengeSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeResultController {

    private final ChallengeSuccessService challengeSuccessService;

    // 챌린지 성공/실패 결과 조회 API
    @GetMapping("/results")
    public ChallengeResultListResponse getChallengeResults(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return challengeSuccessService.getChallengeResults(userDetails.getUserId());
    }
}
