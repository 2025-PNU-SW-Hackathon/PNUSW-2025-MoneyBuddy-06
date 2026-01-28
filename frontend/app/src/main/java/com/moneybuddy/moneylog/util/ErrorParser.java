package com.moneybuddy.moneylog.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Response;


public class ErrorParser {
    static class Err { String message; }

    public static String message(Response<?> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return "알 수 없는 오류가 발생했습니다. (코드: " + response.code() + ")";
        }

        try {
            String errorBodyString = errorBody.string();

            try {
                Err e = new Gson().fromJson(errorBodyString, Err.class);
                if (e != null && e.message != null && !e.message.isEmpty()) {
                    return e.message;
                }
            } catch (JsonSyntaxException jsonException) {
                if (!errorBodyString.isEmpty()) {
                    return errorBodyString;
                }
            }
            return "오류가 발생했습니다. (코드: " + response.code() + ")";

        } catch (IOException ioException) {
            return "오류 응답을 읽는 데 실패했습니다.";
        }
    }
}