package com.moneybuddy.moneylog.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengeFilterRequest {
    private String type;              // "지출" / "저축" / "기타"
    private String category;          // 선택적: 예) "식비", "저축", "건강"
    private Boolean isAccountLinked;  // 선택적: 지출일 때만 의미 있음
}
