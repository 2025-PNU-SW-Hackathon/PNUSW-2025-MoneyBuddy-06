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
@RequestMapping(value = "/api/v1/challenges", produces = "application/json")
public class UserChallengeController {

    private final UserChallengeService userChallengeService;

    // 챌린지 참여 API
    @PostMapping(value = "/join", consumes = "application/json")
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

    // 진행 중 필터 (프론트는 category만 JSON으로 전송)
    @PostMapping(value = "/ongoing/filter", consumes = "application/json")
    public List<ChallengeCardResponse> filterOngoingChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChallengeFilterRequest request
    ) {
        return userChallengeService.filterOngoingChallenges(userDetails.getUserId(), request);
    }

    // 완료된 챌린지 조회
    @GetMapping("/view/completed")
    public List<ChallengeCardResponse> getCompleted(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userChallengeService.getCompletedChallenges(userDetails.getUserId());
    }

    // 완료된 챌린지 필터 (프론트는 category만 JSON으로 전송)
    @PostMapping(value = "/completed/filter", consumes = "application/json")
    public List<ChallengeCardResponse> filterCompletedChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChallengeFilterRequest request
    ) {
        return userChallengeService.filterCompletedChallenges(userDetails.getUserId(), request);
    }

    // 챌린지 가계부 연동/평가 API
    @PostMapping("/evaluate")
    public ResponseEntity<Void> evaluateAll(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userChallengeService.evaluateOngoingChallenges(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}