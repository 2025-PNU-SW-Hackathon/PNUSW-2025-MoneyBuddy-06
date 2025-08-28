package com.moneybuddy.moneylog.ledger.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.moneybuddy.moneylog.ledger.ui.CategoryColors;

import java.util.LinkedHashMap;
import java.util.Map;

public class PieChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private LinkedHashMap<String, Long> data = new LinkedHashMap<>();
    private long total = 0L;

    // 남은 구간(회색) 식별/색상
    private static final String REM_LABEL = "__REMAINDER__";
    private static final int REM_COLOR = 0xFFCFD3D8; // 밝은 회색

    public PieChartView(Context ctx){ super(ctx); init(); }
    public PieChartView(Context ctx, @Nullable AttributeSet attrs){ super(ctx, attrs); init(); }
    public PieChartView(Context ctx, @Nullable AttributeSet attrs, int defStyle){ super(ctx, attrs, defStyle); init(); }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
    }

    /** key: 카테고리, value: 금액(양수) */
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

    /** 백엔드 ratioPercent가 0~100(%) 로 올 때 사용.
     *  합이 100 미만이면 남은 구간을 회색 슬라이스로 자동 추가 */
    public void setDataByRatio(Map<String, Double> ratiosPercent) {
        LinkedHashMap<String, Long> weights = new LinkedHashMap<>();
        double sum = 0.0;

        if (ratiosPercent != null) {
            // 합계(%) 계산
            for (Map.Entry<String, Double> e : ratiosPercent.entrySet()) {
                double p = e.getValue() == null ? 0.0 : Math.max(0.0, e.getValue());
                sum += p; // p는 0~100
            }

            // 가중치 스케일(비례만 유지되면 값은 무엇이든 OK)
            final long SCALE = 1_000L;

            // 각 항목 가중치(%) -> 정수 가중치
            for (Map.Entry<String, Double> e : ratiosPercent.entrySet()) {
                double p = e.getValue() == null ? 0.0 : Math.max(0.0, e.getValue());
                weights.put(e.getKey(), Math.round(p * SCALE));
            }

            // 남은 구간 추가: 100 - clamp(sum, 0, 100)
            double clampedSum = Math.min(100.0, Math.max(0.0, sum));
            double remain = 100.0 - clampedSum;
            if (remain > 1e-9) {
                weights.put(REM_LABEL, Math.round(remain * SCALE));
            }
        }

        setData(weights);
    }

    @Override protected void onMeasure(int wMs, int hMs) {
        int w = MeasureSpec.getSize(wMs);
        int h = MeasureSpec.getSize(hMs);
        int s = Math.min(w, h);
        setMeasuredDimension(s, s); // 정사각형 유지
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float pad = dp(12);
        arcRect.set(pad, pad, getWidth() - pad, getHeight() - pad);

        // 데이터가 없으면 연한 회색 원으로
        if (total <= 0) {
            paint.setColor(0xFFE5E7EB);
            canvas.drawArc(arcRect, 0, 360, true, paint);
            return;
        }

        float start = -90f;
        for (Map.Entry<String, Long> e : data.entrySet()) {
            String key = e.getKey();
            long v = e.getValue();
            if (v <= 0) continue;

            float sweep = (float) (360.0 * v / total);

            // 남은 구간은 고정 회색, 카테고리는 CategoryColors 기반
            if (REM_LABEL.equals(key)) {
                paint.setColor(REM_COLOR);
            } else {
                paint.setColor(CategoryColors.bg(getContext(), key));
            }

            canvas.drawArc(arcRect, start, sweep, true, paint);
            start += sweep;
        }
    }

    private float dp(float v){ return v * getResources().getDisplayMetrics().density; }
}
