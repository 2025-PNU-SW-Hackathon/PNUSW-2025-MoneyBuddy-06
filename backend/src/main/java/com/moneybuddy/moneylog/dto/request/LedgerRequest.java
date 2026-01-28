package com.moneybuddy.moneylog.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class LedgerRequest {
    private LocalDateTime dateTime;
    private BigDecimal amount;
    private String asset;
    private String store;
    private String category;
    private String description;
    private com.moneybuddy.moneylog.domain.EntryType entryType;
}
