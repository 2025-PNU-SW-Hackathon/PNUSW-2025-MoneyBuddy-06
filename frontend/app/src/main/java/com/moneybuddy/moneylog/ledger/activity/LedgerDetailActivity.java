package com.moneybuddy.moneylog.ledger.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.ledger.adapter.LedgerDetailAdapter;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerDayResponse;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.ledger.model.Transaction;
import com.moneybuddy.moneylog.ledger.model.Transaction.Type;
import com.moneybuddy.moneylog.ledger.repository.LedgerRepository;
import com.moneybuddy.moneylog.util.CalendarUtils;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LedgerDetailActivity extends AppCompatActivity {

    private TextView tvDateTitle, tvDayTotal;
    private RecyclerView rvDetails;
    private Calendar selectedDate;
    private final SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

    private LedgerRepository ledgerRepo;
    private Call<LedgerDayResponse> dayCall; // 진행 중 콜 보관

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_detail);

        tvDateTitle = findViewById(R.id.tv_detail_date);
        tvDayTotal  = findViewById(R.id.tv_day_total);
        rvDetails   = findViewById(R.id.rv_ledger_detail);
        rvDetails.setLayoutManager(new LinearLayoutManager(this));

        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 1) 선택 날짜 수신 — "date" 우선, 없으면 "selected_date" (둘 다 없으면 오늘)
        String dateExtra = getIntent().getStringExtra("date");
        if (dateExtra == null || dateExtra.length() < 10) {
            dateExtra = getIntent().getStringExtra("selected_date");
        }
        selectedDate = Calendar.getInstance();
        try {
            if (dateExtra != null && dateExtra.length() >= 10) {
                selectedDate.setTime(iso.parse(dateExtra));
            }
        } catch (ParseException ignored) { /* 오늘 날짜 유지 */ }

        // 2) 상단 제목
        updateTitle(selectedDate);

        // 3) 주간 날짜 칩 UI
        setupWeekDays(selectedDate);

        // 4) 레포지토리 준비 (SharedPreferences 기반 토큰)
        ledgerRepo = new LedgerRepository(this, token());

        // 5) 최초 로드
        loadDay(iso.format(selectedDate.getTime()));

        // +) 작성 버튼
        ImageView btnAdd = findViewById(R.id.btn_add);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(LedgerDetailActivity.this, LedgerWriteActivity.class);
                startActivity(intent);
            });
        }
    }

    // 앱 저장소에서 JWT 가져오기 (TokenManager 미사용)
    private String token() {
        try {
            SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
            String jwt = sp.getString("jwt", null);     // 우선 키
            if (jwt != null && !jwt.isEmpty()) return jwt;

            // 레거시 호환 (있을 경우)
            String legacy = sp.getString("token", null);
            return legacy == null ? "" : legacy;
        } catch (Exception e) {
            return "";
        }
    }

    private void updateTitle(Calendar date) {
        if (tvDateTitle != null) {
            tvDateTitle.setText(CalendarUtils.formatDate(date));
        }
    }

    private void setupWeekDays(Calendar base) {
        LinearLayout container = findViewById(R.id.layout_week_days);
        if (container == null) return;

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
                loadDay(iso.format(selectedDate.getTime())); // 날짜 변경 시 재호출
            });

            container.addView(chip);
        }
    }

    /** 날짜별 데이터 로드 */
    private void loadDay(String date) {
        // 이전 값 남지 않도록 즉시 0 표시 + 기본색(#333333)
        setDayTotal(0);

        // 진행 중 콜 있으면 취소
        if (dayCall != null) {
            dayCall.cancel();
            dayCall = null;
        }

        dayCall = ledgerRepo.getDay(date);
        dayCall.enqueue(new Callback<LedgerDayResponse>() {
            @Override
            public void onResponse(Call<LedgerDayResponse> call, Response<LedgerDayResponse> res) {
                if (isFinishing() || isDestroyed()) { dayCall = null; return; }
                if (!res.isSuccessful() || res.body() == null) { dayCall = null; return; }
                bindDay(res.body());
                dayCall = null;
            }
            @Override
            public void onFailure(Call<LedgerDayResponse> call, Throwable t) {
                // 취소 등은 무시 (0 유지)
                dayCall = null;
            }
        });
    }

    /** 합계 텍스트/색상 동시 갱신 */
    private void setDayTotal(long net) {
        if (tvDayTotal == null) return;

        tvDayTotal.setText(KoreanMoney.format(net));

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

    private void bindDay(LedgerDayResponse dto) {
        long income  = (dto != null) ? Math.max(0L, dto.getTotalIncome())  : 0L;
        long expense = (dto != null) ? Math.max(0L, dto.getTotalExpense()) : 0L;
        long net     = income - expense;

        setDayTotal(net);

        List<Transaction> list = new ArrayList<>();
        if (dto != null && dto.getEntries() != null) {
            for (LedgerEntryDto e : dto.getEntries()) {
                if (e == null) continue;

                // 1) 시간 "HH:mm"
                String dt = e.getDateTime();
                String time = (dt != null && dt.length() >= 16) ? dt.substring(11, 16) : "";

                // 2) 제목
                String title =
                        !isEmpty(e.getStore())       ? e.getStore() :
                                !isEmpty(e.getDescription()) ? e.getDescription() :
                                        !isEmpty(e.getCategory())    ? e.getCategory() : "";

                // 3) 카테고리/자산
                String category = e.getCategory() != null ? e.getCategory() : "";
                String asset    = e.getAsset()    != null ? e.getAsset()    : "";

                // 4) 금액/타입 (서버 amount는 부호 적용: 수입+, 지출-)
                long amount    = e.getAmount();
                int  amountInt = safeToInt(amount);
                Type type      = (amount >= 0) ? Type.INCOME : Type.EXPENSE;

                String groupId = "DAY";
                Transaction t = new Transaction(time, title, category, asset, amountInt, type, groupId);
                list.add(t);
            }
        }

        bindList(list);
    }

    private void bindList(List<Transaction> list) {
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

    @Override
    protected void onStop() {
        super.onStop();
        if (dayCall != null) { dayCall.cancel(); dayCall = null; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dayCall != null) { dayCall.cancel(); dayCall = null; }
    }
}
