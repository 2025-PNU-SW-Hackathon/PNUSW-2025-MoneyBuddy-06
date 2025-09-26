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
import com.moneybuddy.moneylog.ledger.activity.LedgerDetailActivity;
import com.moneybuddy.moneylog.ledger.activity.LedgerWriteActivity;
import com.moneybuddy.moneylog.ledger.adapter.CalendarAdapter;
import com.moneybuddy.moneylog.ledger.domain.CalendarGridBuilder;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerMonthResponse;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;
import com.moneybuddy.moneylog.ledger.repository.AnalyticsRepository;
import com.moneybuddy.moneylog.ledger.repository.LedgerRepository;
import com.moneybuddy.moneylog.ledger.ui.CategoryColors;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenuLedgerFragment extends Fragment {

    private RecyclerView rvCalendar;
    private TextView tvYearMonth;
    private CalendarAdapter adapter;                // 재사용
    private Calendar currentCalendar;

    // 서버 API
    private AnalyticsRepository analyticsRepo;
    private LedgerRepository ledgerRepo;

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

    private final List<LedgerDayData> days = new ArrayList<>();

    // 최근 지출 합계(미니 막대 갱신용)
    private long lastMonthSpentCached = 0L;

    // 작성 화면 런처(저장 성공 시 달력 새로고침)
    private ActivityResultLauncher<Intent> writeLauncher;

    // ✅ 진행 중인 네트워크 콜 보관/관리
    private Call<LedgerMonthResponse> monthCall;
    private Call<CategoryRatioResponse> ratioCall;

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
        String tk = token();
        analyticsRepo = new AnalyticsRepository(requireContext(), tk);
        ledgerRepo    = new LedgerRepository(requireContext(), tk);

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
        rvCalendar  = v.findViewById(R.id.rv_calendar);
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

        // RecyclerView + 어댑터 1회 세팅
        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        if (rvCalendar.getAdapter() instanceof CalendarAdapter) {
            adapter = (CalendarAdapter) rvCalendar.getAdapter();
        } else {
            adapter = new CalendarAdapter(getContext());
            rvCalendar.setAdapter(adapter);
        }

        // 날짜 클릭 → 상세화면 이동
        adapter.setOnDayClickListener(this::openDayDetail);

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
        updateCalendar();
    }

    @Override
    public void onStop() {
        super.onStop();
        // 화면 내려갈 때 백그라운드 IPC 줄이기: 콜 취소
        cancelEnqueuedCalls();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 뷰 정리: 콜 취소 + 리스너 해제
        cancelEnqueuedCalls();
        if (adapter != null) adapter.setOnDayClickListener(null);
    }

    private void cancelEnqueuedCalls() {
        try { if (monthCall != null) { monthCall.cancel(); monthCall = null; } } catch (Exception ignore) {}
        try { if (ratioCall != null) { ratioCall.cancel(); ratioCall = null; } } catch (Exception ignore) {}
    }

    /** 현재 currentCalendar 기준으로 달력 렌더 & 서버 집계 반영 */
    private void updateCalendar() {
        // 새로 불러오기 전에 진행 중 콜 정리
        cancelEnqueuedCalls();

        int year  = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        String ym = String.format(Locale.KOREAN, "%04d-%02d", year, month);

        if (tvYearMonth != null) {
            tvYearMonth.setText(year + "년 " + month + "월");
        }

        // 1) 42칸 셀 생성 → 어댑터에 주입 (필드 리스트/어댑터 재사용)
        days.clear();
        days.addAll(CalendarGridBuilder.buildMonthCells(year, month));
        adapter.submitDays(days); // 날짜/표시 초기화

        // 2) 서버 오기 전 임시 요약 값 — 곧 서버 응답으로 덮어씌움
        MonthTotals fallback = calcMonthlyTotals(days);
        if (tvMonthIncome  != null)  tvMonthIncome.setText(KoreanMoney.format(fallback.income));
        if (tvMonthExpense != null) tvMonthExpense.setText(KoreanMoney.format(fallback.expense));
        if (tvMonthNet != null) {
            long net = fallback.income - fallback.expense;
            tvMonthNet.setText(net < 0 ? "-" + KoreanMoney.format(Math.abs(net))
                    : KoreanMoney.format(net));
            tvMonthNet.setTextColor(net >= 0 ? Color.parseColor("#2A86FF")
                    : Color.parseColor("#C5463F"));
        }

        // 3) 미니 막대(초기 = 임시 합계, 서버 응답으로 갱신)
        lastMonthSpentCached = fallback.expense;
        previewSegments = buildSegmentsForPreview(lastMonthSpentCached);
        if (tvGoalSpent != null) tvGoalSpent.setText(KoreanMoney.format(lastMonthSpentCached));

        // 4) 서버 월 집계 → 각 날짜 밑 합계 주입 + 상단 요약(서버 값) 갱신
        fetchAndApplyMonthEntries(ym);

        // 5) 목표/실지출 막대 최신화
        fetchGoalAndApply(ym);
    }

    /** 날짜 클릭 시 상세 화면으로 이동 */
    private void openDayDetail(LedgerDayData item) {
        if (item == null || !item.isInThisMonth()) return;
        String ymd = item.getDate();
        if (ymd == null || ymd.length() < 10) return;

        Intent i = new Intent(requireContext(), LedgerDetailActivity.class);
        i.putExtra("date", ymd);
        startActivity(i);
    }

    /** 서버에서 goalAmount/카테고리 받아와 목표/미니막대 반영 (색상은 PieChart와 동일 룰) */
    private void fetchGoalAndApply(String ym) {
        // 이전 콜이 있으면 취소
        if (ratioCall != null) { ratioCall.cancel(); ratioCall = null; }

        ratioCall = analyticsRepo.getCategoryRatio(ym);
        ratioCall.enqueue(new Callback<CategoryRatioResponse>() {
            @Override public void onResponse(Call<CategoryRatioResponse> call, Response<CategoryRatioResponse> res) {
                if (!isAdded() || getView() == null) { ratioCall = null; return; }

                if (!res.isSuccessful() || res.body() == null) {
                    if (goalBarTrack != null)
                        renderStackedGoalBar(monthGoal, lastMonthSpentCached, previewSegments);
                    ratioCall = null;
                    return;
                }

                CategoryRatioResponse body = res.body();

                long spentFromApi = Math.max(0L, body.spent);
                Long goalFromApi  = body.goalAmount;
                monthGoal = (goalFromApi == null) ? 0L : Math.max(0L, goalFromApi);

                lastMonthSpentCached = spentFromApi;
                if (tvGoalSpent  != null) tvGoalSpent.setText(KoreanMoney.format(spentFromApi));
                if (tvGoalTarget != null) tvGoalTarget.setText(KoreanMoney.format(monthGoal));

                renderGoalBarWithCategories(monthGoal, spentFromApi, body.items);
                ratioCall = null;
            }

            @Override public void onFailure(Call<CategoryRatioResponse> call, Throwable t) {
                if (!isAdded() || getView() == null) { ratioCall = null; return; }
                renderStackedGoalBar(monthGoal, lastMonthSpentCached, previewSegments);
                ratioCall = null;
            }
        });
    }

    /** 서버 월 내역을 불러 달력 셀(일자별) 합계를 주입 */
    private void fetchAndApplyMonthEntries(String ym) {
        // 이전 콜이 있으면 취소
        if (monthCall != null) { monthCall.cancel(); monthCall = null; }

        monthCall = ledgerRepo.getMonth(ym);
        monthCall.enqueue(new Callback<LedgerMonthResponse>() {
            @Override
            public void onResponse(Call<LedgerMonthResponse> call, Response<LedgerMonthResponse> res) {
                if (!isAdded() || getView() == null) { monthCall = null; return; }
                if (!res.isSuccessful() || res.body() == null) { monthCall = null; return; }

                LedgerMonthResponse data = res.body();
                List<LedgerEntryDto> list = (data.getEntries() != null)
                        ? data.getEntries()
                        : java.util.Collections.emptyList();

                // A) 상단 월 요약: 서버 값 그대로
                if (tvMonthIncome  != null) tvMonthIncome.setText(KoreanMoney.format(Math.max(0, data.getTotalIncome())));
                if (tvMonthExpense != null) tvMonthExpense.setText(KoreanMoney.format(Math.max(0, data.getTotalExpense())));
                if (tvMonthNet != null) {
                    long net = data.getBalance();
                    tvMonthNet.setText(net < 0 ? "-" + KoreanMoney.format(Math.abs(net))
                            : KoreanMoney.format(net));
                    tvMonthNet.setTextColor(net >= 0 ? Color.parseColor("#2A86FF")
                            : Color.parseColor("#C5463F"));
                }

                // B) 날짜별 합계 맵: key="YYYY-MM-DD", val[0]=income(+), val[1]=expense(+)
                Map<String, long[]> daily = new HashMap<>();
                for (LedgerEntryDto it : list) {
                    if (it == null) continue;
                    String date = extractDateYYYYMMDD(it);
                    if (date == null) continue;

                    long amt = it.getAmount();    // 서버가 부호 적용: INCOME=+, EXPENSE=-
                    long inc = (amt >= 0 ? amt : 0);
                    long exp = (amt <  0 ? -amt : 0);

                    long[] pair = daily.get(date);
                    if (pair == null) pair = new long[]{0L, 0L};
                    pair[0] += inc;
                    pair[1] += exp;
                    daily.put(date, pair);
                }

                // C) 어댑터에 주입 → 각 날짜 밑 “수입/지출” 표시 갱신
                if (adapter != null) {
                    adapter.updateTotals(daily);
                } else if (rvCalendar.getAdapter() != null) {
                    rvCalendar.getAdapter().notifyDataSetChanged();
                }
                monthCall = null;
            }

            @Override public void onFailure(Call<LedgerMonthResponse> call, Throwable t) {
                // 취소로 떨어진 실패는 무시
                monthCall = null;
            }
        });
    }

    /** DTO에서 yyyy-MM-dd 추출 */
    @Nullable
    private String extractDateYYYYMMDD(LedgerEntryDto dto) {
        if (dto == null) return null;
        String s = dto.getDateTime();
        if (s == null) return null;
        return (s.length() >= 10) ? s.substring(0, 10) : null;
    }

    /** 파이차트와 동일한 카테고리 색으로 미니 막대 렌더링 */
    private void renderGoalBarWithCategories(long goal, long spent, List<CategoryRatioResponse.Item> items) {
        if (goalBarTrack == null) return;
        goalBarTrack.setVisibility(View.VISIBLE);
        goalBarTrack.removeAllViews();

        // 부모(track)는 weight 합 = goal
        LinearLayout spentBar = new LinearLayout(requireContext());
        spentBar.setOrientation(LinearLayout.HORIZONTAL);
        spentBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(spent, 0)));

        View remainSpacer = new View(requireContext());
        remainSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(goal - spent, 0)));

        goalBarTrack.addView(spentBar);
        goalBarTrack.addView(remainSpacer);

        if (items == null || items.isEmpty() || spent <= 0) return;

        // 카테고리별로 spentBar 내부를 다시 weight로 분할
        List<CategoryRatioResponse.Item> list = new ArrayList<>(items);
        list.sort((a, b) -> Long.compare(b.expense, a.expense));

        long totalExpense = 0L;
        for (CategoryRatioResponse.Item it : list) totalExpense += Math.max(0L, it.expense);
        if (totalExpense <= 0L) return;

        for (int i = 0; i < list.size(); i++) {
            CategoryRatioResponse.Item it = list.get(i);
            long v = Math.max(0L, it.expense);
            if (v == 0) continue;

            // spentBar의 weight 합 = spent 로 맞추기 위해 비례 가중치 사용
            float w = (float) v / (float) totalExpense * (float) spent;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, w);

            View part = new View(requireContext());
            part.setLayoutParams(lp);

            boolean first = (spentBar.getChildCount() == 0);
            boolean last  = (i == list.size() - 1);

            int color = CategoryColors.bg(requireContext(), it.category);
            part.setBackground(makeRoundedSegment(color, first, last));
            spentBar.addView(part);
        }
    }

    /** 네트워크 실패 시의 단순 프리뷰(고정 팔레트) */
    private void renderStackedGoalBar(long goal, long spent, int[] seg) {
        if (goalBarTrack == null) return;
        goalBarTrack.setVisibility(View.VISIBLE);
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
        if (total <= 0 || spent <= 0) return;

        long cap = spent; // spentBar의 weight 합

        for (int i = 0; i < seg.length; i++) {
            long amt = seg[i];
            if (amt <= 0) continue;

            // seg들의 합(total)을 spent로 스케일링
            float w = (float) amt / (float) total * (float) cap;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, w);
            View part = new View(requireContext());
            part.setLayoutParams(lp);

            boolean first = (spentBar.getChildCount() == 0);
            boolean last  = (i == seg.length - 1);
            part.setBackground(makeRoundedSegment(colors[i % colors.length], first, last));

            spentBar.addView(part);
        }
    }

    /** 년·월 선택 다이얼로그 */
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

    private MonthTotals calcMonthlyTotals(List<LedgerDayData> list) {
        long income = 0, expense = 0;
        if (list != null) {
            for (LedgerDayData d : list) {
                if (d == null) continue;
                // 가능한 필드 이름을 모두 시도
                long in  = reflectGetLong(d, "income", 0L);
                if (in == 0L)  in  = reflectGetLong(d, "in", 0L);
                long out = reflectGetLong(d, "expense", 0L);
                if (out == 0L) out = reflectGetLong(d, "out", 0L);
                income  += Math.max(0, in);
                expense += Math.max(0, out);
            }
        }
        return new MonthTotals(income, expense);
    }

    private long reflectGetLong(Object obj, String field, long def) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object v = f.get(obj);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Throwable ignore) {}
        return def;
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
}
