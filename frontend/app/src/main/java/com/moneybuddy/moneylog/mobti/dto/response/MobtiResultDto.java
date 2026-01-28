package com.moneybuddy.moneylog.mobti.dto.response;

import java.util.Map;

public class MobtiResultDto {
    private Long userId;
    private String email;
    private String mobti;                 // ex) "EMCP"
    private Map<String, Integer> counts;  // I/E/M/S/T/C/P/R
    private String mobtiUpdatedAt;        // 백엔드 LocalDateTime -> 문자열로 받기

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getMobti() { return mobti; }
    public Map<String, Integer> getCounts() { return counts; }
    public String getMobtiUpdatedAt() { return mobtiUpdatedAt; }
}
