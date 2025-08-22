package com.moneybuddy.moneylog.activity;

// ✅ minSdk 24 대응 - LocalDate 대신 Calendar 사용

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.model.Transaction;
import com.moneybuddy.moneylog.adapter.LedgerDetailAdapter;
import com.moneybuddy.moneylog.util.CalendarUtils;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Intent;

// ▼▼▼ 백엔드 연동 추가 import
import com.moneybuddy.moneylog.repository.LedgerRepository;
import com.moneybuddy.moneylog.dto.ledger.LedgerDayResponse;
import com.moneybuddy.moneylog.dto.ledger.LedgerEntryDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.moneybuddy.moneylog.model.Transaction.Type;
import androidx.core.content.ContextCompat;

public class LedgerDetailActivity extends AppCompatActivity {

    private TextView tvDateTitle, tvDayTotal;
    private RecyclerView rvDetails;
    private Calendar selectedDate;
    private final SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

    // ▼▼▼ 레포지토리 추가
    private LedgerRepository ledgerRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_detail);
        tvDateTitle = findViewById(R.id.tv_detail_date);
        tvDayTotal  = findViewById(R.id.tv_day_total);
        rvDetails   = findViewById(R.id.rv_ledger_detail);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 1) 선택 날짜 수신 (예: "2025-07-05")
        String selectedDateStr = getIntent().getStringExtra("selected_date");
        selectedDate = Calendar.getInstance();
        try {
            selectedDate.setTime(iso.parse(selectedDateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 2) 상단 제목
        updateTitle(selectedDate);

        // 3) 주간 날짜 칩 UI
        setupWeekDays(selectedDate);

        // 4) 레포지토리 준비(토큰 가져오는 부분은 앱 로직에 맞게 교체)
        ledgerRepo = new LedgerRepository(this, token());

        // 5) 최초 로드
        loadDay(iso.format(selectedDate.getTime()));

        // +) 작성 버튼
        ImageView btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(LedgerDetailActivity.this, LedgerWriteActivity.class);
            startActivity(intent);
        });
    }

    // 앱 저장소에서 JWT 가져오는 부분을 실제 구현으로 교체하세요
    private String token() {
        return getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", "");
    }

    private void updateTitle(Calendar date) {
        tvDateTitle.setText(CalendarUtils.formatDate(date));
    }

    private void setupWeekDays(Calendar base) {
        LinearLayout container = findViewById(R.id.layout_week_days);
        container.removeAllViews();

        Calendar start = CalendarUtils.getWeekStart(base);
        for (int i = 0; i < 7; i++) {
            Calendar day = CalendarUtils.addDays(start, i);

            TextView chip = new TextView(this);
            chip.setText(String.valueOf(CalendarUtils.getDay(day)));
            chip.setTextSize(14);
            chip.setGravity(Gravity.CENTER);
            int hPad = dp(12), vPad = dp(6);
            chip.setPadding(hPad, vPad, hPad, vPad);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            chip.setLayoutParams(lp);

            if (day.get(Calendar.YEAR) == base.get(Calendar.YEAR) &&
                    day.get(Calendar.MONTH) == base.get(Calendar.MONTH) &&
                    day.get(Calendar.DAY_OF_MONTH) == base.get(Calendar.DAY_OF_MONTH)) {
                chip.setBackgroundResource(R.drawable.bg_day_chip_selected);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_day_chip_unselected);
                chip.setTextColor(0xFF444444);
            }

            chip.setOnClickListener(v -> {
                selectedDate = day;
                updateTitle(selectedDate);
                setupWeekDays(selectedDate);

                // ▼▼▼ 날짜 바뀔 때 서버에서 해당 일 다시 로드
                loadDay(iso.format(selectedDate.getTime()));
            });

            container.addView(chip);
        }
    }

    // ▼▼▼ 여기서부터 "일별 로드/바인딩" 추가

    /** 날짜별 데이터 로드 */
    private void loadDay(String date) {
        // 이전 값 남지 않도록 즉시 0 표시 + 기본색(#333333)
        setDayTotal(0);

        ledgerRepo.getDay(date).enqueue(new Callback<LedgerDayResponse>() {
            @Override public void onResponse(Call<LedgerDayResponse> call, Response<LedgerDayResponse> res) {
                if (!res.isSuccessful() || res.body() == null) return;
                bindDay(res.body());
            }
            @Override public void onFailure(Call<LedgerDayResponse> call, Throwable t) { /* 0 유지 */ }
        });
    }



    /** 합계 텍스트/색상 동시 갱신 */
    private void setDayTotal(long net) {
        if (tvDayTotal == null) return;

        tvDayTotal.setText(com.moneybuddy.moneylog.util.KoreanMoney.format(net));

        int color;
        if (net > 0) {
            color = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
        } else if (net < 0) {
            color = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        } else {
            color = Color.parseColor("#333333");
        }
        tvDayTotal.setTextColor(color);
    }



    /** 서버 응답을 화면에 바인딩 */
    private void bindDay(LedgerDayResponse dto) {
        long income  = dto.totalIncome  == null ? 0 : dto.totalIncome;
        long expense = dto.totalExpense == null ? 0 : dto.totalExpense;
        long net     = income - expense;
        tvDayTotal.setText(KoreanMoney.format(net));

        List<Transaction> list = new ArrayList<>();
        if (dto.entries != null) {
            for (LedgerEntryDto e : dto.entries) {
                // 1) 시간 "HH:mm"
                String time = (e.dateTime != null && e.dateTime.length() >= 16)
                        ? e.dateTime.substring(11, 16) : "";

                // 2) 제목(상호명 우선, 없으면 카테고리/메모)
                String title = !isEmpty(e.store) ? e.store
                        : !isEmpty(e.description) ? e.description
                        : !isEmpty(e.category) ? e.category
                        : "";

                // 3) 카테고리/자산
                String category = e.category == null ? "" : e.category;
                String asset    = e.asset == null ? "" : e.asset;

                // 4) 금액/타입 (서버: 지출 음수, 수입 양수 응답)
                int amountInt   = safeToInt(e.amount);  // long -> int 안전 변환
                Type type       = (e.amount >= 0) ? Type.INCOME : Type.EXPENSE;

                // 5) groupId는 화면 섹션 구분용 – 필요에 맞게 값 지정
                String groupId  = "DAY";

                // ★ 새 생성자 사용: (time, title, category, asset, amount, type, groupId)
                Transaction t = new Transaction(time, title, category, asset, amountInt, type, groupId);
                list.add(t);
            }
        }
        // ✔ 합계 숫자/색상 동시 반영
        setDayTotal(net);
        bindList(list);
    }

    private void bindList(List<Transaction> list) {
        rvDetails.setLayoutManager(new LinearLayoutManager(this));
        rvDetails.setAdapter(new LedgerDetailAdapter(list));
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }

    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }

    private int safeToInt(long v) {
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (v < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) v;
    }

}
