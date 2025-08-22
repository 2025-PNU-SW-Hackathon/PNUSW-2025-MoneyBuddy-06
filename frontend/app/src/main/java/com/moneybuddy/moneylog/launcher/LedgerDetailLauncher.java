package com.moneybuddy.moneylog.launcher;

import android.content.Context;
import android.content.Intent;

import com.moneybuddy.moneylog.activity.LedgerDetailActivity;

public class LedgerDetailLauncher {

    public static void start(Context context, int year, int month, int day) {
        String dateStr = String.format("%04d-%02d-%02d", year, month, day);
        Intent intent = new Intent(context, LedgerDetailActivity.class);
        intent.putExtra("selected_date", dateStr);
        context.startActivity(intent);
    }

    public static void start(Context context, String dateIso) {
        Intent intent = new Intent(context, LedgerDetailActivity.class);
        intent.putExtra("selected_date", dateIso);
        context.startActivity(intent);
    }
}

