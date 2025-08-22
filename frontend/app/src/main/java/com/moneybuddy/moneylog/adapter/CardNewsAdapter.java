package com.moneybuddy.moneylog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.dto.response.KnowledgeResponse;
import java.util.ArrayList;
import java.util.List;

public class CardNewsAdapter extends RecyclerView.Adapter<CardNewsAdapter.CardNewsViewHolder> {
    private List<KnowledgeResponse> cardNewsList = new ArrayList<>();

    @NonNull
    @Override
    public CardNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_news, parent, false);
        return new CardNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardNewsViewHolder holder, int position) {
        KnowledgeResponse cardNews = cardNewsList.get(position);
        holder.bind(cardNews);
    }

    @Override
    public int getItemCount() {
        return cardNewsList.size();
    }

    public void setCardNewsList(List<KnowledgeResponse> newList) {
        this.cardNewsList.clear();
        this.cardNewsList.addAll(newList);
        notifyDataSetChanged();
    }

    static class CardNewsViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView contentTextView;

        public CardNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_card_title);
            contentTextView = itemView.findViewById(R.id.text_card_content);
        }

        public void bind(KnowledgeResponse cardNews) {
            titleTextView.setText(cardNews.getTitle());
            contentTextView.setText(cardNews.getContent());
        }
    }
}