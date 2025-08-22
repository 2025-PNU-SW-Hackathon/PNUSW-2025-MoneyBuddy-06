package com.moneybuddy.moneylog.util;

import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Response;


public class ErrorParser {
    static class Err { String message; }
    public static String message(Response<?> response) {
        try {
            ResponseBody body = response.errorBody();
            if (body == null) return "요청이 실패했습니다. (" + response.code() + ")";
            Err e = new Gson().fromJson(body.string(), Err.class);
            return (e != null && e.message != null && !e.message.isEmpty())
                    ? e.message : "요청이 실패했습니다. (" + response.code() + ")";
        } catch (IOException ignored) {
            return "네트워크 오류가 발생했습니다.";
        }
    }
}