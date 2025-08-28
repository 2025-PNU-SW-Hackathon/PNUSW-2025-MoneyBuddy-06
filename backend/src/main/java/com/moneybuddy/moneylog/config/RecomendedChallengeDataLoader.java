package com.moneybuddy.moneylog.config;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecomendedChallengeDataLoader implements CommandLineRunner {

    private final ChallengeRepository challengeRepository;

    @Override
    public void run(String... args) {
        // I형
        saveIfNotExists("감정소비는 꾹참고, 산책이나 글쓰기처럼 말을 편하게 해주는 활동하기",
                Challenge.builder()
                        .title("감정소비는 꾹참고, 산책이나 글쓰기처럼 말을 편하게 해주는 활동하기")
                        .description("감정소비를 대신해 마음이 편해지는 활동을 해보세요")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("I")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("구매하기 전, 중고앱이나 최저가 사이트를 먼저 찾아보기",
                Challenge.builder()
                        .title("구매하기 전, 중고앱이나 최저가 사이트를 먼저 찾아보기")
                        .description("즉흥적 구매 전에 가격을 비교해보는 습관 들이기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("I")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("자주쓰는 카테고리에서 15000원 이하로 소비하기",
                Challenge.builder()
                        .title("자주쓰는 카테고리에서 15000원 이하로 소비하기")
                        .description("특정 소비 카테고리에서 절약하기 (가계부 연동)")
                        .goalPeriod("1주")
                        .goalType("금액")
                        .goalValue(15000)
                        .mobtiType("I")
                        .type("지출")
                        .category("카테고리")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(null)
                        .build());

        // E형
        saveIfNotExists("구매한 사이트에 실용적인 리뷰 남기기",
                Challenge.builder()
                        .title("구매한 사이트에 실용적인 리뷰 남기기")
                        .description("나의 소비 경험을 공유해 타인에게도 도움되게 하기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("E")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("쿠폰이나 포인트를 활용해서 할인 받기",
                Challenge.builder()
                        .title("쿠폰이나 포인트를 활용해서 할인 받기")
                        .description("쌓인 포인트, 쿠폰을 적극 활용해 소비 습관 개선")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("E")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("최저가 비교를 통해 1000원 이상 절약해보기",
                Challenge.builder()
                        .title("최저가 비교를 통해 1000원 이상 절약해보기")
                        .description("동일 상품이라면 더 저렴하게 사는 습관 들이기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("E")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());




        // M형
        saveIfNotExists("한 달 수입의 30%를 저축하기",
                Challenge.builder()
                        .title("한 달 수입의 30%를 저축하기")
                        .description("수입의 30%를 자동이체 또는 따로 관리하며 저축하기")
                        .goalPeriod("1달")
                        .goalType("금액")
                        .goalValue(300000)
                        .mobtiType("M")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());
      
        saveIfNotExists("‘무지출 데이’ 실천하기",
                Challenge.builder()
                        .title("‘무지출 데이’ 실천하기")
                        .description("일주일에 하루는 무조건 무지출로 버텨보기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("M")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(null)
                        .build());

        saveIfNotExists("사고 싶었지만 참은 물건들, 그 물건 가격만큼 저축하기",
                Challenge.builder()
                        .title("사고 싶었지만 참은 물건들, 그 물건 가격만큼 저축하기")
                        .description("소비를 참은 만큼 나에게 저축으로 보상하기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("M")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        // S형
        saveIfNotExists("한 달 수입의 20% 저축하기",
                Challenge.builder()
                        .title("한 달 수입의 20% 저축하기")
                        .description("소비형이라도 기본 저축 습관 만들기")
                        .goalPeriod("1달")
                        .goalType("금액")
                        .goalValue(0)
                        .mobtiType("S")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("사지 않은 물건 대신 그 가격만큼 저축으로 보상해주기",
                Challenge.builder()
                        .title("사지 않은 물건 대신 그 가격만큼 저축으로 보상해주기")
                        .description("무언가를 안 샀을 때의 성취감을 저축으로 연결하기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(2)
                        .mobtiType("S")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("일주일에 15만원 내로 소비하기",
                Challenge.builder()
                        .title("일주일에 15만원 내로 소비하기")
                        .description("정해진 일주일 소비 한도를 지켜보기")
                        .goalPeriod("1주")
                        .goalType("금액")
                        .goalValue(150000)
                        .mobtiType("S")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(null)
                        .build());

        // T형
        saveIfNotExists("유행 아이템을 구매하기 전 ‘한 달 뒤에도 쓸까?’ 라는 질문해보기",
                Challenge.builder()
                        .title("유행 아이템을 구매하기 전 ‘한 달 뒤에도 쓸까?’ 라는 질문해보기")
                        .description("충동구매 전 자기 점검하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("T")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("SNS에서 본 소비 추천을 참아보기",
                Challenge.builder()
                        .title("SNS에서 본 소비 추천을 참아보기")
                        .description("소셜 미디어의 유혹에서 벗어나기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(7)
                        .mobtiType("T")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("같은 종류의 물건이 있는지 먼저 확인하고 없을 경우에만 구매하기",
                Challenge.builder()
                        .title("같은 종류의 물건이 있는지 먼저 확인하고 없을 경우에만 구매하기")
                        .description("중복 소비 방지하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("T")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        // C형
        saveIfNotExists("평소에 사용하던 제품과 다른 브랜드 제품을 비교해보기",
                Challenge.builder()
                        .title("평소에 사용하던 제품과 다른 브랜드 제품을 비교해보기")
                        .description("새로운 브랜드에 대한 탐색 기회 가지기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("C")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("리뷰 3개 이상 확인하고 구매 결정하기",
                Challenge.builder()
                        .title("리뷰 3개 이상 확인하고 구매 결정하기")
                        .description("정보에 기반한 소비 습관 만들기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(2)
                        .mobtiType("C")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        // P형
        saveIfNotExists("예상 못한 상황을 대비해서 비상금 이체하기",
                Challenge.builder()
                        .title("예상 못한 상황을 대비해서 비상금 이체하기")
                        .description("계획형도 위기를 대비하자")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("P")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("소비 계획을 다시 확인하고, 새로운 할인 정보나 필요에 따라 조정하기",
                Challenge.builder()
                        .title("소비 계획을 다시 확인하고, 새로운 할인 정보나 필요에 따라 조정하기")
                        .description("계획대로 지출되었는지 점검하고 업데이트하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("P")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("계획대로 지출했는지 체크하기",
                Challenge.builder()
                        .title("계획대로 지출했는지 체크하기")
                        .description("예산 관리 실천 여부 확인하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(7)
                        .mobtiType("P")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(null)
                        .build());

        saveIfNotExists("지난달 지출 중에서 줄일 수 있는 고정비 항목 찾아보기",
                Challenge.builder()
                        .title("지난달 지출 중에서 줄일 수 있는 고정비 항목 찾아보기")
                        .description("지출 리포트를 바탕으로 불필요한 고정비 줄이기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("P")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        // R형
        saveIfNotExists("물건을 사기 전, 하루만 고민해보기",
                Challenge.builder()
                        .title("물건을 사기 전, 하루만 고민해보기")
                        .description("최소 하루 고민 후 구매 결정하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("R")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());

        saveIfNotExists("일주일에 10만원 내로 소비하기",
                Challenge.builder()
                        .title("일주일에 10만원 내로 소비하기")
                        .description("일주일 예산 설정과 지키기 연습")
                        .goalPeriod("1주일")
                        .goalType("금액")
                        .goalValue(100000)
                        .mobtiType("R")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(null)
                        .build());

        saveIfNotExists("소비 전 간단한 구매 계획을 세우고 소비하기",
                Challenge.builder()
                        .title("소비 전 간단한 구매 계획을 세우고 소비하기")
                        .description("구매 전에 간단한 계획을 세워 충동 소비 줄이기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("R")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(null)
                        .build());
    }

    /**
     * 같은 제목(title)이 이미 존재하면 저장하지 않음 (중복 방지)
     */
    private void saveIfNotExists(String title, Challenge challenge) {
        if (!challengeRepository.existsByTitle(title)) {
            challengeRepository.save(challenge);
        }
    }
}
