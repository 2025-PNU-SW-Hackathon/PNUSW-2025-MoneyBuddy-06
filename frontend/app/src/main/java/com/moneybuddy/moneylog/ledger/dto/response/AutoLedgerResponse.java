package com.moneybuddy.moneylog.ledger.dto.response;

public class AutoLedgerResponse {
    public String status;           // "success"
    public LedgerEntryDto parsed;   // 서버가 파싱한 결과
}
