package com.moneybuddy.moneylog.ledger.mapper;

import com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.ledger.dto.response.MonthSummaryDto;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 서버 월 응답 → 캘린더 42칸 셀로 변환.
 * 각 셀은 "YYYY-MM-DD", dayOfMonth, inThisMonth, income(+), expense(+) 를 가진다.
 */
public final class CalendarServerMapper {

    private CalendarServerMapper() {}

    /**
     * @param dto   서버 월 응답(엔트리 목록 포함)
     * @param year  예) 2025
     * @param month 예) 8 (1~12)
     */
    public static List<LedgerDayData> mapMonth(MonthSummaryDto dto, int year, int month) {
        // 0) 엔트리 목록 확보 (null-safe)
        List<LedgerEntryDto> entries = getEntries(dto);

        // 1) 해당 월의 일수만큼 합계 버퍼 준비
        Calendar first = Calendar.getInstance(Locale.KOREAN);
        first.clear();
        first.set(Calendar.YEAR, year);
        first.set(Calendar.MONTH, month - 1); // 0-based
        first.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH);
        long[] income = new long[daysInMonth + 1];   // index = day(1..)
        long[] expense = new long[daysInMonth + 1];

        // 2) 엔트리 → 일자별 수입/지출 합계(양수로 누적)
        if (entries != null) {
            for (LedgerEntryDto e : entries) {
                if (e == null) continue;
                String dt = e.getDateTime();                 // ✅ 게터 사용
                if (dt == null || dt.length() < 10) continue;

                int day;
                try {
                    day = Integer.parseInt(dt.substring(8, 10));
                } catch (Exception ignore) {
                    continue;
                }
                if (day < 1 || day > daysInMonth) continue;

                long amt = e.getAmount();                    // ✅ 게터 사용 (서버가 부호 적용)
                if (amt >= 0) income[day] += amt; else expense[day] += -amt;
            }
        }

        // 3) 42칸 그리드 생성 (일요일 시작)
        int dow = first.get(Calendar.DAY_OF_WEEK);           // SUNDAY=1..SATURDAY=7
        int leading = dow - Calendar.SUNDAY;                 // 0..6
        Calendar cur = (Calendar) first.clone();
        cur.add(Calendar.DAY_OF_MONTH, -leading);

        List<LedgerDayData> out = new ArrayList<>(42);
        for (int i = 0; i < 42; i++) {
            boolean inThisMonth = (cur.get(Calendar.MONTH) == (month - 1));
            int y = cur.get(Calendar.YEAR);
            int m = cur.get(Calendar.MONTH) + 1;
            int d = cur.get(Calendar.DAY_OF_MONTH);
            String ymd = String.format(Locale.KOREAN, "%04d-%02d-%02d", y, m, d);

            LedgerDayData cell = new LedgerDayData(ymd, d, inThisMonth);
            if (inThisMonth && d >= 1 && d <= daysInMonth) {
                cell.setIncome(income[d]);
                cell.setExpense(expense[d]);
            } else {
                cell.setIncome(0);
                cell.setExpense(0);
            }
            out.add(cell);

            cur.add(Calendar.DAY_OF_MONTH, 1);
        }
        return out;
    }

    // MonthSummaryDto가 getEntries() 게터를 제공한다는 가정.
    // 만약 아직 public 필드(dto.entries)를 쓰고 있다면, DTO에 게터를 추가하거나
    // 아래 메서드 내용을 dto.entries로 바꿔주세요.
    private static List<LedgerEntryDto> getEntries(MonthSummaryDto dto) {
        if (dto == null) return Collections.emptyList();
        try {
            // 게터가 있는 경우
            List<LedgerEntryDto> list = dto.getEntries();
            return list == null ? Collections.emptyList() : list;
        } catch (NoSuchMethodError | Exception ignore) {
            // 게터가 없다면 public 필드로 시도(필요 시 MonthSummaryDto에 게터 추가 권장)
            try {
                java.lang.reflect.Field f = dto.getClass().getDeclaredField("entries");
                f.setAccessible(true);
                Object v = f.get(dto);
                if (v instanceof List) return (List<LedgerEntryDto>) v;
            } catch (Exception ignored) {}
        }
        return Collections.emptyList();
    }
}
