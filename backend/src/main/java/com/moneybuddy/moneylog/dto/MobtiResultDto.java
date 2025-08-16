package com.moneybuddy.moneylog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class MobtiResultDto {
    private Long userId;
    private String email;
    private String mobti;
    private Map<String, Integer> counts;
    private LocalDateTime mobtiUpdatedAt;
}
