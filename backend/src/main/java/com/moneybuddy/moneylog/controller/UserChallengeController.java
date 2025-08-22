package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.ChallengeFilterRequest;
import com.moneybuddy.moneylog.dto.response.ChallengeCardResponse;
import com.moneybuddy.moneylog.dto.request.UserChallengeRequest;
import com.moneybuddy.moneylog.dto.response.UserChallengeResponse;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.UserChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class UserChallengeController {

    private final UserChallengeService userChallengeService;

    // 챌린지 참여 API
    @PostMapping("/join")
    public ResponseEntity<UserChallengeResponse> joinChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserChallengeRequest request
    ) {
        UserChallengeResponse response = userChallengeService.joinChallenge(
                userDetails.getUserId(),
                request.getChallengeId()
        );
        return ResponseEntity.ok(response);
    }

    // 진행 중 조회
    @GetMapping("/view/ongoing")
    public List<ChallengeCardResponse> getOngoing(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userChallengeService.getOngoingChallenges(userDetails.getUserId());
    }

    @PostMapping("/ongoing/filter")
    public List<ChallengeCardResponse> filterOngoingChallenges(
            @RequestParam Long userId,
            @RequestBody ChallengeFilterRequest request) {
        return userChallengeService.filterOngoingChallenges(userId, request);
    }

    // 완료된 챌린지 조회
    @GetMapping("/view/completed")
    public List<ChallengeCardResponse> getCompleted(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userChallengeService.getCompletedChallenges(userDetails.getUserId());
    }

    @PostMapping("/completed/filter")
    public List<ChallengeCardResponse> filterCompletedChallenges(
            @RequestParam Long userId,
            @RequestBody ChallengeFilterRequest request) {
        return userChallengeService.filterCompletedChallenges(userId, request);
    }

    // 챌린지 가계부 연동 API
    @PostMapping("/evaluate")
    public ResponseEntity<Void> evaluateAll(@RequestParam Long userId) {
        userChallengeService.evaluateOngoingChallenges(userId);
        return ResponseEntity.ok().build();
    }
}
