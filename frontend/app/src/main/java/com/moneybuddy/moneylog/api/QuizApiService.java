package com.moneybuddy.moneylog.api;

import com.moneybuddy.moneylog.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.dto.response.QuizResponse;
import com.moneybuddy.moneylog.dto.response.QuizResultResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface QuizApiService {

    // 오늘의 퀴즈 조회
    @GET("api/v1/quiz/today")
    Call<QuizResponse> getTodayQuiz(@Header("Authorization") String authToken);

    // 퀴즈 정답 제출
    @POST("api/v1/quiz/answer")
    Call<QuizResultResponse> submitAnswer(
            @Header("Authorization") String authToken,
            @Body QuizAnswerRequest request
    );
}