package com.moneybuddy.moneylog.util;

import java.text.NumberFormat;
import java.util.Locale;

public class KoreanMoney {
    public static String format(long amount) {
        return NumberFormat.getInstance(Locale.KOREA).format(amount);
    }
}
