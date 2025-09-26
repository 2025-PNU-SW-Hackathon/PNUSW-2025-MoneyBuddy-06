package com.moneybuddy.moneylog.config;

import com.moneybuddy.moneylog.domain.Quiz;
import com.moneybuddy.moneylog.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class QuizDataLoader {

    private final QuizRepository quizRepository;

    @Bean
    public CommandLineRunner insertQuiz() {
        return args -> {
            // 넣고 싶은 문제들만 “질문/정답/설명”으로 정의
            record Seed(String q, boolean a, String e) {}

            List<Seed> seeds = List.of(
                    new Seed("예·적금 이자에는 세금이 부과된다.", true, "일반적으로 15.4%의 이자소득세가 부과됩니다."),
                    new Seed("신용카드는 무조건 빚이다.", false, "계획적으로 사용하면 혜택도 누릴 수 있습니다."),
                    new Seed("금융기관은 고객 동의 없이 신용정보를 열람할 수 있다.", false, "본인 동의가 있어야만 열람할 수 있습니다."),
                    new Seed("연금저축은 노후 준비와 세액공제를 동시에 할 수 있다.", true, "연간 400만 원 한도로 세액공제 받을 수 있습니다."),
                    new Seed("예금자 보호 한도는 금융기관당 최대 1억 원이다.", true, "원금과 이자를 합쳐 최대 1억 원까지 보호됩니다."),
                    new Seed("모든 체크카드는 신용점수에 영향을 준다.", false, "체크카드는 신용점수에 직접 영향을 주지 않습니다."),
                    new Seed("주식은 무조건 장기 보유가 유리하다.", false, "시장 상황에 따라 다르며 전략이 필요합니다."),
                    new Seed("적금은 중도해지해도 이자를 동일하게 받을 수 있다.", false, "중도해지 시 약정 이자보다 낮은 이자를 받습니다."),
                    new Seed("개인회생 중에도 신용카드를 만들 수 있다.", false, "제한이 있습니다."),
                    new Seed("비과세 금융상품은 무제한으로 가입할 수 있다.", false, "대부분 가입 한도가 존재합니다."),
                    new Seed("인터넷전문은행도 예금자보호를 받는다.", true, "인터넷은행도 보호 대상입니다."),
                    new Seed("ETF는 주식처럼 실시간 거래가 가능하다.", true, "ETF는 상장된 주식처럼 매매할 수 있습니다."),
                    new Seed("연체 이자가 일반 이자보다 낮다.", false, "연체 이자는 일반 이자보다 높습니다."),
                    new Seed("신용등급은 금융사마다 동일하다.", false, "평가기관 및 금융사마다 차이가 있습니다."),
                    new Seed("전세자금대출 이자는 연말정산에서 공제받을 수 있다.", true, "조건을 충족하면 소득공제 대상입니다."),
                    new Seed("금융소득이 연 2000만 원을 초과하면 종합과세 대상이다.", true, "2000만 원 초과 시 다른 소득과 합산 과세됩니다."),
                    new Seed("주택청약은 미성년자도 가입할 수 있다.", true, "가능합니다. 단, 일부 조건은 다를 수 있습니다."),
                    new Seed("대출을 많이 받으면 무조건 신용점수가 떨어진다.", false, "상환능력 대비 과도할 때 영향을 줍니다."),
                    new Seed("적금은 자유적금과 정기적금이 있다.", true, "자유롭게/정기적으로 넣는 방식이 있습니다."),
                    new Seed("비상금대출은 신용점수에 영향을 미치지 않는다.", false, "대출이므로 영향을 줄 수 있습니다."),
                    new Seed("파킹통장은 언제든 출금 가능한 고금리 상품이다.", true, "일부 파킹통장은 높은 이율과 수시입출금이 가능합니다."),
                    new Seed("카드론은 현금서비스보다 금리가 낮다.", false, "더 높은 경우도 많습니다."),
                    new Seed("연금보험은 해지 시 원금 손실이 있을 수 있다.", true, "해지 시 손해 가능성이 있습니다."),
                    new Seed("모든 보험은 소득공제 대상이다.", false, "일부 보험만 공제 대상입니다."),
                    new Seed("암보험은 진단만 받아도 보험금이 지급된다.", true, "보장 조건에 따라 진단 시 지급됩니다."),
                    new Seed("사회초년생은 신용등급이 낮은 것이 정상이다.", true, "신용 이력이 부족해 초기에 낮을 수 있습니다."),
                    new Seed("저축은행은 은행이 아니라 대부업이다.", false, "저축은행도 금융기관입니다."),
                    new Seed("금리가 오르면 대출이자는 내려간다.", false, "보통 대출이자도 함께 오릅니다."),
                    new Seed("개인신용평가는 정부에서 관리한다.", false, "신용평가 기관이 관리합니다."),
                    new Seed("ISA 계좌는 절세 혜택이 있다.", true, "비과세·분리과세 혜택이 있습니다."),
                    new Seed("주거래은행은 무조건 혜택이 많다.", false, "상품/조건에 따라 다릅니다.")
            );

            for (Seed s : seeds) {
                if (!quizRepository.existsByQuestion(s.q())) {
                    quizRepository.save(Quiz.builder()
                            .question(s.q())
                            .correctAnswer(s.a())
                            .explanation(s.e())
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }
        };
    }
}
