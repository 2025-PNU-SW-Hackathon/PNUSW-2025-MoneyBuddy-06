package com.moneybuddy.moneylog.dto.ledger;

public class LedgerCreateRequest {
    public String dateTime;
    public String entryType; // "INCOME" | "EXPENSE"
    public long amount;      // 항상 양수로 전송 !!
    public String asset;
    public String store;
    public String category;
    public String description;

    public static LedgerCreateRequest fromForm(String dateTime, String entryType,
                                               long userInputSignedAmount, // 사용자가 입력한 금액이 - 인 경우라도 절대값으로 보냄
                                               String asset, String store, String category, String description) {
        LedgerCreateRequest r = new LedgerCreateRequest();
        r.dateTime = dateTime;
        r.entryType = entryType;
        r.amount = Math.abs(userInputSignedAmount);
        r.asset = asset;
        r.store = store;
        r.category = category;
        r.description = description;
        return r;
    }
}
