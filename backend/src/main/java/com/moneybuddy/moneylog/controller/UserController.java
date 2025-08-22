package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.UserLoginRequest;
import com.moneybuddy.moneylog.dto.UserLoginResponse;
import com.moneybuddy.moneylog.dto.UserSignupRequest;
import com.moneybuddy.moneylog.dto.UserSignupResponse;
import com.moneybuddy.moneylog.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

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
}
