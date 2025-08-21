package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.KnowledgeResponse;
import com.moneybuddy.moneylog.service.FinancialKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/knowledge")
public class FinancialKnowledgeController {

    private final FinancialKnowledgeService service;

    // GET /api/v1/knowledge/cardnews
    // 오늘 날짜의 카드뉴스 여러 개 조회
    @GetMapping("/cardnews")
    public List<KnowledgeResponse> getTodayKnowledgeList() {
        return service.getTodayKnowledgeList();
    }
}
