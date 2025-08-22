package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.ChallengeFilterRequest;
import com.moneybuddy.moneylog.dto.response.ChallengeDetailResponse;
import com.moneybuddy.moneylog.dto.response.RecommendedChallengeResponse;
import com.moneybuddy.moneylog.dto.request.UserChallengeRequest;
import com.moneybuddy.moneylog.dto.response.ChallengeCardResponse;
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

    // 사용자 챌린지 생성 API
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

    // MoBTI 유형 기반 챌린지 추천 API
    @GetMapping("/recommended/mobti")
    public List<RecommendedChallengeResponse> getRecommendedChallenges(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return challengeService.getRecommendedChallenges(userDetails.getUserId());
    }

    @PostMapping("/recommended/mobti/filter")
    public List<ChallengeCardResponse> filterMobtiChallenges(
            @RequestParam Long userId,
            @RequestBody ChallengeFilterRequest filterRequest) {
        return challengeService.filterMobtiRecommendedChallenges(userId, filterRequest);
    }

    // 공유 챌린지 리스트 조회 API
    @GetMapping("/shared")
    public List<ChallengeCardResponse> getChallengeBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return challengeService.getSharedChallenges(userDetails.getUserId());
    }

    @PostMapping("/shared/filter")
    public List<ChallengeCardResponse> filterSharedChallenges(@RequestBody ChallengeFilterRequest request) {
        return challengeService.filterSharedChallenges(request);
    }

    // 챌린지 상세 정보 조회
    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeDetailResponse> getChallengeDetail(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(challengeService.getChallengeDetail(challengeId, userDetails.getUserId()));
    }


}
