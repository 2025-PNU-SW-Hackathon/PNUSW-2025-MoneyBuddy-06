package com.moneybuddy.moneylog.ledger.model;

import androidx.annotation.Nullable;

public class DayCell {
    // final 제거 → 호출부에서 세터로 채울 수 있게
    @Nullable private String date;     // "YYYY-MM-DD" (nullable 허용)
    private int dayOfMonth;
    private boolean inThisMonth;
    private long income;               // 합계(양수)
    private long expense;              // 합계(양수)

    // ===== 기본(기존) 생성자 =====
    public DayCell(String date, int dayOfMonth, boolean inThisMonth) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.inThisMonth = inThisMonth;
    }

    // ===== 오버로드 생성자들 (호출부 호환) =====
    public DayCell() { this(null, 0, true); }                    // 0개 인자
    public DayCell(int dayOfMonth) { this(null, dayOfMonth, true); }                 // 1개: dayOfMonth
    public DayCell(String date) { this(date, extractDayOfMonth(date), true); }       // 1개: date
    public DayCell(String date, int dayOfMonth) { this(date, dayOfMonth, true); }    // 2개
    public DayCell(int dayOfMonth, boolean inThisMonth) { this(null, dayOfMonth, inThisMonth); } // 2개

    private static int extractDayOfMonth(@Nullable String date) {
        if (date == null || date.length() < 10) return 0;
        try { return Integer.parseInt(date.substring(8, 10)); } catch (Exception e) { return 0; }
    }

    // ===== getters =====
    @Nullable public String getDate() { return date; }
    public int getDayOfMonth() { return dayOfMonth; }
    public boolean isInThisMonth() { return inThisMonth; }
    public long getIncome() { return income; }
    public long getExpense() { return expense; }

    // ===== setters =====
    public void setDate(@Nullable String date) { this.date = date; }
    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public void setInThisMonth(boolean inThisMonth) { this.inThisMonth = inThisMonth; }
    public void setIncome(long v) { this.income = Math.max(0, v); }
    public void setExpense(long v) { this.expense = Math.max(0, v); }
}
