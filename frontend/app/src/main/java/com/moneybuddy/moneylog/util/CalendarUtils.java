
package com.moneybuddy.moneylog.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CalendarUtils {

    // 날짜 → "7월 5일 토요일" 형식으로 반환
    public static String formatDate(Calendar cal) {
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String weekday = getDayOfWeekKorean(cal.get(Calendar.DAY_OF_WEEK));
        return month + "월 " + day + "일 " + weekday;
    }

    // 요일 숫자 → 한글 (일~토)
    public static String getDayOfWeekKorean(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "일요일";
            case Calendar.MONDAY: return "월요일";
            case Calendar.TUESDAY: return "화요일";
            case Calendar.WEDNESDAY: return "수요일";
            case Calendar.THURSDAY: return "목요일";
            case Calendar.FRIDAY: return "금요일";
            case Calendar.SATURDAY: return "토요일";
            default: return "";
        }
    }

    // 주 시작일 (해당 날짜 포함된 주의 일요일)
    public static Calendar getWeekStart(Calendar base) {
        Calendar copy = (Calendar) base.clone();
        copy.setFirstDayOfWeek(Calendar.SUNDAY);
        copy.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return copy;
    }

    // Calendar 날짜 +일 수
    public static Calendar addDays(Calendar base, int offset) {
        Calendar copy = (Calendar) base.clone();
        copy.add(Calendar.DAY_OF_MONTH, offset);
        return copy;
    }

    // 날짜 숫자만 추출
    public static int getDay(Calendar cal) {
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    // 오늘 날짜 반환
    public static Calendar today() {
        return new GregorianCalendar();
    }

    // yyyy-MM-dd 포맷 문자열 반환
    public static String toIso(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        return sdf.format(cal.getTime());
    }
}

