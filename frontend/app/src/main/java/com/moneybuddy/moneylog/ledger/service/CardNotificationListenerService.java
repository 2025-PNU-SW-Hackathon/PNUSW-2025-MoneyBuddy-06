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


public class CardNotificationListenerService extends NotificationListenerService {

    private String token() {
        String t = com.moneybuddy.moneylog.common.TokenManager
                .getInstance(getApplicationContext())
                .getToken();

        if (t == null || t.isEmpty()) {
            android.content.SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
            t = sp.getString("token", null);
            if (t == null) t = sp.getString("jwt", "");
        }
        return t == null ? "" : t;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        CharSequence csTitle = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence csText = sbn.getNotification().extras.getCharSequence("android.text");
        CharSequence csBig = sbn.getNotification().extras.getCharSequence("android.bigText");

        String title = csTitle == null ? "" : csTitle.toString();
        String text  = csBig != null ? csBig.toString() : (csText == null ? "" : csText.toString());

        // 카드/결제성 알림일 때만
        if (TextUtils.isEmpty(text)) return;
        if (!looksLikeCardNotice(title, text)) return;

        String receivedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
                .format(new Date(sbn.getPostTime()));

        AutoWriteRepository repo = new AutoWriteRepository(getApplicationContext(), token());
        repo.sendMessage(formatMessage(title, text), receivedAt)
                .enqueue(new Callback<AutoLedgerResponse>() {
                    @Override public void onResponse(Call<AutoLedgerResponse> call, Response<AutoLedgerResponse> res) {
                        if (!res.isSuccessful() || res.body() == null) {
                            android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                            h.post(() -> android.widget.Toast.makeText(getApplicationContext(),
                                    "자동 가계부 저장 실패 (" + res.code() + ")", android.widget.Toast.LENGTH_SHORT).show());
                            android.util.Log.e("AutoWrite", "HTTP " + res.code());
                            return;
                        }

                        AutoLedgerResponse body = res.body();

                        getSharedPreferences("auto_write", MODE_PRIVATE)
                                .edit().putLong("last_synced_at", System.currentTimeMillis()).apply();

                        android.content.Intent intent =
                                new android.content.Intent("com.moneybuddy.moneylog.ACTION_AUTO_LEDGER_CREATED");
                        intent.putExtra("payload_json", new com.google.gson.Gson().toJson(body));

                        try {
                            androidx.localbroadcastmanager.content.LocalBroadcastManager
                                    .getInstance(getApplicationContext()).sendBroadcast(intent);
                        } catch (Throwable ignore) {
                            sendBroadcast(intent);
                        }


                        android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                        h.post(() -> android.widget.Toast.makeText(getApplicationContext(),
                                "자동 가계부 1건이 추가되었어요", android.widget.Toast.LENGTH_SHORT).show());
                    }

                    @Override public void onFailure(Call<AutoLedgerResponse> call, Throwable t) {
                        android.util.Log.e("AutoWrite", "sendMessage failed", t);
                        android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                        h.post(() -> android.widget.Toast.makeText(getApplicationContext(),
                                "서버와 통신하지 못했어요", android.widget.Toast.LENGTH_SHORT).show());
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
