package com.moneybuddy.moneylog.data.dummy;

import java.util.LinkedHashMap;
import java.util.Map;

/** 카테고리별 월 합계 더미 (고정 값) */
public final class CategoryDummy {
    private CategoryDummy(){}

    /** key는 한글 라벨(식비/카페베이커리/교통/문화여가/의료건강/의류미용/기타) */
    public static Map<String, Long> monthCategoryTotals(int year, int month1to12) {
        // 보기 좋게 LinkedHashMap으로 순서 유지
        LinkedHashMap<String, Long> m = new LinkedHashMap<>();
        m.put("식비",         180_000L);
        m.put("카페베이커리",  60_000L);
        m.put("교통",          75_000L);
        m.put("문화여가",     120_000L);
        m.put("의료건강",      30_000L);
        m.put("의류미용",      95_000L);
        m.put("기타",          40_000L);
        return m;
    }

    /** 합계(지출 총액) */
    public static long sum(Map<String, Long> map){
        long s = 0L;
        if (map != null) for (Long v : map.values()) if (v != null) s += v;
        return s;
    }
}
