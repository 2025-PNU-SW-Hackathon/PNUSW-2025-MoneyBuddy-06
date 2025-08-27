package com.moneybuddy.moneylog.ledger.activity;

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
import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.ledger.dto.response.BudgetGoalDto;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.repository.AnalyticsRepository;
import com.moneybuddy.moneylog.ledger.repository.BudgetRepository;
import com.moneybuddy.moneylog.ledger.ui.CategoryColors;
import com.moneybuddy.moneylog.ledger.view.PieChartView;
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

/** 월별 카테고리 비율/목표를 불러 파이차트+범례를 표시 */
public class GraphActivity extends AppCompatActivity {

    // Views
    private TextView tvDate, tvMonthGoal, tvMonthNet, tvTitle, tvUserName, tvBetween;
    private PieChartView pie;
    private LinearLayout legend;

    // Repos
    private AnalyticsRepository analyticsRepo;
    private BudgetRepository budgetRepo;

    // State
    private final Calendar currentMonth = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // bind
        ImageView btnBack = findViewById(R.id.btn_back);
        tvTitle      = findViewById(R.id.tv_title);
        tvDate       = findViewById(R.id.tv_date);
        tvUserName   = findViewById(R.id.user_name);
        tvMonthGoal  = findViewById(R.id.user_month_goal);
        tvMonthNet   = findViewById(R.id.tv_month_net);
        tvBetween    = safeFindTv(R.id.tv_between_of);
        pie          = findViewById(R.id.pie);
        legend       = findViewById(R.id.legend_container);
        if (btnBack != null) btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // repos (토큰 파라미터는 내부에서 인터셉터가 쓰면 무시되어도 OK)
        String tk = token();
        analyticsRepo = new AnalyticsRepository(this, tk);
        budgetRepo    = new BudgetRepository(this, tk);

        // 상단 닉네임
        if (tvUserName != null) tvUserName.setText(loadMobtiNickname());

        // ym 인텐트 우선 → 없으면 year/month → 없으면 오늘
        boolean loaded = false;
        String ymArg = getIntent().getStringExtra("ym");
        if (ymArg != null && ymArg.matches("\\d{4}-\\d{2}")) {
            int y = Integer.parseInt(ymArg.substring(0, 4));
            int m = Integer.parseInt(ymArg.substring(5, 7));
            setMonth(y, m);
            loadMonth(ymArg);
            loaded = true;
        }
        if (!loaded) {
            int y = getIntent().getIntExtra("year", -1);
            int m = getIntent().getIntExtra("month", -1);
            if (y <= 0 || m < 1 || m > 12) {
                Calendar now = Calendar.getInstance();
                y = now.get(Calendar.YEAR);
                m = now.get(Calendar.MONTH) + 1;
            }
            setMonth(y, m);
            loadMonth(y, m);
        }

        // UI actions
        if (tvDate != null) tvDate.setOnClickListener(v -> showYearMonthPicker());
        View btnSetGoal = findViewById(R.id.tv_set_goal);
        if (btnSetGoal != null) btnSetGoal.setOnClickListener(v -> showSetGoalDialog());
    }

    private String token() {
        return TokenManager.getInstance(this).getToken();
    }

    private void setMonth(int year, int month) {
        currentMonth.set(Calendar.YEAR, year);
        currentMonth.set(Calendar.MONTH, month - 1);
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        updateYmText();
    }

    private void updateYmText() {
        if (tvDate == null) return;
        int y = currentMonth.get(Calendar.YEAR);
        int m = currentMonth.get(Calendar.MONTH) + 1;
        tvDate.setText(String.format(Locale.KOREAN, "%04d-%02d", y, m));
    }

    private void showYearMonthPicker() {
        final int curYear = currentMonth.get(Calendar.YEAR);
        final int curMonth = currentMonth.get(Calendar.MONTH) + 1;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        int pad = Math.round(getResources().getDisplayMetrics().density * 16);
        container.setPadding(pad, pad, pad, pad);

        NumberPicker yearPicker = new NumberPicker(this);
        yearPicker.setMinValue(curYear - 50);
        yearPicker.setMaxValue(curYear + 50);
        yearPicker.setValue(curYear);

        NumberPicker monthPicker = new NumberPicker(this);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(curMonth);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        container.addView(yearPicker, lp);
        container.addView(monthPicker, lp);

        new AlertDialog.Builder(this)
                .setTitle("년/월 선택")
                .setView(container)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", (d, w) -> {
                    setMonth(yearPicker.getValue(), monthPicker.getValue());
                    loadMonth(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH) + 1);
                })
                .show();
    }

    // --- Load (overloads) ---
    private void loadMonth(int year, int month) {
        loadMonth(String.format(Locale.KOREAN, "%04d-%02d", year, month));
    }

    private void loadMonth(String ym) {
        analyticsRepo.getCategoryRatio(ym).enqueue(new Callback<CategoryRatioResponse>() {
            @Override public void onResponse(Call<CategoryRatioResponse> call, Response<CategoryRatioResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(GraphActivity.this, "그래프 데이터 실패(" + res.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                CategoryRatioResponse dto = res.body();

                long spent = Math.max(0L, dto.spent);
                Long goal = dto.goalAmount == null ? null : Math.max(0L, dto.goalAmount);

                if (tvMonthNet != null)  tvMonthNet.setText(KoreanMoney.format(spent));
                if (goal == null) {
                    if (tvBetween != null) tvBetween.setVisibility(View.GONE);
                    if (tvMonthGoal != null) tvMonthGoal.setVisibility(View.GONE);
                } else {
                    if (tvBetween != null) tvBetween.setVisibility(View.VISIBLE);
                    if (tvMonthGoal != null) {
                        tvMonthGoal.setVisibility(View.VISIBLE);
                        tvMonthGoal.setText(KoreanMoney.format(goal));
                    }
                }

                LinkedHashMap<String, Long> sorted = new LinkedHashMap<>();
                if (dto.items != null) {
                    List<CategoryRatioResponse.Item> items = new ArrayList<>(dto.items);
                    items.sort((a, b) -> Long.compare(b.expense, a.expense));
                    for (CategoryRatioResponse.Item it : items) sorted.put(it.category, Math.max(0L, it.expense));
                }
                if (pie != null) pie.setData(sorted);
                if (legend != null) renderLegend(sorted, spent);
            }
            @Override public void onFailure(Call<CategoryRatioResponse> call, Throwable t) {
                Toast.makeText(GraphActivity.this, "그래프 데이터 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Goal ---
    private void showSetGoalDialog() {
        final int y = currentMonth.get(Calendar.YEAR);
        final int m = currentMonth.get(Calendar.MONTH) + 1;

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
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
        budgetRepo.putGoal(ym, goal).enqueue(new Callback<BudgetGoalDto>() {
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
                loadMonth(ym);
            }
            @Override public void onFailure(Call<BudgetGoalDto> call, Throwable t) {
                Toast.makeText(GraphActivity.this, "목표 저장 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Legend ---
    private void renderLegend(Map<String, Long> data, long all) {
        legend.removeAllViews();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            String label = e.getKey();
            long amount = e.getValue() == null ? 0 : e.getValue();
            int percent = all > 0 ? Math.round(100f * amount / all) : 0;

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

    // Utils
    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }
    private TextView safeFindTv(int id) { try { return findViewById(id); } catch (Exception ignore) { return null; } }

    private String loadMobtiNickname() {
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
