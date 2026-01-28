package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "mobti")
public class MobtiInfo {

    @Id
    @Column(length = 8)
    private String code;  // 영어 약자

    @Column(nullable = false, length = 100)
    private String nickname;   // 한글 닉네임

    @Column(name = "summary_short", nullable = false, length = 255)
    private String summaryShort;   // 한 줄 요약

    @Lob
    @Column(name = "description_long", nullable = false, columnDefinition = "TEXT")
    private String detailTraits;   // 상세 특성

    @Lob
    @Column(name = "spending_tendency", nullable = false, columnDefinition = "TEXT")
    private String spendingTendency;   // 나의 소비 성향

    @Lob
    @Column(name = "social_style", nullable = false, columnDefinition = "TEXT")
    private String socialStyle;   // 친구/지인 관계 속 소비 스타일
}
