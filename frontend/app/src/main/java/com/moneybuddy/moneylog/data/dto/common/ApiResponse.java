package com.moneybuddy.moneylog.data.dto.common;

import com.moneybuddy.moneylog.data.dto.ledger.LedgerEntryDto;

public class ApiResponse<T> {
    public String status; // "success"
    public T entry;       // /api/ledger (create/update 응답 본문)
    public T parsed;      // /api/ledger/auto 응답 본문
    public T data;
}
