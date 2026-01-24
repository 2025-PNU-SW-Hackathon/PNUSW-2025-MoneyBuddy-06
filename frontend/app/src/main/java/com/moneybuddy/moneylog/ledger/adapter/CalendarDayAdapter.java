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
import com.moneybuddy.moneylog.ledger.model.DayCell;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.ArrayList;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.VH> {

    public interface OnDayClickListener { void onDayClick(DayCell item); }

    private final List<DayCell> items = new ArrayList<>();
    @Nullable private OnDayClickListener listener;
    @Nullable private Context appCtx; // 필요 시 사용

    // ===== 생성자 오버로드 (호출부 호환) =====
    public CalendarDayAdapter() { }
    public CalendarDayAdapter(@Nullable Context ctx) { this.appCtx = ctx != null ? ctx.getApplicationContext() : null; }
    public CalendarDayAdapter(@Nullable List<DayCell> initial, @Nullable OnDayClickListener l) {
        if (initial != null) items.addAll(initial);
        this.listener = l;
    }
    public CalendarDayAdapter(@Nullable Context ctx, @Nullable List<DayCell> initial, @Nullable OnDayClickListener l) {
        this(ctx);
        if (initial != null) items.addAll(initial);
        this.listener = l;
    }

    // 호출부에서 쓰던 이름을 그대로 지원
    public void submitDays(@Nullable List<DayCell> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }
    public void submit(@Nullable List<DayCell> list) { submitDays(list); }

    public void setOnDayClickListener(@Nullable OnDayClickListener l) { this.listener = l; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DayCell d = items.get(position);
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
