package com.moneybuddy.moneylog.ledger.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class GoalBarBinder {

    private GoalBarBinder() {}

    public static void bindSpentAndGoal(TextView tvSpent, TextView tvGoal, CategoryRatioResponse dto) {
        if (dto == null) return;
        long spent = Math.max(0L, dto.spent);
        long goal  = (dto.goalAmount == null) ? 0L : Math.max(0L, dto.goalAmount);
        if (tvSpent != null) tvSpent.setText(KoreanMoney.format(spent));
        if (tvGoal  != null) tvGoal.setText(KoreanMoney.format(goal));
    }

    /** 가계부 화면과 동일: baseline = GOAL or SPENT, 카테고리 색은 CategoryColors 사용 */
    public static void renderGoalBar(Context ctx, LinearLayout track, CategoryRatioResponse dto) {
        if (ctx == null || track == null || dto == null) return;

        final long spent = Math.max(0L, dto.spent);
        final long goal  = (dto.goalAmount == null) ? 0L : Math.max(0L, dto.goalAmount);

        track.setVisibility(View.VISIBLE);
        track.removeAllViews();

        LinearLayout spentBar = new LinearLayout(ctx);
        spentBar.setOrientation(LinearLayout.HORIZONTAL);
        spentBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(spent, 0)));

        View remain = new View(ctx);
        remain.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(goal - spent, 0)));

        track.addView(spentBar);
        track.addView(remain);

        if (dto.items == null || dto.items.isEmpty() || spent <= 0) return;

        List<CategoryRatioResponse.Item> list = new ArrayList<>(dto.items);
        list.sort((a, b) -> Long.compare(b.expense, a.expense));

        long totalExpense = 0L;
        for (CategoryRatioResponse.Item it : list) totalExpense += Math.max(0L, it.expense);
        if (totalExpense <= 0L) return;

        for (int i = 0; i < list.size(); i++) {
            CategoryRatioResponse.Item it = list.get(i);
            long v = Math.max(0L, it.expense);
            if (v == 0) continue;

            float w = (float) v / (float) totalExpense * (float) spent;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, w);

            View seg = new View(ctx);
            seg.setLayoutParams(lp);

            boolean first = (spentBar.getChildCount() == 0);
            boolean last  = (i == list.size() - 1);

            int color = CategoryColors.bg(ctx, it.category);
            seg.setBackground(makeRounded(color, ctx, first, last));
            spentBar.addView(seg);
        }
    }

    public static void renderPreview(Context ctx, LinearLayout track, long goal, long spent, int[] seg) {
        if (ctx == null || track == null) return;
        track.setVisibility(View.VISIBLE);
        track.removeAllViews();

        LinearLayout spentBar = new LinearLayout(ctx);
        spentBar.setOrientation(LinearLayout.HORIZONTAL);
        spentBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(spent, 0)));

        View remain = new View(ctx);
        remain.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(goal - spent, 0)));

        track.addView(spentBar);
        track.addView(remain);

        int[] colors = new int[]{ 0xFFFCA5A5, 0xFFFDBA74, 0xFFFDE047,
                0xFF86EFAC, 0xFF93C5FD, 0xFFA5B4FC, 0xFFD8B4FE };

        long total = 0; if (seg != null) for (int v : seg) total += v;
        if (total <= 0 || spent <= 0 || seg == null) return;

        for (int i = 0; i < seg.length; i++) {
            long amt = seg[i];
            if (amt <= 0) continue;

            float w = (float) amt / (float) total * (float) spent;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, w);
            View part = new View(ctx);
            part.setLayoutParams(lp);

            boolean first = (spentBar.getChildCount() == 0);
            boolean last  = (i == seg.length - 1);
            part.setBackground(makeRounded(colors[i % colors.length], ctx, first, last));

            spentBar.addView(part);
        }
    }

    private static GradientDrawable makeRounded(int color, Context ctx, boolean first, boolean last) {
        float r = ctx.getResources().getDisplayMetrics().density * 8f;
        float tl = first ? r : 0, bl = first ? r : 0;
        float tr = last  ? r : 0, br = last  ? r : 0;

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadii(new float[]{tl, tl, tr, tr, br, br, bl, bl});
        return gd;
    }
}
