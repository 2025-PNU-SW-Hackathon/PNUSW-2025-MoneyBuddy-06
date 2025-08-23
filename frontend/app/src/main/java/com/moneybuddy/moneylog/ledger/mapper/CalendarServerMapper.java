package com.moneybuddy.moneylog.ledger.mapper;

import com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.ledger.dto.response.MonthSummaryDto;
import com.moneybuddy.moneylog.ledger.model.LedgerDayData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarServerMapper {

    // 서버 entries → LedgerDayData[42]
    public static List<LedgerDayData> mapMonth(MonthSummaryDto dto, int year, int month){
        List<LedgerDayData> out = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month-1, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // leading empty
        int leading = firstDayOfWeek - 1;
        for (int i=0;i<leading;i++) out.add(new LedgerDayData(null, 0, 0));

        // init day slots
        int[] income = new int[daysInMonth+1];
        int[] expense = new int[daysInMonth+1];

        if (dto != null && dto.entries != null) {
            for (LedgerEntryDto e : dto.entries) {
                // dateTime: "YYYY-MM-DDTHH:mm:ss"
                if (e.dateTime == null || e.dateTime.length()<10) continue;
                int day = Integer.parseInt(e.dateTime.substring(8,10));
                long amt = e.amount;
                if (amt >= 0) income[day] += (int)amt;
                else expense[day] += (int)Math.abs(amt);
            }
        }

        for (int d=1; d<=daysInMonth; d++){
            out.add(new LedgerDayData(d, income[d], expense[d]));
        }

        while (out.size()<42) out.add(new LedgerDayData(null, 0, 0));
        return out;
    }
}
