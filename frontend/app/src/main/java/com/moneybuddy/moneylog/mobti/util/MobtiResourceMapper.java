package com.moneybuddy.moneylog.mobti.util;

import com.moneybuddy.moneylog.R;

public class MobtiResourceMapper {
    public static int mascotFor(String code) {
        if (code == null) return R.drawable.ic_mascot_placeholder;
        switch (code) {
            case "EMCP": return R.drawable.ic_mascot_placeholder;
            default: return R.drawable.ic_mascot_placeholder;
        }
    }
}
