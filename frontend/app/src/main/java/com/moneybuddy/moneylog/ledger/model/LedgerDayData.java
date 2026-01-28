package com.moneybuddy.moneylog.ledger.model;

import androidx.annotation.Nullable;

public class LedgerDayData {
    @Nullable private String date;     // "YYYY-MM-DD"
    private int dayOfMonth;
    private boolean inThisMonth;

    // 합계(항상 양수로 저장: 수입+, 지출+)
    private long income;   // 하루 수입 합(양수)
    private long expense;  // 하루 지출 합(양수)

    // --- 생성자 오버로드(호출부 호환) ---
    public LedgerDayData(String date, int dayOfMonth, boolean inThisMonth) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.inThisMonth = inThisMonth;
    }
    public LedgerDayData() { }
    public LedgerDayData(int dayOfMonth) { this(null, dayOfMonth, true); }
    public LedgerDayData(String date) { this(date, extractDay(date), true); }

    private static int extractDay(@Nullable String date) {
        if (date == null || date.length() < 10) return 0;
        try { return Integer.parseInt(date.substring(8, 10)); } catch (Exception e) { return 0; }
    }

    // --- getters ---
    @Nullable public String getDate() { return date; }
    public int getDayOfMonth() { return dayOfMonth; }
    public boolean isInThisMonth() { return inThisMonth; }
    public long getIncome() { return income; }
    public long getExpense() { return expense; }

    // --- setters ---
    public void setDate(@Nullable String date) { this.date = date; }
    public void setDayOfMonth(int v) { this.dayOfMonth = v; }
    public void setInThisMonth(boolean v) { this.inThisMonth = v; }
    public void setIncome(long v) { this.income = Math.max(0, v); }
    public void setExpense(long v) { this.expense = Math.max(0, v); }
}
