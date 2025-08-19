package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.dto.UserChallengeRequest;
import com.moneybuddy.moneylog.dto.UserChallengeResponse;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.UserChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-challenges")
public class UserChallengeController {

    private final UserChallengeService userChallengeService;

    @PostMapping("/join")
    public ResponseEntity<UserChallengeResponse> joinChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserChallengeRequest request
    ) {
        UserChallenge userChallenge = userChallengeService.joinChallenge(userDetails.getUserId(), request.getChallengeId());

        UserChallengeResponse response = UserChallengeResponse.builder()
                .userId(userChallenge.getUserId())
                .challengeId(userChallenge.getChallenge().getId())
                .status("참여완료")
                .joinedAt(userChallenge.getJoinedAt())
                .build();

        return ResponseEntity.ok(response);
    }
}
