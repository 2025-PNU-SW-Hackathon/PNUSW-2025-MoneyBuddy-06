package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.domain.UserChallengeSuccess;
import com.moneybuddy.moneylog.domain.UserExp;
import com.moneybuddy.moneylog.dto.request.ChallengeFilterRequest;
import com.moneybuddy.moneylog.dto.response.ChallengeCardResponse;
import com.moneybuddy.moneylog.dto.response.UserChallengeResponse;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeSuccessRepository;
import com.moneybuddy.moneylog.repository.UserExpRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserChallengeService {

    //  의존성 주입
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository userChallengeSuccessRepository;
    private final ChallengeLedgerService challengeLedgerService;
    private final UserExpRepository userExpRepository;

    /**
     *  챌린지 참여 요청 처리
     */
    public UserChallengeResponse joinChallenge(Long userId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        boolean alreadyJoined = userChallengeRepository.existsByUserIdAndChallengeId(userId, challengeId);
        if (alreadyJoined) {
            throw new IllegalStateException("이미 이 챌린지에 참여하고 있습니다.");
        }

        UserChallenge userChallenge = UserChallenge.builder()
                .userId(userId)
                .challenge(challenge)
                .joinedAt(LocalDateTime.now())
                .completed(false)
                .success(false)
                .rewarded(false)
                .build();
        userChallenge.setUserId(userId);
        userChallenge.setChallenge(challenge);
        userChallenge.setJoinedAt(LocalDateTime.now());

        userChallengeRepository.save(userChallenge);

        return toJoinChallengeResponse(userChallenge);
    }

    /**
     *  챌린지 참여 정보 응답
     */
    public UserChallengeResponse toJoinChallengeResponse(UserChallenge userChallenge) {
        Challenge c = userChallenge.getChallenge();

        return UserChallengeResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .goalPeriod(c.getGoalPeriod())
                .goalType(c.getGoalType())
                .goalValue(c.getGoalValue())
                .isAccountLinked(c.getIsAccountLinked())
                .isShared(c.getIsShared())
                .joinedAt(userChallenge.getJoinedAt())
                .completed(userChallenge.getCompleted())
                .rewarded(userChallenge.getRewarded())
                .build();
    }

    /**
     *  진행 중인 챌린지 조회
     */
    public List<ChallengeCardResponse> getOngoingChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndCompletedFalse(userId).stream()
                .map(this::toOngoingChallengeCardResponse)
                .collect(Collectors.toList());
    }

    /**
     *  진행 중 챌린지 정보 응답
     */
    private ChallengeCardResponse toOngoingChallengeCardResponse(UserChallenge uc) {
        Challenge c = uc.getChallenge();

        return ChallengeCardResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .goalPeriod(c.getGoalPeriod())
                .goalValue(c.getGoalValue())
                .completed(false)
                .success(null)
                .build();
    }

    /**
     *  완료된 챌린지 조회
     */
    public List<ChallengeCardResponse> getCompletedChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndCompletedTrue(userId).stream()
                .map(this::toCompletedChallengeCardResponse)
                .collect(Collectors.toList());
    }

    /**
     *  완료된 챌린지 정보 응답
     */
    private ChallengeCardResponse toCompletedChallengeCardResponse(UserChallenge uc) {
        Challenge c = uc.getChallenge();

        LocalDate start = uc.getJoinedAt().toLocalDate();
        LocalDate end = start.plusDays(parseGoalPeriod(c.getGoalPeriod()));

        long successCount = userChallengeSuccessRepository
                .countByUserIdAndChallengeIdAndSuccessDateBetween(
                        uc.getUserId(), c.getId(), start, end.minusDays(1)
                );

        boolean isSuccess = successCount >= c.getGoalValue();

        return ChallengeCardResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .goalPeriod(c.getGoalPeriod())
                .goalValue(c.getGoalValue())
                .completed(true)
                .success(isSuccess)
                .build();
    }

    /**
     *  진행 중인 챌린지 필터링
     */
    public List<ChallengeCardResponse> filterOngoingChallenges(Long userId, ChallengeFilterRequest request) {
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserIdAndCompletedFalse(userId);
        return filterOngoingChallengesByCondition(userChallenges, request);
    }

    /**
     *  진행 중인 챌린지 필터링 함수
     */
    private List<ChallengeCardResponse> filterOngoingChallengesByCondition(List<UserChallenge> userChallenges, ChallengeFilterRequest req) {
        return userChallenges.stream()
                .filter(uc -> {
                    Challenge c = uc.getChallenge();
                    if (!req.getType().equals(c.getType())) return false;

                    if ("저축".equals(req.getType())) {
                        return "저축".equals(c.getCategory());
                    } else if ("지출".equals(req.getType())) {
                        boolean cat = req.getCategory() == null || req.getCategory().equals(c.getCategory());
                        boolean acc = req.getIsAccountLinked() == null || req.getIsAccountLinked().equals(c.getIsAccountLinked());
                        return cat && acc;
                    }

                    return true;
                })
                .map(this::toOngoingChallengeCardResponse)
                .collect(Collectors.toList());
    }

    /**
     *  완료된 챌린지 필터링
     */
    public List<ChallengeCardResponse> filterCompletedChallenges(Long userId, ChallengeFilterRequest request) {
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserIdAndCompletedTrue(userId);
        return filterCompletedChallengesByCondition(userChallenges, request);
    }

    /**
     *  완료된 챌린지 필터링 함수
     */
    private List<ChallengeCardResponse> filterCompletedChallengesByCondition(List<UserChallenge> userChallenges, ChallengeFilterRequest req) {
        return userChallenges.stream()
                .filter(uc -> {
                    Challenge c = uc.getChallenge();
                    if (!req.getType().equals(c.getType())) return false;

                    if ("저축".equals(req.getType())) {
                        return "저축".equals(c.getCategory());
                    } else if ("지출".equals(req.getType())) {
                        boolean cat = req.getCategory() == null || req.getCategory().equals(c.getCategory());
                        boolean acc = req.getIsAccountLinked() == null || req.getIsAccountLinked().equals(c.getIsAccountLinked());
                        return cat && acc;
                    }

                    return true;
                })
                .map(this::toCompletedChallengeCardResponse)
                .collect(Collectors.toList());
    }


    /**
     *  가계부 연동 챌린지를 자동 평가 + 성공 시 완료 처리 + 보상 지급
     */
    @Transactional
    public void evaluateOngoingChallenges(Long userId) {
        List<UserChallenge> ongoing = userChallengeRepository.findByUserIdAndCompletedFalse(userId);

        for (UserChallenge uc : ongoing) {
            Challenge c = uc.getChallenge();

            if (c.getIsAccountLinked() == null || !c.getIsAccountLinked()) continue;

            boolean isSuccess = evaluateChallenge(c, userId);

            if (isSuccess) {
                uc.setCompleted(true);
                userChallengeRepository.save(uc); // 완료 처리

                // 성공 기록 저장
                userChallengeSuccessRepository.save(UserChallengeSuccess.builder()
                        .userId(userId)
                        .challenge(c)
                        .successDate(LocalDate.now())
                        .build()
                );

                // 보상 지급 로직
                giveRewardToUser(userId);
            }
        }
    }

    /**
     *  가계부 연동 챌린지가 자동으로 성공 처리됐을 때
     *  사용자에게 경험치 보상 지급 및 레벨업 처리
     */
    private void giveRewardToUser(Long userId) {
        UserExp exp = userExpRepository.findByUserId(userId)
                .orElseGet(() -> UserExp.builder().userId(userId).experience(0).level(1).build());

        int newExp = exp.getExperience() + 25;
        int newLevel = exp.getLevel();

        if (newExp >= 100) {
            newExp -= 100;
            newLevel++;
        }

        exp.setExperience(newExp);
        exp.setLevel(newLevel);
        userExpRepository.save(exp);
    }

    /**
     *  가계부 기반 챌린지 평가
     *  챌린지 제목에 따라 조건 분기
     */
    public boolean evaluateChallenge(Challenge challenge, Long userId) {
        if (!challenge.getIsAccountLinked()) return false;

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(parseGoalPeriod(challenge.getGoalPeriod()));

        BigDecimal goalValue = BigDecimal.valueOf(challenge.getGoalValue());

        switch (challenge.getTitle()) {

            case "무지출 데이 실천하기":
                long noSpendDays = challengeLedgerService.countNoSpendDays(start, end, userId);
                return noSpendDays >= goalValue.intValue();

            case "일주일에 10만원 내로 소비하기":
            case "일주일에 15만원 내로 소비하기":
                BigDecimal totalExpense = challengeLedgerService.sumExpenseOfCategory(start, end, userId, "전체");
                return totalExpense.compareTo(goalValue) <= 0;

            case "자주쓰는 카테고리에서 15000원 이하로 소비하기":
                String topCategory = challengeLedgerService.mostUsedCategory(start, end, userId);
                BigDecimal categoryExpense = challengeLedgerService.sumExpenseOfCategory(start, end, userId, topCategory);
                return categoryExpense.compareTo(goalValue) <= 0;

            case "계획대로 지출했는지 체크하기":
                long daysWithExpense = challengeLedgerService.countDaysUnderDailyBudget(start, end, userId, new BigDecimal("999999999"));
                return daysWithExpense >= goalValue.intValue();

            default:
                return false;
        }
    }

    /**
     * 챌린지 기간을 일(day) 단위로 환산
     */
    private int parseGoalPeriod(String periodStr) {
        if (periodStr.endsWith("일")) {
            return Integer.parseInt(periodStr.replace("일", "").trim());
        } else if (periodStr.endsWith("주")) {
            return Integer.parseInt(periodStr.replace("주", "").trim()) * 7;
        } else if (periodStr.endsWith("개월") || periodStr.endsWith("달")) {
            return Integer.parseInt(periodStr.replaceAll("개월|달", "").trim()) * 30;
        } else {
            throw new IllegalArgumentException("goalPeriod 형식이 잘못되었습니다: " + periodStr);
        }
    }

}
