package com.moneybuddy.moneylog.ledger.dto.request;

public class AutoLedgerRequest {
    public String message;
    public String receivedAt; // "YYYY-MM-DDTHH:mm:ss"

    public AutoLedgerRequest(String message, String receivedAt) {
        this.message = message;
        this.receivedAt = receivedAt;
    }
}
