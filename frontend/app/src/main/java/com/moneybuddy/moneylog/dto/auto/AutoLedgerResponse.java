package com.moneybuddy.moneylog.dto.auto;

import com.moneybuddy.moneylog.dto.ledger.LedgerEntryDto;

public class AutoLedgerResponse {
    public String status;           // "success"
    public LedgerEntryDto parsed;   // 서버가 파싱한 결과
}
