package com.moneybuddy.moneylog.controller;


import com.moneybuddy.moneylog.dto.response.UserExpResponse;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.UserExpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class UserExpController {

    private final UserExpService userExpService;

    //
    @GetMapping("/exp")
    public UserExpResponse getMyExp(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userExpService.getUserExp(userDetails.getUserId());
    }
}
