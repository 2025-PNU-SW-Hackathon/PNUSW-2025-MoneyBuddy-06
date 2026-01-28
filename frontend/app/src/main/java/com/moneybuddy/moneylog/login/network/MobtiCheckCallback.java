package com.moneybuddy.moneylog.login.network;

public interface MobtiCheckCallback {
    void onMobtiExists(); // MoBTI 결과가 있을 때
    void onMobtiNotExists(); // MoBTI 결과가 없을 때 (신규 사용자)
    void onError(String message); // 통신 오류 발생 시
}