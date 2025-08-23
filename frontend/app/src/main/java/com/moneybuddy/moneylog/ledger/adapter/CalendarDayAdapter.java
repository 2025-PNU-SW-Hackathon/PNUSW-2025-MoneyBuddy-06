package com.moneybuddy.moneylog.ledger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 캘린더 날짜 셀 어댑터 (막대 제거 버전)
 * - item_calendar_day.xml 은 tv_day, tv_total 두 개의 TextView만 사용
 * - 일별 합계(total)는 submitTotals(...) 로 전달(없으면 0 표시)
 * - 색상 규칙: 0=#333333, 양수=holo_blue_dark, 음수=holo_red_dark
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.VH> {

    public static class DayCell {
        public final Integer day; // null 이면 빈칸 셀
        public DayCell(Integer d) { day = d; }
    }

    private final LayoutInflater inflater;
    private final Context ctx;
    private final List<DayCell> days = new ArrayList<>();

    // 일자 -> 합계 (원 단위, 지출 음수/수입 양수)
    private Map<Integer, Integer> dayTotals = Collections.emptyMap();

    private final NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);

    public CalendarDayAdapter(Context ctx) {
        this.ctx = ctx;
        this.inflater = LayoutInflater.from(ctx);
    }

    /** 6x7(42칸) 셀 목록 주입 */
    public void submitDays(List<DayCell> items) {
        days.clear();
        if (items != null) days.addAll(items);
        notifyDataSetChanged();
    }

    /** 일자별 합계(원)를 주입. 없으면 0으로 표기됨 */
    public void submitTotals(Map<Integer, Integer> totals) {
        this.dayTotals = (totals == null ? Collections.emptyMap() : totals);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(inflater.inflate(R.layout.item_calendar_day, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DayCell cell = days.get(position);

        if (cell.day == null) {
            // 빈칸
            h.tvDay.setText("");
            h.tvTotal.setText("");
            h.tvTotal.setTextColor(0xFF888888); // 기본
            return;
        }

        // 날짜
        h.tvDay.setText(String.valueOf(cell.day));

        // 합계
        int total = 0;
        if (dayTotals != null && dayTotals.containsKey(cell.day)) {
            Integer v = dayTotals.get(cell.day);
            total = (v == null ? 0 : v);
        }

        // 텍스트
        if (total > 0) {
            h.tvTotal.setText("+" + nf.format(total));
            h.tvTotal.setTextColor(ctx.getColor(android.R.color.holo_blue_dark));
        } else if (total < 0) {
            h.tvTotal.setText(nf.format(total)); // 음수 그대로
            h.tvTotal.setTextColor(ctx.getColor(android.R.color.holo_red_dark));
        } else {
            h.tvTotal.setText("0");
            h.tvTotal.setTextColor(0xFF333333);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDay;
        final TextView tvTotal;
        VH(@NonNull View v) {
            super(v);
            tvDay   = v.findViewById(R.id.tv_day);
            tvTotal = v.findViewById(R.id.tv_total);
        }
    }
}
