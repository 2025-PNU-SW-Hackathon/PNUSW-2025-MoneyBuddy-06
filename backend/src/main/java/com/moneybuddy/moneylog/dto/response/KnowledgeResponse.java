package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 클라이언트에게 응답할 데이터 형태 정의
@Getter
@AllArgsConstructor
public class KnowledgeResponse {
    private String title;
    private String content;
}
