package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouthPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String applicationPeriod;

    @Column(columnDefinition = "TEXT")
    private String amount;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    @Column(columnDefinition = "TEXT")
    private String benefit;

    @Column(columnDefinition = "TEXT")
    private String applicationMethod;


    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String url;

}
