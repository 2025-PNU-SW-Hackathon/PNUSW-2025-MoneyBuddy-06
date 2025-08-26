package com.moneybuddy.moneylog.ledger.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.ledger.activity.LedgerDetailActivity;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final Context context;
    private final List<LedgerDayData> dayDataList;
    private final String yearMonth; // ex: "2025-07"

    public CalendarAdapter(Context context, List<LedgerDayData> dayDataList, String yearMonth) {
        this.context = context;
        this.dayDataList = dayDataList;
        this.yearMonth = yearMonth;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LedgerDayData item = dayDataList.get(position);

        // 빈 칸
        if (item == null || item.isEmpty()) {
            holder.tvDay.setText("");
            holder.tvTotal.setText("");
            holder.itemView.setClickable(false);
            return;
        }

        // 날짜
        holder.tvDay.setText(String.valueOf(item.getDay()));

        // 합계 = 수입 - 지출
        int net = item.getIncome() - item.getExpense();
        String text;
        if (net > 0) {
            text = "+ " + KoreanMoney.format(net);
            holder.tvTotal.setTextColor(Color.parseColor("#2A86FF")); // 파랑
        } else if (net < 0) {
            text = "- " + KoreanMoney.format(Math.abs(net));
            holder.tvTotal.setTextColor(Color.parseColor("#C5463F")); // 빨강
        } else {
            text = "0";
            holder.tvTotal.setTextColor(Color.parseColor("#888888")); // 회색
        }
        holder.tvTotal.setText(text);

        // 상세로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LedgerDetailActivity.class);
            intent.putExtra("selected_date", yearMonth + "-" + String.format("%02d", item.getDay()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dayDataList.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTotal;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvTotal = itemView.findViewById(R.id.tv_total);
        }
    }
}
