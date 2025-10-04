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
        saveIfNotExists("마음을 달래는 힐링타임",
                Challenge.builder()
                        .title("마음을 달래는 힐링타임")
                        .description("감정소비는 꾹참고, 산책이나 글쓰기처럼 말을 편하게 해주는 활동하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("I")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("중고 먼저 보기 습관",
                Challenge.builder()
                        .title("중고 먼저 보기 습관")
                        .description("중고앱이나 최저가 사이트 먼저 찾아보기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("I")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("좋아하는 건 소소하게",
                Challenge.builder()
                        .title("좋아하는 건 소소하게")
                        .description("자주쓰는 카테고리에서 15000원 이하로 소비하기")
                        .goalPeriod("1주")
                        .goalType("금액")
                        .goalValue(15000)
                        .mobtiType("I")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(0L)
                        .build());

        // E형
        saveIfNotExists("리뷰 달고 나눔왕 되기",
                Challenge.builder()
                        .title("리뷰 달고 나눔왕 되기")
                        .description("구매한 사이트에 실용적인 리뷰 남기기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("E")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("쿠폰 챙겨 알뜰천사",
                Challenge.builder()
                        .title("쿠폰 챙겨 알뜰천사")
                        .description("쿠폰이나 포인트를 활용해서 할인 받기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("E")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("최저가로 득템 성공",
                Challenge.builder()
                        .title("최저가로 득템 성공")
                        .description("최저가 비교를 통해 1000원 이상 절약해보기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("E")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());




        // M형
        saveIfNotExists("월급 30% 금고 속으로",
                Challenge.builder()
                        .title("월급 30% 금고 속으로")
                        .description("한 달 수입의 30%를 저축하기")
                        .goalPeriod("1달")
                        .goalType("금액")
                        .goalValue(300000)
                        .mobtiType("M")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());
      
        saveIfNotExists("무지출 데이 도전",
                Challenge.builder()
                        .title("무지출 데이 도전")
                        .description("무지출 데이’ 실천하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("M")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("참은 소비, 저금통 뿅",
                Challenge.builder()
                        .title("참은 소비, 저금통 뿅")
                        .description("사고 싶었지만 참은 물건들, 그 물건 가격만큼 저축하기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("M")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        // S형
        saveIfNotExists("20%는 미래 통통",
                Challenge.builder()
                        .title("20%는 미래 통통")
                        .description("한 달 수입의 20% 저축하기")
                        .goalPeriod("1달")
                        .goalType("금액")
                        .goalValue(0)
                        .mobtiType("S")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("안 산 물건값 저금통",
                Challenge.builder()
                        .title("안 산 물건값 저금통")
                        .description("사지 않은 물건 대신 그 가격만큼 저축으로 보상해주기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(2)
                        .mobtiType("S")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("일주일 15만 지키기",
                Challenge.builder()
                        .title("일주일 15만 지키기")
                        .description("일주일에 15만원 내로 소비하기")
                        .goalPeriod("1주")
                        .goalType("금액")
                        .goalValue(150000)
                        .mobtiType("S")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(0L)
                        .build());

        // T형
        saveIfNotExists("한 달 뒤에도 쓸까?",
                Challenge.builder()
                        .title("한 달 뒤에도 쓸까?")
                        .description("유행 아이템을 구매하기 전 ‘한 달 뒤에도 쓸까?’ 라는 질문해보기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("T")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("SNS 유혹, 잠깐 뿅",
                Challenge.builder()
                        .title("SNS 유혹, 잠깐 뿅")
                        .description("SNS에서 본 소비 추천을 참아보기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(7)
                        .mobtiType("T")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("집에 있나 확인 먼저",
                Challenge.builder()
                        .title("집에 있나 확인 먼저")
                        .description("같은 종류의 물건이 있는지 먼저 확인하고 없을 경우에만 구매하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("T")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        // C형
        saveIfNotExists("다른 브랜드도 탐험",
                Challenge.builder()
                        .title("다른 브랜드도 탐험")
                        .description("평소에 사용하던 제품과 다른 브랜드 제품을 비교해보기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("C")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("리뷰 3개는 기본이지",
                Challenge.builder()
                        .title("리뷰 3개는 기본이지")
                        .description("리뷰 3개 이상 확인하고 구매 결정하기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(2)
                        .mobtiType("C")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        // P형
        saveIfNotExists("작은 비상금 챙겨두기",
                Challenge.builder()
                        .title("작은 비상금 챙겨두기")
                        .description("예상 못한 상황을 대비해서 비상금 이체하기")
                        .goalPeriod("2주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("P")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("소비 계획 다시 점검",
                Challenge.builder()
                        .title("소비 계획 다시 점검")
                        .description("소비 계획을 다시 확인하고, 새로운 할인 정보나 필요에 따라 조정하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("P")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("오늘 지출 체크 완료",
                Challenge.builder()
                        .title("오늘 지출 체크 완료")
                        .description("계획대로 지출했는지 체크하기")
                        .goalPeriod("1주")
                        .goalType("횟수")
                        .goalValue(7)
                        .mobtiType("P")
                        .type("습관")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("고정비 줄이는 습관",
                Challenge.builder()
                        .title("고정비 줄이는 습관")
                        .description("지난달 지출 중에서 줄일 수 있는 고정비 항목 찾아보기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(1)
                        .mobtiType("P")
                        .type("저축")
                        .category("저축")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
                        .build());

        // R형
        saveIfNotExists("하루만 참아보기",
                Challenge.builder()
                        .title("하루만 참아보기")
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
                        .createdBy(0L)
                        .build());

        saveIfNotExists("주간 10만 챌린지",
                Challenge.builder()
                        .title("주간 10만 챌린지")
                        .description("일주일에 10만원 내로 소비하기")
                        .goalPeriod("1주일")
                        .goalType("금액")
                        .goalValue(100000)
                        .mobtiType("R")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(true)
                        .createdBy(0L)
                        .build());

        saveIfNotExists("사기 전 계획 세우기",
                Challenge.builder()
                        .title("사기 전 계획 세우기")
                        .description("소비 전 간단한 구매 계획을 세우고 소비하기")
                        .goalPeriod("1달")
                        .goalType("횟수")
                        .goalValue(3)
                        .mobtiType("R")
                        .type("지출")
                        .category("기타")
                        .isSystemGenerated(true)
                        .isShared(false)
                        .isAccountLinked(false)
                        .createdBy(0L)
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
