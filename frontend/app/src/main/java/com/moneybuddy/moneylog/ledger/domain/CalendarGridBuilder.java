package com.moneybuddy.moneylog.ledger.domain;

import com.moneybuddy.moneylog.ledger.model.LedgerDayData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** API 24 안전: 6주(42칸) 캘린더 셀 생성 */
public final class CalendarGridBuilder {
    private CalendarGridBuilder() {}

    /**
     * @param year  예) 2025
     * @param month 예) 8 (1~12)
     */
    public static List<LedgerDayData> buildMonthCells(int year, int month) {
        final List<LedgerDayData> out = new ArrayList<>(42);

        // first day of month
        Calendar first = Calendar.getInstance(Locale.KOREAN);
        first.clear();
        first.set(Calendar.YEAR, year);
        first.set(Calendar.MONTH, month - 1); // 0-based
        first.set(Calendar.DAY_OF_MONTH, 1);

        // Sunday-start grid: SUNDAY=1 ... SATURDAY=7  → back = dow-1
        int dow = first.get(Calendar.DAY_OF_WEEK);
        int back = dow - Calendar.SUNDAY; // 0..6
        Calendar start = (Calendar) first.clone();
        start.add(Calendar.DAY_OF_MONTH, -back);

        for (int i = 0; i < 42; i++) {
            Calendar cur = (Calendar) start.clone();
            cur.add(Calendar.DAY_OF_MONTH, i);

            boolean inThisMonth = (cur.get(Calendar.MONTH) == (month - 1));
            int y  = cur.get(Calendar.YEAR);
            int m  = cur.get(Calendar.MONTH) + 1;
            int d  = cur.get(Calendar.DAY_OF_MONTH);
            String ymd = String.format(Locale.KOREAN, "%04d-%02d-%02d", y, m, d);

            out.add(new LedgerDayData(ymd, d, inThisMonth));
        }
        return out;
    }
}
