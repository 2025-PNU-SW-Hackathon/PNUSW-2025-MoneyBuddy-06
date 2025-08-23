package com.moneybuddy.moneylog.ledger.service;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.moneybuddy.moneylog.ledger.dto.response.AutoLedgerResponse;
import com.moneybuddy.moneylog.ledger.repository.AutoWriteRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 카드/결제 알림 텍스트를 수집해서 서버로 전송.
 * - 앱 설정에서 알림 접근 권한 필요.
 * - AndroidManifest.xml 에 서비스 등록 필요.
 */
public class CardNotificationListenerService extends NotificationListenerService {

    private String token() {
        // TODO: 앱의 토큰 보관소에서 JWT를 가져오세요.
        return "YOUR_JWT_TOKEN";
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        CharSequence csTitle = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence csText = sbn.getNotification().extras.getCharSequence("android.text");
        CharSequence csBig = sbn.getNotification().extras.getCharSequence("android.bigText");

        String title = csTitle == null ? "" : csTitle.toString();
        String text  = csBig != null ? csBig.toString() : (csText == null ? "" : csText.toString());

        // 카드/결제성 알림일 때만 필터링(필요시 패키지명/키워드 조건 강화)
        if (TextUtils.isEmpty(text)) return;
        if (!looksLikeCardNotice(title, text)) return;

        String receivedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
                .format(new Date(sbn.getPostTime()));

        AutoWriteRepository repo = new AutoWriteRepository(getApplicationContext(), token());
        repo.sendMessage(formatMessage(title, text), receivedAt)
                .enqueue(new Callback<AutoLedgerResponse>() {
                    @Override public void onResponse(Call<AutoLedgerResponse> call, Response<AutoLedgerResponse> res) {
                        if (res.isSuccessful() && res.body() != null) {
                            // TODO: 로컬 캐시/화면 반영 (LiveData/Room/Bus 등으로 브로드캐스트)
                        }
                    }
                    @Override public void onFailure(Call<AutoLedgerResponse> call, Throwable t) {
                        // TODO: 실패 로깅
                    }
                });
    }

    private boolean looksLikeCardNotice(String title, String text) {
        String all = (title + " " + text).toLowerCase(Locale.ROOT);
        return all.contains("승인") || all.contains("결제") || all.contains("취소") ||
                all.contains("카드") || all.contains("payment") || all.contains("approved");
    }

    private String formatMessage(String title, String text) {
        // 서버가 자유 텍스트를 파싱하므로 최대한 원문을 보존
        return "[" + title + "]\n" + text;
    }
}
