package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String period;

    private String mobtiType;
    private String category;

    private Boolean isSystemGenerated;
    private Boolean isShared;

    private Long createdBy;
}
