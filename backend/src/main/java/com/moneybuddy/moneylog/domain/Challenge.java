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

    private String title;               // 챌린지 제목
    private String description;         // 챌린지 설명
    private String type;                // 챌린지 유형: 지출 / 저축 / 기타
    private String goalPeriod;          // 목표 기간
    private String goalType;            // 목표 기준
    private Integer goalValue;          // 목표 값
    private Boolean isSystemGenerated;  // 시스템 생성 여부
    private Boolean isShared;           // 공유 여부
    private Boolean isAccountLinked;    // 가계부 연동 여부
    private Long createdBy;             // 생성자 (시스템이면 null)

    private String category;            // 사용자가 직접 입력한 카테고리 (식비, 카페 등)

    private String mobtiType;           // Mobti 추천 대상 유형 (예: "I", "E", "S" 등)


}
