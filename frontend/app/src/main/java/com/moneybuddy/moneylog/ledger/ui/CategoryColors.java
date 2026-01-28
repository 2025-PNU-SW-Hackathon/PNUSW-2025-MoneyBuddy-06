package com.moneybuddy.moneylog.ledger.ui;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.moneybuddy.moneylog.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** 카테고리 키(영문) 또는 한글 라벨 → colors.xml 색상 리소스 매핑 */
public final class CategoryColors {
    private CategoryColors() {}

    private static Map<String, Integer> map;

    private static void ensure() {
        if (map != null) return;
        map = new HashMap<>();
        // 영어 키
        map.put("food", R.color.food);
        map.put("cafe_bakery", R.color.cafe_bakery);
        map.put("transport", R.color.transport);
        map.put("culture_leisure", R.color.culture_leisure);
        map.put("healthcare", R.color.healthcare);
        map.put("clothing_beauty", R.color.clothing_beauty);
        map.put("other", R.color.other);
        // 한글 라벨도 바로 대응
        map.put("식비", R.color.food);
        map.put("카페베이커리", R.color.cafe_bakery);
        map.put("교통", R.color.transport);
        map.put("문화여가", R.color.culture_leisure);
        map.put("의료건강", R.color.healthcare);
        map.put("의류미용", R.color.clothing_beauty);
        map.put("기타", R.color.other);
    }

    /** 배경색 */
    @ColorInt
    public static int bg(Context c, String key) {
        ensure();
        if (key == null) return ContextCompat.getColor(c, R.color.other);
        Integer res = map.get(key);
        if (res == null) res = map.get(key.toLowerCase(Locale.ROOT));
        if (res == null) res = R.color.other;
        return ContextCompat.getColor(c, res);
    }

    /** 텍스트색(노랑엔 검정, 나머진 흰색) */
    @ColorInt
    public static int onBg(Context c, String key) {
        if ("transport".equalsIgnoreCase(key) || "교통".equals(key)) {
            return ContextCompat.getColor(c, R.color.black);
        }
        return ContextCompat.getColor(c, R.color.white);
    }
}
