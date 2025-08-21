package com.moneybuddy.moneylog.ui.activity.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 달력 셀 생성 유틸.
 * - 더미/랜덤 값 생성 금지!
 * - 실제 금액은 generateCalendar(year, month, dailyMap) 오버로드를 사용해 주입합니다.
 */
public class CalendarUtils {

    /** 날짜 키: yyyy-MM-dd */
    public static String dateKey(int year, int month, int day) {
        return String.format(Locale.KOREAN, "%04d-%02d-%02d", year, month, day);
    }

    /**
     * (구버전 호환) 금액 0으로만 채운 스켈레톤 생성.
     * 기존 호출부를 당장 유지해야 할 때 사용. 실제 금액 반영은 아답터/액티비티에서 따로 주입하세요.
     */
    public static List<LedgerDayData> generateCalendar(int year, int month) {
        return generateCalendar(year, month, null);
    }

    /**
     * 실제 집계 맵을 받아 달력 셀을 생성.
     * @param dailyMap key: yyyy-MM-dd, value: DaySum(수입/지출 합)
     */
    public static List<LedgerDayData> generateCalendar(
            int year,
            int month,
            Map<String, DaySum> dailyMap
    ) {
        List<LedgerDayData> calendarCells = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1); // Calendar는 month 0-based

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1(일)~7(토)
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 앞쪽 빈칸
        int leadingEmpty = firstDayOfWeek - 1; // 일요일 시작 가정
        for (int i = 0; i < leadingEmpty; i++) {
            calendarCells.add(new LedgerDayData(null, 0, 0));
        }

        // 1 ~ 말일까지 실제 합계 주입 (가짜값 금지)
        for (int day = 1; day <= daysInMonth; day++) {
            int income = 0;
            int expense = 0;

            if (dailyMap != null) {
                DaySum sum = dailyMap.get(dateKey(year, month, day));
                if (sum != null) {
                    // LedgerDayData가 int 금액을 받는다면 범위를 안전하게 캐스팅
                    income = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, sum.income));
                    expense = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, sum.expense));
                }
            }
            calendarCells.add(new LedgerDayData(day, income, expense));
        }

        // 뒤쪽 빈칸(총 42칸 유지)
        while (calendarCells.size() < 42) {
            calendarCells.add(new LedgerDayData(null, 0, 0));
        }

        return calendarCells;
    }

    /** 날짜별 합계를 담는 간단 POJO */
    public static class DaySum {
        public long income;
        public long expense;
        public DaySum() {}
        public DaySum(long income, long expense) { this.income = income; this.expense = expense; }
    }
}
