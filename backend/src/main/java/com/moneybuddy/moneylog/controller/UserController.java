package com.moneybuddy.moneylog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.moneybuddy.moneylog.dto.UserSignupRequest;
import com.moneybuddy.moneylog.dto.UserSignupResponse;
import com.moneybuddy.moneylog.service.UserService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
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