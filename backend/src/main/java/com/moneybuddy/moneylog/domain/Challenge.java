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

    @Column(nullable = false)
    private String title = "";

    @Column(nullable = false)
    private String description = "";

    @Column(nullable = false)
    private String type = "";

    @Column(nullable = false)
    private String category = "";

    @Column(nullable = false)
    private String goalPeriod = "";

    @Column(nullable = false)
    private String goalType = "";

    @Builder.Default
    @Column(nullable = false)
    private Integer goalValue = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean isSystemGenerated = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isShared = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isAccountLinked = false;

    @Column(nullable = false)
    private Long createdBy = 0L; 

    @Column(nullable = false)
    private String mobtiType = "";
}
