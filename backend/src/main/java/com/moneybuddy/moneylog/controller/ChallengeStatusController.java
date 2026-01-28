package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.ChallengeStatusRequest;
import com.moneybuddy.moneylog.dto.response.ChallengeStatusResponse;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.ChallengeSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeStatusController {

    private final ChallengeSuccessService challengeSuccessService;

    // 하루 챌린지 성공 상태 업데이트 API
    @PostMapping("/status")
    public ResponseEntity<ChallengeStatusResponse> updateChallengeStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChallengeStatusRequest request
    ) {
        Long userId = userDetails.getUserId();
        Long challengeId = request.getChallengeId();
        boolean isTodayCompleted = request.isTodayCompleted();

        ChallengeStatusResponse response =
                challengeSuccessService.updateTodayStatus(userId, challengeId, isTodayCompleted);

        return ResponseEntity.ok(response);
    }
}