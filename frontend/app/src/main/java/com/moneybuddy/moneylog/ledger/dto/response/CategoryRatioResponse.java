package com.moneybuddy.moneylog.ledger.dto.response;

import java.util.List;

public class CategoryRatioResponse {
    public String yearMonth;
    public Long goalAmount;     // null 가능
    public long spent;          // 이 달 지출 합(양수)
    public boolean exceeded;    // 목표 초과 여부
    public String baseline;     // "GOAL" | "SPENT"
    public List<Item> items;

    public static class Item {
        public String category;
        public long expense;        // 카테고리 지출(양수)
        public double ratioPercent; // 소수점
    }
}
