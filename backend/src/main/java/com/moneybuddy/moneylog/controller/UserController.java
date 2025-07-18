package com.moneybuddy.moneylog.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @PostMapping("/signup")
    public Map<String, String> signup() {
        System.out.println("백엔드 회원가입 API 호출됨");

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "회원가입 요청이 성공적으로 서버에 도착했습니다!");
        return response;
    }
}
