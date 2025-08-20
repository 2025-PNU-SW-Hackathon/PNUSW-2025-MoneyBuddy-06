package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.UserStatusResponse;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/status")
    public UserStatusResponse getUserStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return userService.getUserStatus(userId);
    }

}
