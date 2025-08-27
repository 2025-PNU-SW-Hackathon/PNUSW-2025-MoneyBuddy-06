package com.moneybuddy.moneylog.ledger.dto.response;

import java.util.List;

public class CategoryRatioDto {
    public String yearMonth;
    public Long goalAmount; // null 가능
    public long spent;
    public boolean exceeded;
    public String baseline; // "GOAL"|"SPENT"
    public List<Item> items;

    public static class Item{
        public String category;
        public long expense;
        public double ratioPercent;
    }
}
