package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "ledger",
        indexes = {
                @Index(name = "idx_ledger_user_datetime", columnList = "userId, dateTime")
        }
)
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDateTime dateTime;

    // 부호 있는 금액 (수입=양수, 지출=음수)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type")
    private EntryType entryType;

    @Column(nullable = false)
    private String asset;

    private String store;

    @Column(nullable = false)
    private String category;

    private String description;

    private LocalDateTime createdAt;
}