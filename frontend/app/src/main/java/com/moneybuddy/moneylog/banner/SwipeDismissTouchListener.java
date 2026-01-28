package com.moneybuddy.moneylog.banner;

import android.view.MotionEvent;
import android.view.View;

final class SwipeDismissTouchListener implements View.OnTouchListener {
    private final View view;
    private final Runnable onDismiss;
    private float downY;
    private boolean dragging;

    SwipeDismissTouchListener(View view, Runnable onDismiss) {
        this.view = view;
        this.onDismiss = onDismiss;
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downY = e.getRawY();
                dragging = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dy = e.getRawY() - downY;
                if (dy < 0) { // 위로만 스와이프
                    dragging = true;
                    view.setTranslationY(dy);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging && Math.abs(view.getTranslationY()) > view.getHeight() / 3f) {
                    onDismiss.run();
                } else {
                    view.animate().translationY(0f).setDuration(120).start();
                }
                return true;
        }
        return false;
    }
}
