package com.moneybuddy.moneylog.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class DeepLinkResolver {
    private DeepLinkResolver() {}

    /** 서버가 준 deeplink를 그대로 연다. (예: "/ledger/new?from=ocr&ocrId=7731") */
    public static void resolve(Context ctx, String deeplink) {
        if (deeplink == null || deeplink.trim().isEmpty()) return;
        String url = deeplink.startsWith("/") ? "moneylog://internal" + deeplink
                : "moneylog://internal/" + deeplink;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.setPackage(ctx.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    /** Intent만 필요할 때 */
    public static Intent toIntent(Context ctx, String deeplink) {
        if (deeplink == null || deeplink.trim().isEmpty()) return null;
        String url = deeplink.startsWith("/") ? "moneylog://internal" + deeplink
                : "moneylog://internal/" + deeplink;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.setPackage(ctx.getPackageName());
        return i;
    }
}
