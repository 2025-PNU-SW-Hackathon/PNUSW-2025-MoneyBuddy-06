package com.moneybuddy.moneylog.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.common.ResultCallback;
import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.repository.AnalyticsRepository;
import com.moneybuddy.moneylog.ledger.ui.CategoryColors;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.Calendar;   // ✅ LocalDate 대신 Calendar 사용 (minSdk 24 호환)
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 홈 화면 상단 “이번 달 잔액” 카드에
 * - 좌측: 이번 달 사용액(spent)
 * - 우측: 목표 금액(goal)
 * - 하단: 가계부 화면과 동일한 목표 대비 막대바
 * 를 바인딩.
 */
public class MainMenuHomeFragment extends Fragment {

    // XML: fragment_main_menu_home.xml 내 카드 레이아웃의 구성요소
    private TextView tvSpent;          // @id/textView8  (왼쪽: 사용액)
    private TextView tvGoal;           // @id/textView9  (오른쪽: 목표액)
    private LinearLayout goalBarTrack; // @id/goal_bar_track (막대 트랙)

    // 리포지토리
    private AnalyticsRepository analyticsRepo;

    // 동시 요청 무효화용 시퀀스 토큰
    private int ratioReqSeq = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tvSpent = v.findViewById(R.id.textView8);
        tvGoal  = v.findViewById(R.id.textView9);
        goalBarTrack = v.findViewById(R.id.goal_bar_track);

        if (analyticsRepo == null) {
            String token = "";
            try {
                token = TokenManager.getInstance(requireContext()).getToken();
            } catch (Exception ignore) {}
            // ✅ 다른 화면(MainMenuLedgerFragment)과 동일한 생성자 사용
            analyticsRepo = new AnalyticsRepository(requireContext(), token);
        }

        loadHomeCard(currentYearMonth()); // "YYYY-MM"
    }

    @Override
    public void onResume() {
        super.onResume();
        // 돌아올 때 최신 값으로 갱신
        loadHomeCard(currentYearMonth());
    }

    /** ✅ Calendar 사용: API 24에서도 안전 */
    private String currentYearMonth() {
        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return String.format(Locale.KOREAN, "%04d-%02d", year, month);
    }

    /**
     * 홈 카드에 이번 달 사용액/목표액/막대바를 바인딩.
     * GET /analytics/category-ratio?ym=YYYY-MM 응답을 사용.
     */
    private void loadHomeCard(String ym) {
        final int mySeq = ++ratioReqSeq; // 최신 요청 토큰

        analyticsRepo.getCategoryRatio(ym, new ResultCallback<CategoryRatioResponse>() {
            @Override
            public void onSuccess(CategoryRatioResponse dto) {
                if (!isAdded() || getView() == null || mySeq != ratioReqSeq || dto == null) return;

                // 1) 금액 텍스트 바인딩
                long spent = Math.max(0L, dto.spent);
                long goalAmount = (dto.goalAmount == null) ? 0L : Math.max(0L, dto.goalAmount);

                if (tvSpent != null) tvSpent.setText(KoreanMoney.format(spent));       // 왼쪽: 사용액
                if (tvGoal  != null) tvGoal.setText(KoreanMoney.format(goalAmount));   // 오른쪽: 목표액

                // 2) 막대바 렌더 (가계부 화면 로직과 동일: baseline = GOAL 또는 SPENT)
                renderGoalBar(dto);
            }

            @Override
            public void onError(Throwable t) {
                if (!isAdded() || getView() == null || mySeq != ratioReqSeq) return;
                Toast.makeText(requireContext(), "홈 카드 불러오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (goalBarTrack != null) goalBarTrack.removeAllViews();
            }
        });
    }

    /**
     * 카테고리별 지출 비율 막대 (goal 대비 또는 spent 대비)
     * - baseline: dto.baseline이 "GOAL"이면 목표액, "SPENT"면 실제 사용액을 100%로.
     * - 각 카테고리 expense / baseline 가중치로 segment 구성
     * - 남은 영역(목표 미달 시)은 빈 공간으로 둠.
     */
    private void renderGoalBar(CategoryRatioResponse dto) {
        if (goalBarTrack == null || dto == null) return;

        goalBarTrack.removeAllViews();

        // baseline 계산
        final long spent = Math.max(0L, dto.spent);
        final long goal  = (dto.goalAmount == null) ? 0L : Math.max(0L, dto.goalAmount);
        final double baseline = ("GOAL".equalsIgnoreCase(dto.baseline) && goal > 0)
                ? goal
                : Math.max(1.0, spent); // 0 분모 방지

        // 카테고리별 금액 맵
        Map<String, Long> byCategory = new LinkedHashMap<>();
        if (dto.items != null) {
            for (CategoryRatioResponse.Item it : dto.items) {
                long v = Math.max(0L, it.expense);
                if (v <= 0) continue;
                byCategory.put(it.category, v);
            }
        }

        // 전체 가중치 합
        double usedWeight = 0.0;
        for (long v : byCategory.values()) usedWeight += (v / baseline);
        if (usedWeight > 1.0) usedWeight = 1.0; // 목표 초과 시 100% 캡

        // 카테고리 segment 추가
        for (Map.Entry<String, Long> e : byCategory.entrySet()) {
            String cat = e.getKey();
            long v = e.getValue();
            double w = v / baseline;
            if (w <= 0) continue;

            View seg = new View(requireContext());
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) w);
            seg.setLayoutParams(lp);

            // ✅ 프로젝트에서 실제 쓰는 메서드: bg(Context, category)
            int color = CategoryColors.bg(requireContext(), cat);
            seg.setBackgroundColor(color);

            goalBarTrack.addView(seg);
        }

        // 남은 여백(목표 미달 시)
        double remain = 1.0 - usedWeight;
        if (remain > 0.0001) {
            View filler = new View(requireContext());
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) remain);
            filler.setLayoutParams(lp);
            filler.setBackgroundColor(0x00000000); // 투명 (bg_goal_track 위에 비워둠)
            goalBarTrack.addView(filler);
        }
    }
}
