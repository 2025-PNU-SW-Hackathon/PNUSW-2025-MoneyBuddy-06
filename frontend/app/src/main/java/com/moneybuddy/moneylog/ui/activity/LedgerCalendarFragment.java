package com.moneybuddy.moneylog.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.data.dto.ledger.LedgerMonthResponse;
import com.moneybuddy.moneylog.data.repository.LedgerRepository;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LedgerCalendarFragment extends Fragment {
    private TextView tvIncome, tvExpense, tvNet, tvYearMonth;
    private LedgerRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle s) {
        return inf.inflate(R.layout.activity_calendar_ledger, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        tvIncome = v.findViewById(R.id.tv_month_income);
        tvExpense = v.findViewById(R.id.tv_month_expense);
        tvNet = v.findViewById(R.id.tv_month_net);
        tvYearMonth = v.findViewById(R.id.tv_year_month);

        // TODO: 실제 보관 중인 JWT로 교체
        String token = "YOUR_JWT_TOKEN";
        repo = new LedgerRepository(requireContext(), token);

        // 예시: 현재 2025-08 월 데이터 로드
        loadMonth("2025-08");
    }

    private void loadMonth(String ym) {
        repo.getMonth(ym).enqueue(new Callback<LedgerMonthResponse>() {
            @Override
            public void onResponse(Call<LedgerMonthResponse> call, Response<LedgerMonthResponse> res) {
                if (!isAdded()) return;

                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(requireContext(), "월 요약 불러오기 실패(" + res.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }

                LedgerMonthResponse dto = res.body();

                NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
                tvIncome.setText(nf.format(dto.totalIncome));
                tvExpense.setText(nf.format(dto.totalExpense));
                tvNet.setText(nf.format(dto.balance));

                if (dto.yearMonth != null && dto.yearMonth.length() == 7) {
                    String[] p = dto.yearMonth.split("-");
                    String monthNoZero = p[1].replaceFirst("^0", "");
                    tvYearMonth.setText(p[0] + "년 " + monthNoZero + "월");
                }
            }

            @Override
            public void onFailure(Call<LedgerMonthResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "월 요약 불러오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
