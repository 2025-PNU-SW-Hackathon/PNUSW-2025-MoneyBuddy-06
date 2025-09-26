package com.moneybuddy.moneylog.ledger.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.ledger.activity.GraphActivity;
import com.moneybuddy.moneylog.ledger.adapter.CalendarDayAdapter;
import com.moneybuddy.moneylog.ledger.dto.response.BudgetGoalDto;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;
import com.moneybuddy.moneylog.ledger.model.DayCell;
import com.moneybuddy.moneylog.ledger.repository.AnalyticsRepository;
import com.moneybuddy.moneylog.ledger.repository.BudgetRepository;
import com.moneybuddy.moneylog.ledger.repository.LedgerRepository;
import com.moneybuddy.moneylog.ledger.ui.CategoryColors;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LedgerCalendarFragment extends Fragment {

    // 상단 요약
    private TextView tvIncome, tvExpense, tvNet, tvYearMonth;

    // 소비목표 카드(미니 막대)
    private CardView cardGoalBar;
    private LinearLayout goalBarTrack;
    private TextView tvGoalSpent;
    private TextView tvGoalTarget;

    // 달력
    private RecyclerView rv;
    private CalendarDayAdapter adapter;

    // 레포
    private LedgerRepository ledgerRepo;
    private BudgetRepository budgetRepo;
    private AnalyticsRepository analyticsRepo;

    private int year, month; // 표시 중인 연/월

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_main_menu_ledger, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // ---- 뷰 바인딩 ----
        tvIncome    = v.findViewById(R.id.tv_month_income);
        tvExpense   = v.findViewById(R.id.tv_month_expense);
        tvNet       = v.findViewById(R.id.tv_month_net);
        tvYearMonth = v.findViewById(R.id.tv_year_month);

        cardGoalBar   = v.findViewById(R.id.card_goal_bar);
        goalBarTrack  = v.findViewById(R.id.goal_bar_track);
        tvGoalSpent   = v.findViewById(R.id.tv_goal_spent);
        tvGoalTarget  = v.findViewById(R.id.tv_goal_target);

        rv = v.findViewById(R.id.rv_calendar); // 레이아웃 id와 일치
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        adapter = new CalendarDayAdapter(requireContext());
        rv.setAdapter(adapter);

        // ---- 레포 초기화 ----
        String token = TokenManager.getInstance(requireContext()).getToken(); // 또는 .get()
        if (token == null) token = "";
        ledgerRepo   = new LedgerRepository(requireContext(), token);
        budgetRepo   = new BudgetRepository(requireContext(), token);
        analyticsRepo= new AnalyticsRepository(requireContext(), token);

        // ---- 연/월 결정 ----
        Bundle args = getArguments();
        if (args != null) {
            year  = args.getInt("year", nowYear());
            month = args.getInt("month", nowMonth());
        } else {
            year  = nowYear();
            month = nowMonth();
        }

        // 상단 "YYYY년 M월"
        tvYearMonth.setText(String.format(Locale.KOREAN, "%04d년 %d월", year, month));

        // 6x7 셀 구성
        adapter.submitDays(build42Cells(year, month));

        // 월 데이터 + 목표 + 카테고리 비율 → 바인딩
        loadMonthAndGoalAndBar(year, month);

        // 그래프 화면 이동 버튼
        if (cardGoalBar != null) {
            cardGoalBar.setOnClickListener(v2 -> {
                Intent it = new Intent(requireContext(), GraphActivity.class);
                it.putExtra("year", year);
                it.putExtra("month", month);
                startActivity(it);
            });
        }
    }


    //  월 요약 / 목표 / 카테고리 비율(카드 막대)

    private void loadMonthAndGoalAndBar(int y, int m) {
        final String ym = String.format(Locale.KOREAN, "%04d-%02d", y, m);

        // 1) 월 합계(상단 3종)
        ledgerRepo.getMonth(ym).enqueue(new Callback<LedgerMonthResponse>() {
            @Override public void onResponse(Call<LedgerMonthResponse> call, Response<LedgerMonthResponse> res) {
                if (!isAdded()) return;
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "월 데이터 실패(" + res.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindMonthSummary(res.body());
            }
            @Override public void onFailure(Call<LedgerMonthResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "월 데이터 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 2) 목표 금액 (표시용)
        budgetRepo.getGoal(ym).enqueue(new Callback<BudgetGoalDto>() {
            @Override public void onResponse(Call<BudgetGoalDto> call, Response<BudgetGoalDto> gres) {
                if (!isAdded()) return;
                long goal = 0L;
                if (gres.isSuccessful() && gres.body() != null) {
                    goal = Math.max(0L, gres.body().amount);
                }
                if (tvGoalTarget != null) tvGoalTarget.setText(formatWon(goal));
            }
            @Override public void onFailure(Call<BudgetGoalDto> call, Throwable t) {
                // 목표 실패 시 0 유지
            }
        });

        // 3) 카테고리 비율/월 사용 합계(미니 막대 그리기용)
        analyticsRepo.getCategoryRatio(ym).enqueue(new Callback<CategoryRatioResponse>() {
            @Override public void onResponse(Call<CategoryRatioResponse> call, Response<CategoryRatioResponse> ares) {
                if (!isAdded()) return;
                renderGoalBar(ares.isSuccessful() ? ares.body() : null);
            }
            @Override public void onFailure(Call<CategoryRatioResponse> call, Throwable t) {
                if (!isAdded()) return;
                renderGoalBar(null);
            }
        });
    }

    private void bindMonthSummary(@NonNull LedgerMonthResponse dto) {
        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        tvIncome.setText(nf.format(safeLong(dto.getTotalIncome())));
        tvExpense.setText(nf.format(safeLong(dto.getTotalExpense())));
        tvNet.setText(nf.format(safeLong(dto.getBalance())));
    }

    /** 소비목표 카드의 막대 + 숫자 표기 */
    private void renderGoalBar(@Nullable CategoryRatioResponse ratio) {
        if (goalBarTrack == null) return;
        goalBarTrack.removeAllViews();

        long spent = 0L;
        long goal  = 0L;
        boolean baselineIsGoal = false;

        if (ratio != null) {
            spent = Math.max(0L, ratio.spent);
            goal  = ratio.goalAmount == null ? 0L : Math.max(0L, ratio.goalAmount);
            baselineIsGoal = "GOAL".equalsIgnoreCase(ratio.baseline);
        }

        // 숫자 표기
        if (tvGoalSpent != null)  tvGoalSpent.setText(formatWon(spent));
        if (tvGoalTarget != null) tvGoalTarget.setText(formatWon(baselineIsGoal ? goal : Math.max(spent, goal)));

        // baseline: 목표가 있으면 목표, 없으면 spent
        long baseline = baselineIsGoal && goal > 0 ? goal : Math.max(spent, 1L);

        // 트랙 속성
        goalBarTrack.setOrientation(LinearLayout.HORIZONTAL);
        goalBarTrack.setWeightSum((float) baseline);

        final int segHeight = dp(16);
        final int segMargin = dp(1);

        // 1) 카테고리 스택 (사용 영역)
        if (ratio != null && ratio.items != null && spent > 0) {
            for (CategoryRatioResponse.Item it : ratio.items) {
                long amt = Math.max(0L, it.expense);
                if (amt <= 0) continue;

                View seg = new View(requireContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, segHeight, (float) amt);
                lp.leftMargin = segMargin;
                seg.setLayoutParams(lp);
                seg.setBackgroundColor(CategoryColors.bg(requireContext(), it.category));
                goalBarTrack.addView(seg);
            }
        }

        // 2) 잔여(회색) — 목표 기준으로 남은 만큼
        long remain = Math.max(0L, baseline - spent);
        if (remain > 0) {
            View remainView = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, segHeight, (float) remain);
            lp.leftMargin = segMargin;
            remainView.setLayoutParams(lp);
            remainView.setBackgroundColor(requireContext().getColor(R.color.bar_remainder));
            goalBarTrack.addView(remainView);
        }

        // 사용 영역이 없고 목표도 없으면 회색 풀바
        if ((ratio == null || spent == 0) && remain == 0) {
            View gray = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, segHeight);
            gray.setLayoutParams(lp);
            gray.setBackgroundColor(requireContext().getColor(R.color.bar_remainder));
            goalBarTrack.addView(gray);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 캘린더 유틸
    // ─────────────────────────────────────────────────────────────
    private List<DayCell> build42Cells(int y, int m) {
        List<DayCell> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(y, m - 1, 1);

        int firstDow = cal.get(Calendar.DAY_OF_WEEK); // 1=일
        int leading = firstDow - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < leading; i++) list.add(new DayCell(null));
        for (int d = 1; d <= daysInMonth; d++) list.add(new DayCell(d));
        while (list.size() < 42) list.add(new DayCell(null));
        return list;
    }

    private int nowYear()  { return Calendar.getInstance().get(Calendar.YEAR); }
    private int nowMonth() { return Calendar.getInstance().get(Calendar.MONTH) + 1; }
    private long safeLong(Long v) { return v == null ? 0L : v; }
    private String formatWon(long v) { return NumberFormat.getInstance(Locale.KOREA).format(v); }
    private int dp(int v) { return Math.round(v * requireContext().getResources().getDisplayMetrics().density); }
}
