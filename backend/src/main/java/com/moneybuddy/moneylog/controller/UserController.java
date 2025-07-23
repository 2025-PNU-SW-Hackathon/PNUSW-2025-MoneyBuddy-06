package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.MobtiRequestDto;
import com.moneybuddy.moneylog.dto.UserProfileDto;
import com.moneybuddy.moneylog.security.JwtUtil;
import com.moneybuddy.moneylog.service.MobtiService;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final MobtiService mobtiService;
    private final JwtUtil jwtUtil;

    public UserController(MobtiService mobtiService, JwtUtil jwtUtil) {
        this.mobtiService = mobtiService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/save")
    public UserProfileDto submitMobti(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MobtiRequestDto dto) {

        String token = authHeader.replace("Bearer", "");
        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);

        return mobtiService.saveResult(userId, dto);
    }
}
