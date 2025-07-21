package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.UserSignupRequest;
import com.moneybuddy.moneylog.dto.UserSignupResponse;
import com.moneybuddy.moneylog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public UserSignupResponse signup(@RequestBody UserSignupRequest request) {
        userService.signup(request);
        return new UserSignupResponse("success", "회원가입 성공!");
    }
}