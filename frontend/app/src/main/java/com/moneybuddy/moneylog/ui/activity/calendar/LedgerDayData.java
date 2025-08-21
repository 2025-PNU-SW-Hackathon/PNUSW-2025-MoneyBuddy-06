package com.moneybuddy.moneylog.ui.activity.calendar;


public class LedgerDayData {
    private Integer day; // 날짜 (1~31), or null → 빈칸
    private int income;
    private int expense;

    public LedgerDayData(Integer day, int income, int expense) {
        this.day = day;
        this.income = income;
        this.expense = expense;
    }

    public Integer getDay() {
        return day;
    }

    public int getIncome() {
        return income;
    }

    public int getExpense() {
        return expense;
    }

    public boolean isEmpty() {
        return day == null;
    }
}