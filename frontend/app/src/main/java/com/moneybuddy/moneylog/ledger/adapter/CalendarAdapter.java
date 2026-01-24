package com.moneybuddy.moneylog.ledger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.VH> {

    public interface OnDayClickListener { void onDayClick(LedgerDayData item); }

    private final List<LedgerDayData> items = new ArrayList<>();
    @Nullable private OnDayClickListener listener;
    @Nullable private Context appCtx;
    @Nullable private String tag;

    // --- 생성자 오버로드(호출부 호환) ---
    public CalendarAdapter() { }
    public CalendarAdapter(@Nullable Context ctx) {
        this.appCtx = (ctx == null ? null : ctx.getApplicationContext());
    }
    public CalendarAdapter(@Nullable Context ctx, @Nullable List<LedgerDayData> initial) {
        this(ctx);
        if (initial != null) items.addAll(initial);
    }
    // (Context, List<LedgerDayData>, String) 생성자 – 기존 호출부 호환용
    public CalendarAdapter(@Nullable Context ctx,
                           @Nullable List<LedgerDayData> initial,
                           @Nullable String tag) {
        this(ctx, initial);
        this.tag = tag;
    }
    // 선택: 리스너 버전
    public CalendarAdapter(@Nullable Context ctx,
                           @Nullable List<LedgerDayData> initial,
                           @Nullable OnDayClickListener l) {
        this(ctx, initial);
        this.listener = l;
    }

    // --- 데이터 주입 ---
    public void submitDays(@Nullable List<LedgerDayData> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }
    public void submit(@Nullable List<LedgerDayData> list) { submitDays(list); }

    public void setOnDayClickListener(@Nullable OnDayClickListener l) { this.listener = l; }

    /** 날짜별 합계 주입: key=YYYY-MM-DD, val[0]=income(+), val[1]=expense(+) */
    public void updateTotals(@Nullable Map<String, long[]> daily) {
        if (daily == null) return;
        for (LedgerDayData d : items) {
            long inc = 0, exp = 0;
            long[] pair = daily.get(d.getDate());
            if (pair != null) { inc = pair[0]; exp = pair[1]; }
            d.setIncome(inc);
            d.setExpense(exp);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LedgerDayData d = items.get(position);

        h.tvDayNumber.setText(String.valueOf(d.getDayOfMonth()));
        h.itemView.setAlpha(d.isInThisMonth() ? 1f : 0.45f);

        long inc = d.getIncome();
        long exp = d.getExpense();

        if (inc > 0) {
            h.tvIncomeSmall.setVisibility(View.VISIBLE);
            h.tvIncomeSmall.setText("+" + KoreanMoney.format(inc));
        } else {
            h.tvIncomeSmall.setVisibility(View.GONE);
        }

        if (exp > 0) {
            h.tvExpenseSmall.setVisibility(View.VISIBLE);
            h.tvExpenseSmall.setText("-" + KoreanMoney.format(exp));
        } else {
            h.tvExpenseSmall.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onDayClick(d); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDayNumber, tvIncomeSmall, tvExpenseSmall;
        VH(@NonNull View v) {
            super(v);
            tvDayNumber    = v.findViewById(R.id.tvDayNumber);
            tvIncomeSmall  = v.findViewById(R.id.tvIncomeSmall);
            tvExpenseSmall = v.findViewById(R.id.tvExpenseSmall);
        }
    }
}
