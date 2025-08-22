package com.moneybuddy.moneylog.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.dto.analytics.CategoryRatioResponse;
import com.moneybuddy.moneylog.dto.budget.BudgetGoalDto;
import com.moneybuddy.moneylog.repository.AnalyticsRepository;
import com.moneybuddy.moneylog.repository.BudgetRepository;
import com.moneybuddy.moneylog.ui.CategoryColors;
import com.moneybuddy.moneylog.view.PieChartView;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * - 상단 날짜(tvv_date) 클릭 → 년/월 NumberPicker → 해당 월 데이터 로드
 * - 목표 설정 버튼(tv_set_goal) → 서버 PUT /budget-goal
 * - /analytics/category-ratio?ym=YYYY-MM 한 번으로 목표/실사용/카테고리 비율 갱신
 */
public class GraphActivity extends AppCompatActivity {

    // ----- Views -----
    private TextView tvDate, tvMonthGoal, tvMonthNet, tvTitle, tvUserName, tvBetween;
    private PieChartView pie;
    private LinearLayout legend;

    // ----- Repositories -----
    private AnalyticsRepository analyticsRepo;
    private BudgetRepository budgetRepo;

    // ----- State -----
    private final Calendar currentMonth = Calendar.getInstance(); // 선택 중인 연/월

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // ----- View binding -----
        ImageView btnBack = findViewById(R.id.btn_back);
        tvTitle      = findViewById(R.id.tv_title);
        tvDate       = findViewById(R.id.tv_date);          // "YYYY-MM"
        tvUserName   = findViewById(R.id.user_name);        // MoBTI 별명
        tvMonthGoal  = findViewById(R.id.user_month_goal);  // 목표 금액 표시
        tvMonthNet   = findViewById(R.id.tv_month_net);     // 소비 합계 표시
        tvBetween    = safeFindTv(R.id.tv_between_of);      // " 원 중 " (있으면 제어)
        pie          = findViewById(R.id.pie);
        legend       = findViewById(R.id.legend_container);

        if (btnBack != null) btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // ----- Repos -----
        analyticsRepo = new AnalyticsRepository(this, token());
        budgetRepo    = new BudgetRepository(this, token());

        // ----- User nickname (MoBTI) -----
        if (tvUserName != null) tvUserName.setText(loadMobtiNickname());

        // ----- Init current month (from intent or now) -----
        int year  = getIntent().getIntExtra("year",  -1);
        int month = getIntent().getIntExtra("month", -1);
        if (year <= 0 || month < 1 || month > 12) {
            Calendar now = Calendar.getInstance();
            year  = now.get(Calendar.YEAR);
            month = now.get(Calendar.MONTH) + 1;
        }
        currentMonth.set(Calendar.YEAR, year);
        currentMonth.set(Calendar.MONTH, month - 1);
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        updateYmText();

        // ----- Listeners -----
        tvDate.setOnClickListener(v -> showYearMonthPicker());
        View btnSetGoal = findViewById(R.id.tv_set_goal);
        if (btnSetGoal != null) btnSetGoal.setOnClickListener(v -> showSetGoalDialog());

        // ----- First load -----
        loadMonth(year, month);
    }

    /** 실제 앱의 저장소에서 JWT를 가져오도록 교체 */
    private String token() {
        // 예시: SharedPreferences "auth"에서 jwt 읽기 (없으면 빈 문자열)
        return getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", "");
    }

    /** YYYY-MM 텍스트 갱신 */
    private void updateYmText() {
        int y  = currentMonth.get(Calendar.YEAR);
        int m  = currentMonth.get(Calendar.MONTH) + 1;
        tvDate.setText(String.format(Locale.KOREAN, "%04d-%02d", y, m));
    }

    /** 년·월만 고르는 다이얼로그 */
    private void showYearMonthPicker() {
        final int curYear  = currentMonth.get(Calendar.YEAR);
        final int curMonth = currentMonth.get(Calendar.MONTH) + 1;

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

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        container.addView(yearPicker, lp);
        container.addView(monthPicker, lp);

        new AlertDialog.Builder(this)
                .setTitle("년/월 선택")
                .setView(container)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", (d, w) -> {
                    currentMonth.set(Calendar.YEAR, yearPicker.getValue());
                    currentMonth.set(Calendar.MONTH, monthPicker.getValue() - 1);
                    currentMonth.set(Calendar.DAY_OF_MONTH, 1);
                    updateYmText();
                    loadMonth(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH) + 1);
                })
                .show();
    }

    /** 선택 월 데이터 로드 → 상단 문장/목표/파이/범례 갱신 */
    private void loadMonth(int year, int month) {
        String ym = String.format(Locale.KOREAN, "%04d-%02d", year, month);

        analyticsRepo.getCategoryRatio(ym).enqueue(new Callback<CategoryRatioResponse>() {
            @Override public void onResponse(Call<CategoryRatioResponse> call, Response<CategoryRatioResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(GraphActivity.this, "그래프 데이터 실패(" + res.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                CategoryRatioResponse dto = res.body();

                // (1) 상단 목표/실사용 문장
                long spent = dto.spent;
                Long goal  = dto.goalAmount; // null 가능

                if (tvMonthNet != null)  tvMonthNet.setText(KoreanMoney.format(spent));
                if (goal == null) {
                    // 목표가 없으면 "원 중" 문구/목표 값을 가림
                    if (tvBetween != null) tvBetween.setVisibility(View.GONE);
                    if (tvMonthGoal != null) tvMonthGoal.setVisibility(View.GONE);
                } else {
                    if (tvBetween != null) tvBetween.setVisibility(View.VISIBLE);
                    if (tvMonthGoal != null) {
                        tvMonthGoal.setVisibility(View.VISIBLE);
                        tvMonthGoal.setText(KoreanMoney.format(goal));
                    }
                }

                // (2) 파이차트 데이터 (금액 내림차순)
                LinkedHashMap<String, Long> sorted = new LinkedHashMap<>();
                if (dto.items != null) {
                    List<CategoryRatioResponse.Item> items = new ArrayList<>(dto.items);
                    items.sort((a, b) -> Long.compare(b.expense, a.expense));
                    for (CategoryRatioResponse.Item it : items) sorted.put(it.category, it.expense);
                }
                if (pie != null) pie.setData(sorted);
                if (legend != null) renderLegend(sorted, spent);
            }

            @Override public void onFailure(Call<CategoryRatioResponse> call, Throwable t) {
                Toast.makeText(GraphActivity.this, "그래프 데이터 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 목표 설정 다이얼로그 */
    private void showSetGoalDialog() {
        final int y = currentMonth.get(Calendar.YEAR);
        final int m = currentMonth.get(Calendar.MONTH) + 1;

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(12) });
        input.setHint("예: 500000");
        int pad = Math.round(getResources().getDisplayMetrics().density * 16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle(String.format(Locale.KOREAN, "%04d-%02d 목표 금액", y, m))
                .setView(input)
                .setNegativeButton("취소", null)
                .setPositiveButton("저장", (d, w) -> {
                    String s = input.getText() == null ? "" : input.getText().toString().trim();
                    if (s.isEmpty()) { Toast.makeText(this, "금액을 입력해 주세요", Toast.LENGTH_SHORT).show(); return; }
                    long goal;
                    try { goal = Long.parseLong(s.replaceAll("[^0-9]", "")); }
                    catch (NumberFormatException e) { Toast.makeText(this, "숫자만 입력해 주세요", Toast.LENGTH_SHORT).show(); return; }
                    saveGoalToServer(y, m, goal);
                })
                .show();
    }

    private void saveGoalToServer(int y, int m, long goal) {
        String ym = String.format(Locale.KOREAN, "%04d-%02d", y, m);
        Call<BudgetGoalDto> c = budgetRepo.putGoal(ym, goal);
        c.enqueue(new Callback<BudgetGoalDto>() {
            @Override public void onResponse(Call<BudgetGoalDto> call, Response<BudgetGoalDto> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(GraphActivity.this, "목표 저장 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                BudgetGoalDto data = res.body();
                if (tvMonthGoal != null) {
                    tvMonthGoal.setVisibility(View.VISIBLE);
                    tvMonthGoal.setText(KoreanMoney.format(data.amount == null ? 0 : data.amount));
                }
                if (tvBetween != null) tvBetween.setVisibility(View.VISIBLE);
                Toast.makeText(GraphActivity.this, "목표가 저장되었습니다.", Toast.LENGTH_SHORT).show();

                // 저장 후 해당 월 데이터 재로딩 (spent/파이도 갱신)
                loadMonth(y, m);
            }
            @Override public void onFailure(Call<BudgetGoalDto> call, Throwable t) {
                Toast.makeText(GraphActivity.this, "목표 저장 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 카테고리별 합계 목록 → 리스트 UI */
    private void renderLegend(Map<String, Long> data, long all) {
        legend.removeAllViews();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            String label = e.getKey();
            long amount  = e.getValue() == null ? 0 : e.getValue();
            int percent  = all > 0 ? Math.round(100f * amount / all) : 0;

            View row = getLayoutInflater().inflate(R.layout.item_category_breakdown, legend, false);

            View percentBg = row.findViewById(R.id.percent_bg);
            TextView tvPercent = row.findViewById(R.id.tv_percent);
            tvPercent.setText(percent + "%");

            int base = CategoryColors.bg(this, label);
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setCornerRadius(dp(4));
            gd.setColor(base);
            percentBg.setBackground(gd);
            tvPercent.setTextColor(0xFFFFFFFF);

            ((TextView) row.findViewById(R.id.tv_label)).setText(label);
            ((TextView) row.findViewById(R.id.tv_amount)).setText(KoreanMoney.format(amount) + "원");

            legend.addView(row);
        }
    }

    // ----- Utils -----
    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

    private TextView safeFindTv(int id) {
        try { return findViewById(id); } catch (Exception ignore) { return null; }
    }

    private String loadMobtiNickname() {
        // 예시: SharedPreferences에서 mobti 별명을 읽음 (없으면 기본 "실속꾼")
        SharedPreferences sp = getSharedPreferences("profile", MODE_PRIVATE);
        String nick = sp.getString("mobtiNickname", null);
        if (nick != null && !nick.isEmpty()) return nick;

        String mobti = sp.getString("mobtiType", "S1");
        switch (mobti) {
            case "S1": return "실속꾼";
            case "P1": return "플래너";
            case "F1": return "감성소비러";
            case "C1": return "절약가";
            default:   return "실속꾼";
        }
    }
}
