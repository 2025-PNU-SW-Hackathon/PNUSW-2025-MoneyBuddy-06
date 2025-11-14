package com.moneybuddy.moneylog.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 홈 화면 상단 “이번 달 잔액” 카드에
 * - 좌측: 이번 달 사용액(spent)
 * - 우측: 목표 금액(goal)
 * - 하단: 목표 대비 막대바 + 상위 3개 카테고리
 * 를 바인딩.
 */
public class MainMenuHomeFragment extends Fragment {

    // XML: fragment_main_menu_home.xml 내 카드 레이아웃의 구성요소
    private TextView tvSpent;          // @id/textView8  (왼쪽: 사용액)
    private TextView tvGoal;           // @id/textView9  (오른쪽: 목표액)
    private LinearLayout goalBarTrack; // @id/goal_bar_track (막대 트랙)

    // TOP3 카테고리 (막대바 아래 라벨)
    private ImageView ivCat1, ivCat2, ivCat3; // imageView9, 10, 11
    private TextView tvCat1, tvCat2, tvCat3;  // textView11, 12, 13

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

        // TOP3 라벨 뷰
        ivCat1 = v.findViewById(R.id.imageView9);
        ivCat2 = v.findViewById(R.id.imageView10);
        ivCat3 = v.findViewById(R.id.imageView11);
        tvCat1 = v.findViewById(R.id.textView11);
        tvCat2 = v.findViewById(R.id.textView12);
        tvCat3 = v.findViewById(R.id.textView13);

        // 기본값(0원 / 0원 + 빈 막대 + 라벨 숨김)
        applyDefaultState();

        if (analyticsRepo == null) {
            String token = "";
            try {
                token = TokenManager.getInstance(requireContext()).getToken();
            } catch (Exception ignore) {}
            analyticsRepo = new AnalyticsRepository(requireContext(), token);
        }

        loadHomeCard(currentYearMonth()); // "YYYY-MM"
    }

    @Override
    public void onResume() {
        super.onResume();
        // 돌아올 때 최신 값으로 갱신 (실패 시 기본값으로 돌아감)
        loadHomeCard(currentYearMonth());
    }

    /** ✅ Calendar 사용: API 24에서도 안전 */
    private String currentYearMonth() {
        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return String.format(Locale.KOREAN, "%04d-%02d", year, month);
    }

    /** 0원 / 0원 + 빈 막대 + TOP3 라벨 숨김 기본 상태 */
    private void applyDefaultState() {
        if (tvSpent != null) tvSpent.setText(KoreanMoney.format(0));
        if (tvGoal  != null) tvGoal.setText(KoreanMoney.format(0));
        if (goalBarTrack != null) {
            goalBarTrack.removeAllViews(); // 자식 뷰 제거 → bg_goal_track만 보이는 얇은 회색 바
        }
        // TOP3 라벨 숨기기
        setCategoryRowVisible(1, false, null, 0);
        setCategoryRowVisible(2, false, null, 0);
        setCategoryRowVisible(3, false, null, 0);
    }

    /**
     * 홈 카드에 이번 달 사용액/목표액/막대바/상위3카테고리를 바인딩.
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

                // 2) 막대바 렌더
                renderGoalBar(dto);

                // 3) 막대바 아래 TOP3 카테고리 렌더
                renderTopCategories(dto);
            }

            @Override
            public void onError(Throwable t) {
                if (!isAdded() || getView() == null || mySeq != ratioReqSeq) return;
                Toast.makeText(requireContext(), "홈 카드 불러오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // 실패 시 완전 기본 상태로 되돌리기
                applyDefaultState();
            }
        });
    }

    /**
     * 카테고리별 지출 비율 막대 (goal 대비 또는 spent 대비)
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
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (float) w);
            seg.setLayoutParams(lp);

            int color = CategoryColors.bg(requireContext(), cat);
            seg.setBackgroundColor(color);

            goalBarTrack.addView(seg);
        }

        // 남은 여백(목표 미달 시)
        double remain = 1.0 - usedWeight;
        if (remain > 0.0001) {
            View filler = new View(requireContext());
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (float) remain);
            filler.setLayoutParams(lp);
            filler.setBackgroundColor(0x00000000); // 투명 (bg_goal_track 위에 비워둠)
            goalBarTrack.addView(filler);
        }
    }

    /**
     * 막대바 아래 “많이 사용한 품목 상위 3개” 바인딩
     */
    private void renderTopCategories(CategoryRatioResponse dto) {
        if (dto == null || dto.items == null || dto.items.isEmpty()) {
            // 데이터 없으면 라벨 숨김
            setCategoryRowVisible(1, false, null, 0);
            setCategoryRowVisible(2, false, null, 0);
            setCategoryRowVisible(3, false, null, 0);
            return;
        }

        // 지출 금액 기준 내림차순 정렬
        List<CategoryRatioResponse.Item> list = new ArrayList<>(dto.items);
        list.sort((a, b) -> Long.compare(
                Math.max(0L, b.expense),
                Math.max(0L, a.expense)
        ));

        // 상위 3개만 사용 (양수만)
        List<CategoryRatioResponse.Item> top = new ArrayList<>();
        for (CategoryRatioResponse.Item it : list) {
            if (it == null) continue;
            if (Math.max(0L, it.expense) <= 0) continue;
            top.add(it);
            if (top.size() == 3) break;
        }

        // 일단 전부 숨기고 시작
        setCategoryRowVisible(1, false, null, 0);
        setCategoryRowVisible(2, false, null, 0);
        setCategoryRowVisible(3, false, null, 0);

        if (top.isEmpty()) return;

        for (int i = 0; i < top.size() && i < 3; i++) {
            CategoryRatioResponse.Item it = top.get(i);
            String label = it.category;
            int color = CategoryColors.bg(requireContext(), it.category);
            setCategoryRowVisible(i + 1, true, label, color);
        }
    }

    /**
     * TOP3 각 줄의 텍스트/색 설정 및 표시 여부 제어
     *
     * @param index 1,2,3 중 하나
     */
    private void setCategoryRowVisible(int index, boolean visible, String label, int color) {
        ImageView iv = null;
        TextView tv = null;

        switch (index) {
            case 1:
                iv = ivCat1;
                tv = tvCat1;
                break;
            case 2:
                iv = ivCat2;
                tv = tvCat2;
                break;
            case 3:
                iv = ivCat3;
                tv = tvCat3;
                break;
        }

        if (iv == null || tv == null) return;

        if (!visible) {
            iv.setVisibility(View.GONE);
            tv.setVisibility(View.GONE);
            return;
        }

        iv.setVisibility(View.VISIBLE);
        tv.setVisibility(View.VISIBLE);

        if (label != null) tv.setText(label);
        // home_white_square 아이콘 색을 카테고리 색으로 칠함
        iv.setColorFilter(color);
    }
}
