package com.moneybuddy.moneylog.main.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.ledger.activity.GraphActivity;            // ✅ import 추가
import com.moneybuddy.moneylog.ledger.activity.LedgerWriteActivity;    // ✅ import 추가
import com.moneybuddy.moneylog.ledger.adapter.CalendarAdapter;
import com.moneybuddy.moneylog.ledger.domain.CalendarGridBuilder;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainMenuLedgerFragment extends Fragment {

    private RecyclerView rvCalendar;
    private TextView tvYearMonth;
    private CalendarAdapter adapter;
    private Calendar currentCalendar;

    // ── 월 합계(중간 요약줄)
    private TextView tvMonthIncome;   // id: tv_month_income
    private TextView tvMonthExpense;  // id: tv_month_expense
    private TextView tvMonthNet;      // id: tv_month_net

    // ── 소비목표 미니 막대 영역
    private LinearLayout goalBarTrack;           // id: goal_bar_track
    private TextView tvGoalSpent;                // id: tv_goal_spent
    private TextView tvGoalTarget;               // id: tv_goal_target
    private long monthGoal = 0L;
    private int[] previewSegments;



    // 그래프 화면으로 전달할 캐시
    private long lastMonthSpentCached = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu_ledger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // 상단
        tvYearMonth      = v.findViewById(R.id.tv_year_month);
        rvCalendar       = v.findViewById(R.id.rv_calendar);
        ImageView btnPrev = v.findViewById(R.id.btn_prev_month);
        ImageView btnNext = v.findViewById(R.id.btn_next_month);
        ImageView btnAdd  = v.findViewById(R.id.btn_add_calendar);

        // 요약줄
        tvMonthIncome  = v.findViewById(R.id.tv_month_income);
        tvMonthExpense = v.findViewById(R.id.tv_month_expense);
        tvMonthNet     = v.findViewById(R.id.tv_month_net);

        // 미니 막대
        goalBarTrack = v.findViewById(R.id.goal_bar_track);
        tvGoalSpent  = v.findViewById(R.id.tv_goal_spent);
        tvGoalTarget = v.findViewById(R.id.tv_goal_target);

        currentCalendar = Calendar.getInstance();

        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        updateCalendar(); // 최초 표시 (달력 + 요약 + 막대)

        // 이전/다음 달
        btnPrev.setOnClickListener(v2 -> { currentCalendar.add(Calendar.MONTH, -1); updateCalendar(); });
        btnNext.setOnClickListener(v2 -> { currentCalendar.add(Calendar.MONTH,  1); updateCalendar(); });

        // 년·월 선택
        tvYearMonth.setOnClickListener(v2 -> showYearMonthPicker());

        // 작성 화면 이동(+ 버튼)
        btnAdd.setOnClickListener(v2 ->
                startActivity(new Intent(requireContext(), LedgerWriteActivity.class))
        );

        // 카드 탭 시 원그래프 화면으로 이동(같은 데이터 전달)
        View cardGoalBar = v.findViewById(R.id.card_goal_bar);
        if (cardGoalBar != null) {
            cardGoalBar.setOnClickListener(v2 -> {
                int y = currentCalendar.get(Calendar.YEAR);
                int m = currentCalendar.get(Calendar.MONTH) + 1;
                Intent it = new Intent(requireContext(), GraphActivity.class);
                it.putExtra("year", y);
                it.putExtra("month", m);
                startActivity(it);
            });
        }
    }


    private void updateCalendar() {
        int year  = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        String ym = String.format(Locale.KOREAN, "%04d-%02d", year, month);

        tvYearMonth.setText(year + "년 " + month + "월");

        List<LedgerDayData> days = CalendarGridBuilder.generateCalendar(year, month);
        adapter = new CalendarAdapter(requireContext(), days, ym);
        rvCalendar.setAdapter(adapter);

        MonthTotals totals = calcMonthlyTotals(days);

        if (tvMonthIncome != null)  tvMonthIncome.setText(KoreanMoney.format(totals.income));
        if (tvMonthExpense != null) tvMonthExpense.setText(KoreanMoney.format(totals.expense));
        if (tvMonthNet != null) {
            long net = totals.income - totals.expense;
            tvMonthNet.setText(net < 0 ? "-" + KoreanMoney.format(Math.abs(net)) : KoreanMoney.format(net));
            tvMonthNet.setTextColor(net >= 0 ? Color.parseColor("#2A86FF") : Color.parseColor("#C5463F"));
        }

        long monthSpent = totals.expense;
        lastMonthSpentCached = monthSpent;

        previewSegments = buildSegmentsForPreview(monthSpent);
        if (tvGoalSpent != null)  tvGoalSpent.setText(KoreanMoney.format(monthSpent));
        if (tvGoalTarget != null) tvGoalTarget.setText(KoreanMoney.format(monthGoal));
        if (goalBarTrack != null) renderStackedGoalBar(monthGoal, monthSpent, previewSegments);
    }


    /** 달력에서 년·월만 선택 */
    private void showYearMonthPicker() {
        final int curYear  = currentCalendar.get(Calendar.YEAR);
        final int curMonth = currentCalendar.get(Calendar.MONTH) + 1;

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        int pad = Math.round(getResources().getDisplayMetrics().density * 16);
        container.setPadding(pad, pad, pad, pad);

        NumberPicker yearPicker = new NumberPicker(requireContext());
        yearPicker.setMinValue(curYear - 50);
        yearPicker.setMaxValue(curYear + 50);
        yearPicker.setValue(curYear);
        yearPicker.setWrapSelectorWheel(true);

        NumberPicker monthPicker = new NumberPicker(requireContext());
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(curMonth);
        monthPicker.setWrapSelectorWheel(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        container.addView(yearPicker, lp);
        container.addView(monthPicker, lp);

        new AlertDialog.Builder(requireContext())
                .setTitle("년/월 선택")
                .setView(container)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", (d, w) -> {
                    currentCalendar.set(Calendar.YEAR, yearPicker.getValue());
                    currentCalendar.set(Calendar.MONTH, monthPicker.getValue() - 1); // 0-based
                    currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    updateCalendar();
                    Toast.makeText(requireContext(), "달을 변경했어요", Toast.LENGTH_SHORT).show();
                })
                .show();
    }


    private MonthTotals calcMonthlyTotals(List<LedgerDayData> list) {
        long income = 0, expense = 0;
        if (list != null) {
            for (LedgerDayData d : list) {
                if (d == null || d.isEmpty()) continue;
                income  += Math.max(0, d.getIncome());
                expense += Math.max(0, d.getExpense());
            }
        }
        return new MonthTotals(income, expense);
    }

    private static class MonthTotals {
        final long income;
        final long expense;
        MonthTotals(long i, long e) { income = i; expense = e; }
    }

    private int[] buildSegmentsForPreview(long spent) {
        if (spent <= 0) return new int[]{0};
        long a = Math.round(spent * 0.40f);
        long b = Math.round(spent * 0.30f);
        long c = Math.max(0, spent - a - b);
        return new int[]{(int) a, (int) b, (int) c};
    }

    private void renderStackedGoalBar(long goal, long spent, int[] seg) {
        if (goalBarTrack == null) return;

        goalBarTrack.removeAllViews();

        LinearLayout spentBar = new LinearLayout(requireContext());
        spentBar.setOrientation(LinearLayout.HORIZONTAL);
        spentBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(spent, 0)));

        View remainSpacer = new View(requireContext());
        remainSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(goal - spent, 0)));

        goalBarTrack.addView(spentBar);
        goalBarTrack.addView(remainSpacer);

        int[] colors = new int[]{
                0xFFFCA5A5, 0xFFFDBA74, 0xFFFDE047,
                0xFF86EFAC, 0xFF93C5FD, 0xFFA5B4FC, 0xFFD8B4FE
        };

        long total = 0; for (int v : seg) total += v;
        long cap = Math.min(total, spent);
        long acc = 0;

        for (int i = 0; i < seg.length; i++) {
            long amt = seg[i];
            if (amt <= 0) continue;
            if (acc + amt > cap) amt = cap - acc;
            acc += amt;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, amt);
            View part = new View(requireContext());
            part.setLayoutParams(lp);

            boolean first = (spentBar.getChildCount() == 0);
            boolean last  = (acc == cap);
            part.setBackground(makeRoundedSegment(colors[i % colors.length], first, last));

            spentBar.addView(part);
            if (acc >= cap) break;
        }
    }

    private android.graphics.drawable.GradientDrawable makeRoundedSegment(int color, boolean first, boolean last) {
        float r = requireContext().getResources().getDisplayMetrics().density * 8f;
        float tl = first ? r : 0, bl = first ? r : 0;
        float tr = last  ? r : 0, br = last  ? r : 0;

        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadii(new float[]{tl, tl, tr, tr, br, br, bl, bl});
        return gd;
    }
}
