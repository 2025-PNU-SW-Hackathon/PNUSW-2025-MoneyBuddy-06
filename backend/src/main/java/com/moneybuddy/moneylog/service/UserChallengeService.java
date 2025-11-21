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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserChallengeService {

    // 의존성 주입
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository userChallengeSuccessRepository;
    private final ChallengeLedgerService challengeLedgerService;
    private final UserExpRepository userExpRepository;

    /**
     * 챌린지 참여
     */
    @Transactional
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

        userChallengeRepository.save(userChallenge);

        return toJoinChallengeResponse(userChallenge);
    }

    /**
     * 챌린지 참여 응답 DTO 변환
     */
    private UserChallengeResponse toJoinChallengeResponse(UserChallenge userChallenge) {
        Challenge c = userChallenge.getChallenge();

        return UserChallengeResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .goalPeriod(c.getGoalPeriod())
                .goalType(c.getGoalType())
                .goalValue(c.getGoalValue())
                .isAccountLinked(c.isAccountLinked())
                .isShared(c.isShared())
                .joinedAt(userChallenge.getJoinedAt())
                .completed(userChallenge.isCompleted())
                .rewarded(userChallenge.isRewarded())
                .build();
    }

    /**
     * 진행 중인 챌린지 조회
     * - 이 메서드는 "조회 전용"으로 사용한다.
     * - completed 상태를 변경하지 않고, 현재 DB 상태 그대로를 읽어온다.
     * - 오늘 성공 여부(todaySuccess)는 UserChallengeSuccess 테이블을 기준으로 계산한다.
     */
    @Transactional(readOnly = true)
    public List<ChallengeCardResponse> getOngoingChallenges(Long userId) {
        // 사용자 기준, completed=false 이고 Challenge까지 fetch join 된 참여 목록
        List<UserChallenge> userChallenges =
                userChallengeRepository.findByUserIdAndCompletedFalseWithChallenge(userId);

        return userChallenges.stream()
                .map(uc -> toOngoingChallengeCardResponse(uc, userId))
                .toList();
    }

    /**
     * 진행 중 챌린지 카드 응답 변환
     * - 기간(start~end) 계산
     * - 기간 내 누적 성공 횟수(successCount) 계산 (필요 시 확장 가능)
     * - 오늘 성공 여부(todaySuccess) 계산
     */
    private ChallengeCardResponse toOngoingChallengeCardResponse(UserChallenge uc, Long userId) {
        Challenge c = uc.getChallenge();

        // 오늘 날짜 (KST 기준)
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 참여 시작일을 KST 기준 LocalDate로 변환
        LocalDate start = uc.getJoinedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
        LocalDate end = start.plusDays(parseGoalPeriod(c.getGoalPeriod()));

        // 기간 내 누적 성공 횟수 (필요하면 DTO에 필드 추가해서 내려줄 수 있다)
        long successCount = userChallengeSuccessRepository
                .countByUserIdAndChallenge_IdAndSuccessDateBetween(
                        uc.getUserId(), c.getId(), start, end.minusDays(1)
                );

        // 오늘 하루 성공 여부 (체크박스 상태)
        boolean todaySuccess = userChallengeSuccessRepository
                .existsByUserIdAndChallenge_IdAndSuccessDate(
                        uc.getUserId(), c.getId(), today
                );

        return ChallengeCardResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .category(c.getCategory())
                .goalPeriod(c.getGoalPeriod())
                .goalType(c.getGoalType())
                .goalValue(c.getGoalValue())
                .mobtiType(c.getMobtiType())
                .isSystemGenerated(c.isSystemGenerated())
                .isAccountLinked(c.isAccountLinked())
                .isShared(c.isShared())
                .joinedAt(uc.getJoinedAt())
                .completed(uc.isCompleted())
                .success(uc.isSuccess())
                .rewarded(uc.isRewarded())
                .isJoined(true)
                .mine(c.getCreatedBy() != null && c.getCreatedBy().equals(userId))
                .todaySuccess(todaySuccess) // 프론트 체크박스는 이 필드를 기준으로 사용
                .build();
    }

    /**
     * 완료된 챌린지 조회
     */
    @Transactional(readOnly = true)
    public List<ChallengeCardResponse> getCompletedChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndCompletedTrueWithChallenge(userId).stream()
                .map(uc -> toCompletedChallengeCardResponse(uc, userId))
                .toList();
    }

    /**
     * 완료된 챌린지 카드 응답 변환
     */
    private ChallengeCardResponse toCompletedChallengeCardResponse(UserChallenge uc, Long userId) {
        Challenge c = uc.getChallenge();

        LocalDate start = uc.getJoinedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
        LocalDate end = start.plusDays(parseGoalPeriod(c.getGoalPeriod()));

        long successCount = userChallengeSuccessRepository
                .countByUserIdAndChallenge_IdAndSuccessDateBetween(
                        uc.getUserId(), c.getId(), start, end.minusDays(1)
                );

        return ChallengeCardResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .category(c.getCategory())
                .goalPeriod(c.getGoalPeriod())
                .goalType(c.getGoalType())
                .goalValue(c.getGoalValue())
                .mobtiType(c.getMobtiType())
                .isSystemGenerated(c.isSystemGenerated())
                .isAccountLinked(c.isAccountLinked())
                .isShared(c.isShared())
                .joinedAt(uc.getJoinedAt())
                .completed(uc.isCompleted())
                .success(uc.isSuccess())
                .rewarded(uc.isRewarded())
                .isJoined(true)
                .mine(c.getCreatedBy() != null && c.getCreatedBy().equals(userId))
                .build();
    }

    /**
     * 진행 중인 챌린지 필터링
     * - 카테고리 조건으로 필터링
     * - todaySuccess 계산은 toOngoingChallengeCardResponse()에서 공통 처리
     */
    @Transactional(readOnly = true)
    public List<ChallengeCardResponse> filterOngoingChallenges(Long userId, ChallengeFilterRequest request) {
        Object raw = request.getCategoriesRaw();

        // 요청이 배열일 수도 있고 문자열일 수도 있으므로 유연하게 파싱
        List<String> categories = new ArrayList<>();

        if (raw instanceof String s) {
            if (!s.isBlank() && !"전체".equals(s) && !"ALL".equalsIgnoreCase(s)) {
                categories.add(s.trim());
            }
        } else if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o == null) continue;
                String s = o.toString().trim();
                if (!s.isEmpty() && !"전체".equals(s) && !"ALL".equalsIgnoreCase(s)) {
                    categories.add(s);
                }
            }
        }

        // completed=false 인 챌린지 전체 조회
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserIdAndCompletedFalse(userId);

        // 카테고리 미선택(또는 전체)인 경우 그대로 반환
        if (categories.isEmpty()) {
            return userChallenges.stream()
                    .map(uc -> toOngoingChallengeCardResponse(uc, userId))
                    .toList();
        }

        // 카테고리 조건으로 필터 적용
        return userChallenges.stream()
                .filter(uc -> {
                    String dbCat = uc.getChallenge().getCategory();
                    return dbCat != null && categories.stream().anyMatch(dbCat::equalsIgnoreCase);
                })
                .map(uc -> toOngoingChallengeCardResponse(uc, userId))
                .toList();
    }

    /**
     * 완료된 챌린지 필터링
     */
    @Transactional(readOnly = true)
    public List<ChallengeCardResponse> filterCompletedChallenges(Long userId, ChallengeFilterRequest request) {
        Object raw = request.getCategoriesRaw();
        List<String> categories = new ArrayList<>();

        if (raw instanceof String s) {
            if (!s.isBlank() && !"전체".equals(s) && !"ALL".equalsIgnoreCase(s)) {
                categories.add(s.trim());
            }
        } else if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o == null) continue;
                String s = o.toString().trim();
                if (!s.isEmpty() && !"전체".equals(s) && !"ALL".equalsIgnoreCase(s)) {
                    categories.add(s);
                }
            }
        }

        List<UserChallenge> userChallenges = userChallengeRepository.findByUserIdAndCompletedTrue(userId);

        if (categories.isEmpty()) {
            return userChallenges.stream()
                    .map(uc -> toCompletedChallengeCardResponse(uc, userId))
                    .toList();
        }

        return userChallenges.stream()
                .filter(uc -> {
                    String dbCat = uc.getChallenge().getCategory();
                    return dbCat != null && categories.stream().anyMatch(dbCat::equalsIgnoreCase);
                })
                .map(uc -> toCompletedChallengeCardResponse(uc, userId))
                .toList();
    }

    /**
     * 가계부 연동 챌린지 자동 평가
     * - 조건 만족 시 UserChallenge를 성공/완료 처리
     * - 성공 기록(UserChallengeSuccess) 저장
     * - 경험치/레벨 보상 지급
     */
    @Transactional
    public void evaluateOngoingChallenges(Long userId) {
        List<UserChallenge> ongoing = userChallengeRepository.findByUserIdAndCompletedFalse(userId);

        for (UserChallenge uc : ongoing) {
            Challenge c = uc.getChallenge();

            if (!c.isAccountLinked()) {
                continue;
            }

            boolean isSuccess = evaluateChallenge(c, userId);

            if (isSuccess) {
                uc.setSuccess(true);
                uc.setCompleted(true);
                userChallengeRepository.save(uc);

                // 자동 성공 기록 저장 (오늘 날짜 기준)
                userChallengeSuccessRepository.save(UserChallengeSuccess.builder()
                        .userId(userId)
                        .challenge(c)
                        .successDate(LocalDate.now(ZoneId.of("Asia/Seoul")))
                        .build()
                );

                // 보상 지급
                giveRewardToUser(userId);
            }
        }
    }

    /**
     * 가계부 연동 챌린지 성공 시 경험치 지급 및 레벨업 처리
     */
    private void giveRewardToUser(Long userId) {
        UserExp exp = userExpRepository.findByUser_Id(userId)
                .orElseGet(() -> userExpRepository.save(UserExp.builder()
                        .userId(userId)
                        .experience(0)
                        .level(1)
                        .build()));

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
     * 가계부 기반 챌린지 평가 로직
     */
    public boolean evaluateChallenge(Challenge challenge, Long userId) {
        if (!challenge.isAccountLinked()) {
            return false;
        }

        LocalDate end = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate start = end.minusDays(parseGoalPeriod(challenge.getGoalPeriod()));

        BigDecimal goalValue = BigDecimal.valueOf(challenge.getGoalValue());

        switch (challenge.getTitle()) {

            case "무지출 데이 실천하기" -> {
                long noSpendDays = challengeLedgerService.countNoSpendDays(start, end, userId);
                return noSpendDays >= goalValue.intValue();
            }

            case "일주일에 10만원 내로 소비하기",
                 "일주일에 15만원 내로 소비하기" -> {
                BigDecimal totalExpense =
                        challengeLedgerService.sumExpenseOfCategory(start, end, userId, "전체");
                return totalExpense.compareTo(goalValue) <= 0;
            }

            case "자주쓰는 카테고리에서 15000원 이하로 소비하기" -> {
                String topCategory = challengeLedgerService.mostUsedCategory(start, end, userId);
                BigDecimal categoryExpense =
                        challengeLedgerService.sumExpenseOfCategory(start, end, userId, topCategory);
                return categoryExpense.compareTo(goalValue) <= 0;
            }

            case "계획대로 지출했는지 체크하기" -> {
                long daysWithExpense =
                        challengeLedgerService.countDaysUnderDailyBudget(
                                start, end, userId, new BigDecimal("999999999"));
                return daysWithExpense >= goalValue.intValue();
            }

            default -> {
                return false;
            }
        }
    }

    /**
     * 챌린지 기간 문자열을 일(day) 단위로 환산
     * 예) "7일" -> 7, "2주" -> 14, "1개월" -> 30
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
