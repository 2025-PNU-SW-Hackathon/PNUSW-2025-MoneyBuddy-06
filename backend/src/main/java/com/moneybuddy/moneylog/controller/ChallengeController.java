package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.ChallengeResponse;

import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/recommended")
    public List<ChallengeResponse> getRecommendedChallenges(@RequestParam String mobti) {
        return challengeService.getRecommendedChallenges(mobti);
    }
}
