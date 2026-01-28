package com.moneybuddy.moneylog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MobtiBriefDto {
    private String code;
    private String nickname;
    private String summary;
}