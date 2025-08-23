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
    private String type;
    private String category;
    private String goalPeriod;
    private String goalType;
    private Integer goalValue;

    private Boolean isSystemGenerated;
    private Boolean isShared;
    private Boolean isAccountLinked;
    private Long createdBy;

    private String mobtiType;


}
