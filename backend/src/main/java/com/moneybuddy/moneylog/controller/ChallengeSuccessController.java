package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.ChallengeRewardResponse;
import com.moneybuddy.moneylog.dto.request.ChallengeSuccessRequest;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.ChallengeSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeSuccessController {

    private final ChallengeSuccessService challengeSuccessService;

    // 하루 챌린지 성공 기록 API
    @PostMapping("/success")
    public ResponseEntity<String> recordChallengeSuccess(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChallengeSuccessRequest request
    ) {
        challengeSuccessService.recordSuccess(userDetails.getUserId(), request.getChallengeId());
        return ResponseEntity.ok("하루 성공 기록 완료!");
    }

}