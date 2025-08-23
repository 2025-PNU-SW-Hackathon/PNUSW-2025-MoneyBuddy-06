package com.moneybuddy.moneylog.ledger.adapter;

// ✅ 거래 내역 상세 리스트용 어댑터

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.ledger.model.Transaction;
import com.moneybuddy.moneylog.ledger.activity.LedgerWriteActivity;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.Collections;
import java.util.List;

public class LedgerDetailAdapter extends RecyclerView.Adapter<LedgerDetailAdapter.ViewHolder> {

    private final List<Transaction> items;

    public LedgerDetailAdapter(List<Transaction> items) {
        this.items = (items != null) ? items : Collections.emptyList();
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction item);
    }
    private OnItemClickListener clickListener;
    public void setOnItemClickListener(OnItemClickListener l) { this.clickListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ledger_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction item = items.get(position);

        // ----- 바인딩 -----
        holder.tvTime.setText(item.getTime());
        holder.tvTitle.setText(item.getTitle());
        holder.tvCategory.setText(item.getCategory());

        // 자산 표시(없으면 GONE)
        String asset = item.getAsset();
        if (asset == null || asset.trim().isEmpty()) {
            holder.tvAsset.setVisibility(View.GONE);
        } else {
            holder.tvAsset.setVisibility(View.VISIBLE);
            holder.tvAsset.setText(asset);
        }

        // 금액 + 색상
        int amount = item.getAmount();
        String formatted = KoreanMoney.format(Math.abs(amount));
        holder.tvAmount.setText(amount >= 0 ? ("+" + formatted) : ("-" + formatted));

        Transaction.Type type = item.getType();
        if (type == Transaction.Type.INCOME || amount >= 0) {
            holder.tvAmount.setTextColor(Color.parseColor("#2A86FF")); // 수입(파랑)
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#C5463F")); // 지출(빨강)
        }

        // 클릭: 편집 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Transaction clicked = items.get(pos);

            Intent intent = new Intent(holder.itemView.getContext(), LedgerWriteActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("time", clicked.getTime());
            intent.putExtra("title", clicked.getTitle());
            intent.putExtra("category", clicked.getCategory());
            intent.putExtra("asset", clicked.getAsset());
            intent.putExtra("amount", clicked.getAmount());
            intent.putExtra("type",
                    clicked.getType() != null ? clicked.getType().name()
                            : (clicked.getAmount() >= 0 ? "INCOME" : "EXPENSE"));
            holder.itemView.getContext().startActivity(intent);

            if (clickListener != null) clickListener.onItemClick(clicked);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle, tvCategory, tvAmount, tvAsset;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime     = itemView.findViewById(R.id.tv_time);
            tvTitle    = itemView.findViewById(R.id.tv_title);
            // 레이아웃 id가 tv_category라면 아래 한 줄을 그 id로 바꿔주세요.
            tvCategory = itemView.findViewById(R.id.tv_catecory);
            tvAsset    = itemView.findViewById(R.id.tv_asset);
            tvAmount   = itemView.findViewById(R.id.tv_amount);
        }
    }
}
