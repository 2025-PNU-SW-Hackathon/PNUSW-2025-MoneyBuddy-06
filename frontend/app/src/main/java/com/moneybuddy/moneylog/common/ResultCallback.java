package com.moneybuddy.moneylog.common;

/**
 * 공통 콜백 인터페이스
 * 모든 Repository에서 비동기 결과 전달 시 사용
 */
public interface ResultCallback<T> {
    void onSuccess(T data);
    void onError(Throwable t);
}
