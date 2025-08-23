package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.dto.UserProfileDto;
import com.moneybuddy.moneylog.dto.request.UserLoginRequest;
import com.moneybuddy.moneylog.dto.response.UserLoginResponse;
import com.moneybuddy.moneylog.dto.request.UserSignupRequest;
import com.moneybuddy.moneylog.dto.response.UserSignupResponse;
import com.moneybuddy.moneylog.dto.request.UserDeleteRequest;
import com.moneybuddy.moneylog.dto.response.ScoreResponse;
import com.moneybuddy.moneylog.service.UserService;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.MobtiService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final MobtiService mobtiService;

    @GetMapping("/profile")
    public UserProfileDto profile(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("인증 정보가 없습니다. 로그인 후 시도하세요.");
        }
        return mobtiService.loadProfile(principal.getUserId());
    }

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signup(@RequestBody UserSignupRequest request) {
        try {
            userService.signup(request);
            return ResponseEntity.ok(new UserSignupResponse("success", "회원가입 성공!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new UserSignupResponse("fail", e.getMessage()));
        }
    }
  
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            UserLoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new UserLoginResponse("로그인 실패: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@Valid @RequestBody UserDeleteRequest request,
                                             HttpServletRequest httpServletRequest) {
        try {
            userService.deleteUser(request, httpServletRequest);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("회원 탈퇴 실패: " + e.getMessage());
        }
    }
  
    @GetMapping("/score")
    public ResponseEntity<ScoreResponse> getScore(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(
                ScoreResponse.builder()
                        .score(user.getScore())
                        .build()
        );
    }
}