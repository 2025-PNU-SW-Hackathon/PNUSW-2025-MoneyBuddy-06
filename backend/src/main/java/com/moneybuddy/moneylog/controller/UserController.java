package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.UserLoginRequest;
import com.moneybuddy.moneylog.dto.UserLoginResponse;
import com.moneybuddy.moneylog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        try {
            userService.login(request);
            return ResponseEntity.ok(new UserLoginResponse("success", "로그인 성공!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new UserLoginResponse("fail", e.getMessage()));
        }
    }
}

