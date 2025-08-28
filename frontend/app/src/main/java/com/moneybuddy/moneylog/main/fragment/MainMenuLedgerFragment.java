package com.moneybuddy.moneylog.main.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.ledger.activity.GraphActivity;
import com.moneybuddy.moneylog.ledger.activity.LedgerWriteActivity;
import com.moneybuddy.moneylog.ledger.adapter.CalendarAdapter;
import com.moneybuddy.moneylog.ledger.domain.CalendarGridBuilder;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;
import com.moneybuddy.moneylog.ledger.repository.AnalyticsRepository;
import com.moneybuddy.moneylog.ledger.ui.CategoryColors;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenuLedgerFragment extends Fragment {

    private RecyclerView rvCalendar;
    private TextView tvYearMonth;
    private CalendarAdapter adapter;
    private Calendar currentCalendar;

    // 서버 API
    private AnalyticsRepository analyticsRepo;

    // 월 합계(중간 요약)
    private TextView tvMonthIncome;
    private TextView tvMonthExpense;
    private TextView tvMonthNet;

    // 목표 미니 막대
    private LinearLayout goalBarTrack;
    private TextView tvGoalSpent;
    private TextView tvGoalTarget;
    private long monthGoal = 0L;
    private int[] previewSegments;

    // 최근 지출 합계(미니 막대 갱신용)
    private long lastMonthSpentCached = 0L;

    // 상단 필드들 근처에 추가
    private com.moneybuddy.moneylog.ledger.repository.LedgerRepository ledgerRepo;


    // 작성 화면 런처(저장 성공 시 달력 새로고침)
    private ActivityResultLauncher<Intent> writeLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu_ledger, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsRepo = new AnalyticsRepository(requireContext(), token());
        // 월별 내역 조회용 레포
        ledgerRepo    = new com.moneybuddy.moneylog.ledger.repository.LedgerRepository(requireContext(), token());

        writeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { if (result.getResultCode() == Activity.RESULT_OK) updateCalendar(); }
        );
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // 상단
        tvYearMonth = v.findViewById(R.id.tv_year_month);
        rvCalendar = v.findViewById(R.id.rv_calendar);
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
        updateCalendar();

        // 이전/다음 달
        btnPrev.setOnClickListener(v2 -> { currentCalendar.add(Calendar.MONTH, -1); updateCalendar(); });
        btnNext.setOnClickListener(v2 -> { currentCalendar.add(Calendar.MONTH,  1); updateCalendar(); });

        // 년·월 선택
        tvYearMonth.setOnClickListener(v2 -> showYearMonthPicker());

        // 작성 화면 이동(+ 버튼) — 저장 성공 시 갱신
        btnAdd.setOnClickListener(v2 -> {
            Intent it = new Intent(requireContext(), LedgerWriteActivity.class);
            writeLauncher.launch(it);
        });

        // 목표 카드 → 그래프 화면
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

    @Override
    public void onResume() {
        super.onResume();
        // 상세 화면 뒤로가기 등 모든 복귀 지점에서 최신화
        updateCalendar();
    }

    private void updateCalendar() {
        int year  = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        String ym = String.format(Locale.KOREAN, "%04d-%02d", year, month);

        tvYearMonth.setText(year + "년 " + month + "월");

        // 달력 셀 & 어댑터
        List<LedgerDayData> days = CalendarGridBuilder.generateCalendar(year, month);
        adapter = new CalendarAdapter(requireContext(), days, ym);
        rvCalendar.setAdapter(adapter);
        fetchAndApplyMonthEntries(year, month, days);

        // 월 합계(달력 셀 기준) — 화면 요약에는 사용
        MonthTotals totals = calcMonthlyTotals(days);
        if (tvMonthIncome != null)  tvMonthIncome.setText(KoreanMoney.format(totals.income));
        if (tvMonthExpense != null) tvMonthExpense.setText(KoreanMoney.format(totals.expense));
        if (tvMonthNet != null) {
            long net = totals.income - totals.expense;
            tvMonthNet.setText(net < 0 ? "-" + KoreanMoney.format(Math.abs(net))
                    : KoreanMoney.format(net));
            tvMonthNet.setTextColor(net >= 0 ? Color.parseColor("#2A86FF")
                    : Color.parseColor("#C5463F"));
        }

        // 미니 막대: 서버 응답으로 렌더(실패 시 달력 합계 fallback)
        lastMonthSpentCached = totals.expense;
        previewSegments = buildSegmentsForPreview(lastMonthSpentCached);
        if (tvGoalSpent != null) tvGoalSpent.setText(KoreanMoney.format(lastMonthSpentCached));

        fetchGoalAndApply(ym);
    }

    // yyyy-MM 의 각 날짜별로 income/expense 합계를 계산해 Calendar 셀에 반영
    private void fetchAndApplyMonthEntries(int year, int month, List<LedgerDayData> days) {
        if (ledgerRepo == null) return;

        ledgerRepo.getMonth(year, month).enqueue(new retrofit2.Callback<java.util.List<com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto>>() {
            @Override public void onResponse(
                    retrofit2.Call<java.util.List<com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto>> call,
                    retrofit2.Response<java.util.List<com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto>> res) {

                if (!res.isSuccessful() || res.body() == null) {
                    // 실패해도 화면은 기존 합계(로컬 계산)로 유지
                    return;
                }

                // 1) 날짜별 합계 집계
                java.util.Map<String, long[]> daily = new java.util.HashMap<>();
                // long[0] = income(+), long[1] = expense(+) 로 저장
                for (com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto it : res.body()) {
                    if (it == null) continue;

                    // 날짜 문자열 뽑기 (예: "2025-08-28")
                    String date = extractDate(it);
                    if (date == null) continue;

                    long amt = extractAmountAsLong(it);  // signed
                    long inc = 0L, exp = 0L;
                    if (amt >= 0) inc = amt; else exp = -amt;

                    long[] pair = daily.get(date);
                    if (pair == null) pair = new long[]{0L, 0L};
                    pair[0] += inc;
                    pair[1] += exp;
                    daily.put(date, pair);
                }

                // 2) 달력 셀에 주입
                applyDailyTotalsToDays(days, daily);

                // 3) 상단 월 요약도 서버 집계 기반으로 갱신(선택)
                long monthIncome = 0L, monthExpense = 0L;
                for (long[] p : daily.values()) { monthIncome += p[0]; monthExpense += p[1]; }
                if (tvMonthIncome  != null) tvMonthIncome.setText(KoreanMoney.format(monthIncome));
                if (tvMonthExpense != null) tvMonthExpense.setText(KoreanMoney.format(monthExpense));
                if (tvMonthNet != null) {
                    long net = monthIncome - monthExpense;
                    tvMonthNet.setText(net < 0 ? "-" + KoreanMoney.format(Math.abs(net)) : KoreanMoney.format(net));
                    tvMonthNet.setTextColor(net >= 0 ? Color.parseColor("#2A86FF") : Color.parseColor("#C5463F"));
                }

                // 4) 어댑터 갱신
                if (rvCalendar != null && rvCalendar.getAdapter() != null) {
                    rvCalendar.getAdapter().notifyDataSetChanged();
                }
            }

            @Override public void onFailure(
                    retrofit2.Call<java.util.List<com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto>> call,
                    Throwable t) {
                // 네트워크 실패 시 스킵 (로컬 합계 유지)
            }
        });
    }

    /** 서버 응답 DTO에서 yyyy-MM-dd 문자열을 안전하게 뽑는다. */
    @Nullable
    private String extractDate(com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto it) {
        try {
            // 서버/클라 DTO에 따라 dateTime 타입이 다를 수 있어 방어적으로 처리
            // 1) 문자열 ISO 형태: "2025-08-28T10:00:00" 등
            java.lang.reflect.Field f = it.getClass().getDeclaredField("dateTime");
            f.setAccessible(true);
            Object v = f.get(it);
            if (v instanceof String) {
                String s = (String) v;
                if (s.length() >= 10) return s.substring(0, 10);
            } else if (v != null) {
                // 2) java.time.LocalDateTime 가 넘어올 경우 toString() 사용
                String s = String.valueOf(v);
                if (s.length() >= 10) return s.substring(0, 10);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /** 서버 응답 DTO에서 금액(signed)을 long 으로 꺼낸다. (BigDecimal 대응) */
    private long extractAmountAsLong(com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto it) {
        try {
            java.lang.reflect.Field fAmt = it.getClass().getDeclaredField("amount");
            fAmt.setAccessible(true);
            Object v = fAmt.get(it);
            if (v instanceof Number) return ((Number) v).longValue();                 // Long/Integer
            if (v != null && v.getClass().getName().endsWith("BigDecimal")) {        // BigDecimal
                return new java.math.BigDecimal(String.valueOf(v)).longValue();
            }
        } catch (Throwable ignored) {}
        // entryType 으로 부호 유추가 필요하다면 추가 처리 가능
        return 0L;
    }

    /** 날짜별 합계를 기존 days 리스트에 반영 */
    private void applyDailyTotalsToDays(List<LedgerDayData> days, java.util.Map<String, long[]> daily) {
        if (days == null || daily == null) return;
        for (LedgerDayData d : days) {
            if (d == null || d.isEmpty()) continue;
            // LedgerDayData가 yyyy-MM-dd 형태의 dateString 을 갖고 있다면 그대로 사용
            String key = d.getDateString(); // 없으면 d.toDateString() 등 프로젝트에 맞게 대체
            long[] pair = daily.get(key);
            long inc = 0L, exp = 0L;
            if (pair != null) { inc = pair[0]; exp = pair[1]; }

            // 프로젝트의 LedgerDayData에 맞춰 세터 호출
            // (필드명이 다르면 여기를 맞춰 바꿔주세요)
            d.setIncome(inc);
            d.setExpense(exp);
        }
    }


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

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        container.addView(yearPicker, lp);
        container.addView(monthPicker, lp);

        new AlertDialog.Builder(requireContext())
                .setTitle("년/월 선택")
                .setView(container)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", (d, w) -> {
                    currentCalendar.set(Calendar.YEAR, yearPicker.getValue());
                    currentCalendar.set(Calendar.MONTH, monthPicker.getValue() - 1);
                    currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    updateCalendar();
                    Toast.makeText(requireContext(), "달을 변경했어요", Toast.LENGTH_SHORT).show();
                })
                .show();
    }


    /** 서버에서 goalAmount/카테고리 받아와 목표/미니막대 반영 */
    private void fetchGoalAndApply(String ym) {
        analyticsRepo.getCategoryRatio(ym).enqueue(new Callback<CategoryRatioResponse>() {
            @Override public void onResponse(Call<CategoryRatioResponse> call, Response<CategoryRatioResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    if (goalBarTrack != null) renderStackedGoalBar(monthGoal, lastMonthSpentCached, previewSegments);
                    return;
                }

                CategoryRatioResponse body = res.body();

                long spentFromApi = Math.max(0L, body.spent);
                Long goalFromApi  = body.goalAmount;
                monthGoal = (goalFromApi == null) ? 0L : Math.max(0L, goalFromApi);

                lastMonthSpentCached = spentFromApi;
                if (tvGoalSpent  != null) tvGoalSpent.setText(KoreanMoney.format(spentFromApi));
                if (tvGoalTarget != null) tvGoalTarget.setText(KoreanMoney.format(monthGoal));

                // ✅ 파이차트와 동일한 규칙(카테고리 색)으로 미니 막대 렌더
                renderGoalBarWithCategories(monthGoal, spentFromApi, body.items);
            }

            @Override public void onFailure(Call<CategoryRatioResponse> call, Throwable t) {
                if (goalBarTrack != null) renderStackedGoalBar(monthGoal, lastMonthSpentCached, previewSegments);
            }
        });
    }

    /** 파이차트와 동일한 카테고리 색으로 미니 막대 렌더링 */
    private void renderGoalBarWithCategories(long goal, long spent, List<CategoryRatioResponse.Item> items) {
        if (goalBarTrack == null) return;
        goalBarTrack.setVisibility(View.VISIBLE);
        goalBarTrack.removeAllViews();

        goalBarTrack.post(() -> {
            int trackW = goalBarTrack.getWidth();
            if (trackW <= 0) return;

            // 전체 폭 중 지출이 차지하는 픽셀 계산(최소 4dp 보장)
            float ratio = (goal > 0) ? Math.min(1f, spent / (float) goal) : (spent > 0 ? 1f : 0f);
            int minPx = (spent > 0) ? dp2px(4) : 0;
            int spentPx = Math.max(Math.round(trackW * ratio), minPx);
            spentPx = Math.min(spentPx, trackW);
            int remainPx = Math.max(0, trackW - spentPx);

            // 지출 영역 컨테이너
            LinearLayout spentBar = new LinearLayout(requireContext());
            spentBar.setOrientation(LinearLayout.HORIZONTAL);
            spentBar.setLayoutParams(new LinearLayout.LayoutParams(
                    spentPx, LinearLayout.LayoutParams.MATCH_PARENT));

            // 남은 영역(빈 공간)
            View remainSpacer = new View(requireContext());
            remainSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                    remainPx, LinearLayout.LayoutParams.MATCH_PARENT));

            goalBarTrack.addView(spentBar);
            goalBarTrack.addView(remainSpacer);

            if (items == null || items.isEmpty() || spentPx == 0) return;

            // 보기 좋게 금액 내림차순
            List<CategoryRatioResponse.Item> list = new ArrayList<>(items);
            list.sort((a, b) -> Long.compare(b.expense, a.expense));

            long totalExpense = 0L;
            for (CategoryRatioResponse.Item it : list) totalExpense += Math.max(0L, it.expense);
            if (totalExpense <= 0L) return;

            long acc = 0L;
            for (int i = 0; i < list.size(); i++) {
                CategoryRatioResponse.Item it = list.get(i);
                long v = Math.max(0L, it.expense);
                if (v == 0) continue;

                float weight = v / (float) totalExpense;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, weight);

                View part = new View(requireContext());
                part.setLayoutParams(lp);

                boolean first = (spentBar.getChildCount() == 0);
                acc += v;
                boolean last  = (i == list.size() - 1);

                int color = CategoryColors.bg(requireContext(), normalizeLabel(it.category));
                part.setBackground(makeRoundedSegment(color, first, last));
                spentBar.addView(part);
            }
        });
    }

    /** 기존 프리뷰 방식(고정 팔레트) — 네트워크 실패 시 fallback용 */
    private void renderStackedGoalBar(long goal, long spent, int[] seg) {
        if (goalBarTrack == null) return;

        goalBarTrack.setVisibility(View.VISIBLE);
        goalBarTrack.removeAllViews();

        // 픽셀 기반으로 최소 가시성 보장
        goalBarTrack.post(() -> {
            int trackW = goalBarTrack.getWidth();
            if (trackW <= 0) return;

            float ratio = (goal > 0) ? Math.min(1f, spent / (float) goal) : (spent > 0 ? 1f : 0f);
            int minPx = (spent > 0) ? dp2px(4) : 0;
            int spentPx = Math.max(Math.round(trackW * ratio), minPx);
            spentPx = Math.min(spentPx, trackW);
            int remainPx = Math.max(0, trackW - spentPx);

            LinearLayout spentBar = new LinearLayout(requireContext());
            spentBar.setOrientation(LinearLayout.HORIZONTAL);
            spentBar.setLayoutParams(new LinearLayout.LayoutParams(
                    spentPx, LinearLayout.LayoutParams.MATCH_PARENT));

            View remainSpacer = new View(requireContext());
            remainSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                    remainPx, LinearLayout.LayoutParams.MATCH_PARENT));

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
        });
    }

    /** 라벨 정규화 — CategoryColors 키와 맞추기 */
    private static String normalizeLabel(String s) {
        if (s == null) return "";
        String x = s.replace('\u00A0',' ')
                .replace("·", "/")
                .replace("／", "/")
                .replace("|", "/")
                .trim()
                .replaceAll("\\s+", " ");
        if (x.equals("카페 / 베이커리")) x = "카페/베이커리";
        return x;
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

    private String token() {
        try {
            String t = TokenManager.getInstance(requireContext()).getToken();
            return TextUtils.isEmpty(t) ? "" : t;
        } catch (Exception e) {
            return "";
        }
    }

    // ===== 기존 합계/보조 메서드 =====

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

    private int dp2px(float dp) {
        return Math.round(requireContext().getResources().getDisplayMetrics().density * dp);
    }
}
