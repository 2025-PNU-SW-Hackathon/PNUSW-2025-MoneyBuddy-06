package com.moneybuddy.moneylog.dto.response;

import com.moneybuddy.moneylog.domain.Ledger;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LedgerEntryDto {
    private Long id;
    private LocalDateTime dateTime;
    private BigDecimal amount;
    private String asset;
    private String store;
    private String category;
    private String description;
    private String entryType;

    public LedgerEntryDto(Ledger ledger) {
        this.id = ledger.getId();
        this.dateTime = ledger.getDateTime();
        this.amount = ledger.getAmount();
        this.asset = ledger.getAsset();
        this.store = ledger.getStore();
        this.category = ledger.getCategory();
        this.description = ledger.getDescription();

        var t = ledger.getEntryType();
        if (t == null && ledger.getAmount() != null) {
            t = (ledger.getAmount().signum() >= 0)
                    ? com.moneybuddy.moneylog.domain.EntryType.INCOME
                    : com.moneybuddy.moneylog.domain.EntryType.EXPENSE;
        }
        this.entryType = (t != null ? t.name() : null);
    }
}
