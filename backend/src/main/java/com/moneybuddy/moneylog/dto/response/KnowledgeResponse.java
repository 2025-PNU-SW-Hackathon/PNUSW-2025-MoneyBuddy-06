package com.moneybuddy.moneylog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;


@Getter
@AllArgsConstructor
public class KnowledgeResponse {
    private String title;
    private String content;
    private LocalDate date;
}
