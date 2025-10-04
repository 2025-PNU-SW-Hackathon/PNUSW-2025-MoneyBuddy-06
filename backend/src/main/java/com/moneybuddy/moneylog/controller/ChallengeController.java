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
@RequestMapping(value = "/api/v1/challenges", produces = "application/json")
public class ChallengeController {

    private final ChallengeService challengeService;

    // 사용자 챌린지 생성 API
    @PostMapping(value = "/create", consumes = "application/json")
    public ResponseEntity<String> createUserChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserChallengeRequest request
    ) {
        challengeService.createUserChallenge(userDetails.getUserId(), request);
        return ResponseEntity.ok("챌린지 생성 완료!");
    }

    // MoBTI 유형 기반 챌린지 추천 API
    @GetMapping("/recommended/mobti")
    public List<RecommendedChallengeResponse> getRecommendedChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return challengeService.getRecommendedChallenges(userDetails.getUserId());
    }

    // MoBTI 기반 추천 필터
    @PostMapping(value = "/recommended/mobti/filter", consumes = "application/json")
    public List<ChallengeCardResponse> filterMobtiChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChallengeFilterRequest filterRequest
    ) {
        return challengeService.filterMobtiRecommendedChallenges(userDetails.getUserId(), filterRequest);
    }

    // 공유 챌린지 전체(게시판) 조회
    @GetMapping("/shared")
    public List<ChallengeCardResponse> getChallengeBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return challengeService.getSharedChallenges(userDetails.getUserId());
    }

    // 공유 챌린지 필터
    @PostMapping(value = "/shared/filter", consumes = "application/json")
    public List<ChallengeCardResponse> filterSharedChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChallengeFilterRequest request
    ) {
        return challengeService.filterSharedChallenges(userDetails.getUserId(), request);
    }

    // 챌린지 상세
    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeDetailResponse> getChallengeDetail(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                challengeService.getChallengeDetail(challengeId, userDetails.getUserId())
        );
    }
}
