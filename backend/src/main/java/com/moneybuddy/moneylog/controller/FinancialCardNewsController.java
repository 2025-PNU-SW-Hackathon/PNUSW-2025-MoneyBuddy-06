package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.FinancialCardNewsResponse;
import com.moneybuddy.moneylog.service.FinancialCardNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/knowledge")
public class FinancialCardNewsController {

    private final FinancialCardNewsService service;

    // 오늘 날짜의 카드뉴스 여러 개 조회
    @GetMapping("/cardnews")
    public List<FinancialCardNewsResponse> getTodayKnowledgeList() {
        return service.getTodayKnowledgeList();
    }
}
