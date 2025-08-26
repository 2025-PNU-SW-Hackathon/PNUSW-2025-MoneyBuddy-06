package com.moneybuddy.moneylog.ledger.model;

public class Transaction {

    public enum Type { INCOME, EXPENSE }

    private final String time;      // "HH:mm"
    private final String title;     // 메모/상호명
    private final String category;  // 카테고리(식비/교통/수입 등)
    private final String asset;     // 자산(카드/현금/계좌 등)  ★ 추가
    private final int amount;       // 지출은 음수, 수입은 양수(또는 절대값+Type로 처리)
    private final Type type;
    private final String groupId;   // 화면 섹션/더미용 (기존 "A" 등)

    // === 새 생성자: 카테고리 + 자산을 모두 받음 ===
    public Transaction(String time, String title, String category, String asset,
                       int amount, Type type, String groupId) {
        this.time = time;
        this.title = title;
        this.category = category;
        this.asset = asset;
        this.amount = amount;
        this.type = type;
        this.groupId = groupId;
    }

    // === 기존 호출 코드가 있다면 깨지지 않게 유지(자산을 빈 문자열로) ===
    public Transaction(String time, String title, String categoryOrAsset,
                       int amount, Type type, String groupId) {
        this(time, title, categoryOrAsset, /*asset*/"", amount, type, groupId);
    }

    // getters
    public String getTime() { return time; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getAsset() { return asset; }         // ★ 추가
    public int getAmount() { return amount; }
    public Type getType() { return type; }
    public String getGroupId() { return groupId; }
}
