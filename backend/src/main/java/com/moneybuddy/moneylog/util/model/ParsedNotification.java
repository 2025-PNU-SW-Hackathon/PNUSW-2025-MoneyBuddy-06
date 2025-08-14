package com.moneybuddy.moneylog.util.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ParsedNotification {
    private BigDecimal amount;
    private String asset;
    private String store;
    private String category;
    private LocalDateTime dateTime;
}