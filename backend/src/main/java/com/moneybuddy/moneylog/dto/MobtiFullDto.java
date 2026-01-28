package com.moneybuddy.moneylog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MobtiFullDto {
    private String code;
    private String nickname;
    private String summary;
    private List<String> detailTraits;
    private List<String> spendingTendency;
    private List<String> socialStyle;
}
