package com.moneybuddy.moneylog.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import com.moneybuddy.moneylog.ui.CategoryColors;

import java.util.LinkedHashMap;
import java.util.Map;

public class PieChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private LinkedHashMap<String, Long> data = new LinkedHashMap<>();
    private long total = 0L;

    public PieChartView(Context ctx){ super(ctx); init(); }
    public PieChartView(Context ctx, @Nullable AttributeSet attrs){ super(ctx, attrs); init(); }
    public PieChartView(Context ctx, @Nullable AttributeSet attrs, int defStyle){ super(ctx, attrs, defStyle); init(); }

    private void init() { paint.setStyle(Paint.Style.FILL); }

    /** key: 카테고리(한글/영문 가능), value: 금액(양수) */
    public void setData(Map<String, Long> map){
        data = new LinkedHashMap<>();
        total = 0L;
        if (map != null) {
            for (Map.Entry<String, Long> e : map.entrySet()) {
                long v = Math.max(0L, e.getValue() == null ? 0L : e.getValue());
                data.put(e.getKey(), v);
                total += v;
            }
        }
        invalidate();
    }

    @Override protected void onMeasure(int wMs, int hMs) {
        int w = MeasureSpec.getSize(wMs);
        int h = MeasureSpec.getSize(hMs);
        int s = Math.min(w, h);
        setMeasuredDimension(s, s); // 정사각형
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float pad = dp(12);
        arcRect.set(pad, pad, getWidth() - pad, getHeight() - pad);

        if (total <= 0) {
            paint.setColor(0xFFE5E7EB); // light gray (empty)
            canvas.drawArc(arcRect, 0, 360, true, paint);
            return;
        }

        float start = -90f;
        for (Map.Entry<String, Long> e : data.entrySet()) {
            String key = e.getKey();
            long v = e.getValue();
            if (v <= 0) continue;
            float sweep = (float) (360.0 * v / total);
            paint.setColor(CategoryColors.bg(getContext(), key));
            canvas.drawArc(arcRect, start, sweep, true, paint);
            start += sweep;
        }
    }

    private float dp(float v){ return v * getResources().getDisplayMetrics().density; }
}
