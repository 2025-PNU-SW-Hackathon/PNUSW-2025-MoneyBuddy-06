package com.moneybuddy.moneylog.ledger.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast; // ✅ 빠져있던 import

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.ledger.adapter.CalendarAdapter;
import com.moneybuddy.moneylog.ledger.domain.CalendarGridBuilder;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

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
    private long monthGoal = 500_000L;           // TODO: 실제 목표 금액으로 교체
    private int[] previewSegments;               // 파이그래프 분할 값(예시)

    // 그래프 화면으로 전달할 캐시
    private long lastMonthSpentCached = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_ledger);

        // 상단
        tvYearMonth      = findViewById(R.id.tv_year_month);
        rvCalendar       = findViewById(R.id.rv_calendar);
        ImageView btnPrev = findViewById(R.id.btn_prev_month);
        ImageView btnNext = findViewById(R.id.btn_next_month);
        ImageView btnAdd  = findViewById(R.id.btn_add_calendar);

        // 요약줄
        tvMonthIncome  = findViewById(R.id.tv_month_income);
        tvMonthExpense = findViewById(R.id.tv_month_expense);
        tvMonthNet     = findViewById(R.id.tv_month_net);

        // 미니 막대
        goalBarTrack = findViewById(R.id.goal_bar_track);
        tvGoalSpent  = findViewById(R.id.tv_goal_spent);
        tvGoalTarget = findViewById(R.id.tv_goal_target);

        currentCalendar = Calendar.getInstance();

        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        updateCalendar(); // 최초 표시 (달력 + 요약 + 막대)

        // 이전/다음 달
        btnPrev.setOnClickListener(v -> { currentCalendar.add(Calendar.MONTH, -1); updateCalendar(); });
        btnNext.setOnClickListener(v -> { currentCalendar.add(Calendar.MONTH,  1); updateCalendar(); });

        // 년·월만 선택하는 픽커
        tvYearMonth.setOnClickListener(v -> showYearMonthPicker());

        // 작성 화면 이동(+ 버튼)
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(CalendarActivity.this, LedgerWriteActivity.class))
        );

        // 카드 탭 시 원그래프 화면으로 이동(같은 데이터 전달)
        View cardGoalBar = findViewById(R.id.card_goal_bar);
        if (cardGoalBar != null) {
            cardGoalBar.setOnClickListener(v -> {
                long monthSpent = lastMonthSpentCached;
                Intent i = new Intent(CalendarActivity.this, GraphActivity.class);
                i.putExtra("goal", monthGoal);
                i.putExtra("spent", monthSpent);
                i.putExtra("segments", previewSegments);
                startActivity(i);
            });
        }
    }

    /** 화면의 연/월 텍스트와 달력 그리드를 현재 currentCalendar 기준으로 갱신 + 요약/미니막대 갱신 */
    private void updateCalendar() {
        int year  = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1; // 1~12
        String ym = String.format(Locale.KOREAN, "%04d-%02d", year, month);

        tvYearMonth.setText(year + "년 " + month + "월");

        // 달력 셀 데이터(빈칸 포함 42칸)
        List<LedgerDayData> days = CalendarGridBuilder.generateCalendar(year, month);
        adapter = new CalendarAdapter(this, days, ym);
        rvCalendar.setAdapter(adapter);

        // ── 월 합계 계산(달력 셀과 동일 소스: days)
        MonthTotals totals = calcMonthlyTotals(days);

        // 중간 요약 숫자 세팅
        if (tvMonthIncome != null)  tvMonthIncome.setText(KoreanMoney.format(totals.income));
        if (tvMonthExpense != null) tvMonthExpense.setText(KoreanMoney.format(totals.expense));
        if (tvMonthNet != null) {
            long net = totals.income - totals.expense; // 수입 - 지출
            tvMonthNet.setText(net < 0
                    ? "-" + KoreanMoney.format(Math.abs(net))
                    : KoreanMoney.format(net));
            tvMonthNet.setTextColor(net >= 0 ? Color.parseColor("#2A86FF")
                    : Color.parseColor("#C5463F"));
        }

        // ── 소비목표 미니 막대(지출 합계 사용)
        long monthSpent = totals.expense;
        lastMonthSpentCached = monthSpent;

        previewSegments = buildSegmentsForPreview(monthSpent); // ✅ 유틸 메서드
        if (tvGoalSpent != null)  tvGoalSpent.setText(KoreanMoney.format(monthSpent));
        if (tvGoalTarget != null) tvGoalTarget.setText(KoreanMoney.format(monthGoal));
        if (goalBarTrack != null) renderStackedGoalBar(monthGoal, monthSpent, previewSegments); // ✅ 유틸 메서드
    }

    /** 달력에서 년·월만 선택 */
    private void showYearMonthPicker() {
        final int curYear  = currentCalendar.get(Calendar.YEAR);
        final int curMonth = currentCalendar.get(Calendar.MONTH) + 1;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        int pad = Math.round(getResources().getDisplayMetrics().density * 16);
        container.setPadding(pad, pad, pad, pad);

        NumberPicker yearPicker = new NumberPicker(this);
        yearPicker.setMinValue(curYear - 50);
        yearPicker.setMaxValue(curYear + 50);
        yearPicker.setValue(curYear);
        yearPicker.setWrapSelectorWheel(true);

        NumberPicker monthPicker = new NumberPicker(this);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(curMonth);
        monthPicker.setWrapSelectorWheel(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        container.addView(yearPicker, lp);
        container.addView(monthPicker, lp);

        new AlertDialog.Builder(this)
                .setTitle("년/월 선택")
                .setView(container)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", (d, w) -> {
                    currentCalendar.set(Calendar.YEAR, yearPicker.getValue());
                    currentCalendar.set(Calendar.MONTH, monthPicker.getValue() - 1); // 0-based
                    currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    updateCalendar();
                    Toast.makeText(this, "달을 변경했어요", Toast.LENGTH_SHORT).show(); // ✅ Toast 사용
                })
                .show();
    }

    // ────────────── 합계 계산 & 미니 막대 유틸 ──────────────

    /** 달력 셀 리스트에서 월 합계(수입/지출)를 계산. 빈칸은 제외 */
    private MonthTotals calcMonthlyTotals(List<LedgerDayData> list) {
        long income = 0, expense = 0;
        if (list != null) {
            for (LedgerDayData d : list) {
                if (d == null || d.isEmpty()) continue;    // 앞/뒤 빈칸 스킵
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

    /** ✅ 카테고리 분할(예시). 실제 카테고리별 합 배열로 교체 가능 */
    private int[] buildSegmentsForPreview(long spent) {
        if (spent <= 0) return new int[]{0};
        long a = Math.round(spent * 0.40f);
        long b = Math.round(spent * 0.30f);
        long c = Math.max(0, spent - a - b);
        return new int[]{(int) a, (int) b, (int) c};
    }

    /** ✅ 목표 대비 지출 스택 막대 렌더링 */
    private void renderStackedGoalBar(long goal, long spent, int[] seg) {
        if (goalBarTrack == null) return;

        goalBarTrack.removeAllViews();

        LinearLayout spentBar = new LinearLayout(this);
        spentBar.setOrientation(LinearLayout.HORIZONTAL);
        spentBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(spent, 0)));

        View remainSpacer = new View(this);
        remainSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(goal - spent, 0)));

        goalBarTrack.addView(spentBar);
        goalBarTrack.addView(remainSpacer);

        // 파스텔 팔레트(식비~기타)
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
            View part = new View(this);
            part.setLayoutParams(lp);

            boolean first = (spentBar.getChildCount() == 0);
            boolean last  = (acc == cap);
            part.setBackground(makeRoundedSegment(colors[i % colors.length], first, last));

            spentBar.addView(part);
            if (acc >= cap) break;
        }
    }

    private android.graphics.drawable.GradientDrawable makeRoundedSegment(int color, boolean first, boolean last) {
        float r = getResources().getDisplayMetrics().density * 8f;
        float tl = first ? r : 0, bl = first ? r : 0;
        float tr = last  ? r : 0, br = last  ? r : 0;

        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadii(new float[]{tl, tl, tr, tr, br, br, bl, bl});
        return gd;
    }
}
