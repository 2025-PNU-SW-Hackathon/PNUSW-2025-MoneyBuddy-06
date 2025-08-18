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

    // mobti 추천용
    private String period;
    private String mobtiType;

    // 공통
    private String category;
    private Boolean isSystemGenerated;
    private Boolean isShared;

    // 사용자 챌린지용
    private String type;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;

    private Long createdBy;      // 사용자 ID
}
