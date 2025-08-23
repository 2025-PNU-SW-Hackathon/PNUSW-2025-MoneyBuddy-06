package com.moneybuddy.moneylog.config;

import com.moneybuddy.moneylog.domain.YouthPolicy;
import com.moneybuddy.moneylog.repository.YouthPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class YouthPolicyLoader implements CommandLineRunner {

    private final YouthPolicyRepository youthPolicyRepository;

    @Override
    public void run(String... args) {
        if (youthPolicyRepository.count() == 0) {

            youthPolicyRepository.save(
                    YouthPolicy.builder()
                            .title("청년도약계좌")
                            .applicationPeriod("매달 초 신청")
                            .amount("매월 1,000원~70만 원 자유 납입 (5년간)")
                            .eligibility("만 19~34세 이하 청년, 직전 과세기간 총 급여액 7,500만 원 이하, 기준 중위소득 250% 이하")
                            .benefit("소득 구간에 따라 월 최소 21,000원~최대 33,000원의 정부 기여금 이자를 지급")
                            .applicationMethod("시중 은행 방문 또는 인터넷뱅킹으로 신청")
                            .description("청년도약계좌는 청년의 자산형성을 지원하기 위한 목돈 마련 상품으로, 정부가 이자와 세제 혜택을 제공합니다.")
                            .url("https://www.example.com/youth-saving")
                            .build()
            );

            youthPolicyRepository.save(
                    YouthPolicy.builder()
                            .title("청년내일저축계좌")
                            .applicationPeriod("25.5.2~25.5.21 (25년도 기준)")
                            .amount("매월 10만 원 납입 (3년간)")
                            .benefit("3년간 총 360만 원 지원")
                            .eligibility("근로 중인 만 15~34세 청년, 월 평균 소득이 120만 원 이상, 중위소득 100% 이하 가구")
                            .applicationMethod("복지로 사이트에서 온라인 신청")
                            .description("자산 형성이 어려운 청년을 위해 근로와 저축을 병행하면 정부가 일정 금액을 지원해주는 제도")
                            .url("https://www.bokjiro.go.kr\n")
                            .build()
            );

            youthPolicyRepository.save(
                    YouthPolicy.builder()
                            .title("청년월세 특별지원")
                            .applicationPeriod("2025년 말까지 한시적 운영")
                            .amount("월세 최대 20만 원 지원 (최대 12개월)")
                            .benefit("월세 부담 경감, 소득 기준 충족 시 지원금 지급")
                            .eligibility("만 19~34세, 부모와 별도 거주, 중위소득 60% 이하")
                            .applicationMethod("복지로 또는 거주지 지자체 청년부서")
                            .description("청년 1인 가구의 주거비 부담 완화를 위해 일정 요건을 충족하면 월세를 일정 기간 지원해주는 제도")
                            .url("https://www.gov.kr")
                            .build()
            );
        }
    }
}
