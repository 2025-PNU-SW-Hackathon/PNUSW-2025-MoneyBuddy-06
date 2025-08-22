package com.moneybuddy.moneylog.util;

import android.net.Uri;

import java.util.Collections;
import java.util.Map;

public final class DeepLinkResolver {

    private DeepLinkResolver() {}

    /** 서버가 deeplink를 안 준 경우 action+params로 경로 조립 */
    public static String build(String action, Map<String, Object> params) {
        if (action == null) return "/";
        if (params == null) params = Collections.emptyMap();

        switch (action) {
            case "OPEN_CHALLENGE_DETAIL":
                return "/challenges/" + get(params, "challengeId");
            case "OPEN_LEDGER_NEW":
                // /ledger/new?from=ocr&ocrId=7731
                return "/ledger/new?from=" + get(params, "from", "ocr")
                        + "&ocrId=" + get(params, "ocrId");
            case "OPEN_SPENDING_STATS":
                return "/stats/spending";
            case "OPEN_QUIZ_TODAY":
                return "/quiz/today";
            case "OPEN_FINANCE_ARTICLE":
                return "/finance/" + get(params, "articleId");
            case "OPEN_PROFILE_LEVEL":
                return "/profile/level";
            default:
                return "/";
        }
    }

    /** 앱 스킴으로 변환 (웹 절대경로면 그대로) */
    public static Uri toUri(String path) {
        if (path == null || path.isEmpty()) path = "/";
        return path.startsWith("http")
                ? Uri.parse(path)
                : Uri.parse("moneylog://" + (path.startsWith("/") ? path.substring(1) : path));
    }

    private static String get(Map<String, Object> p, String k) {
        Object v = p.get(k);
        return v == null ? "" : String.valueOf(v);
    }
    private static String get(Map<String, Object> p, String k, String def) {
        Object v = p.get(k);
        return v == null ? def : String.valueOf(v);
    }
}
