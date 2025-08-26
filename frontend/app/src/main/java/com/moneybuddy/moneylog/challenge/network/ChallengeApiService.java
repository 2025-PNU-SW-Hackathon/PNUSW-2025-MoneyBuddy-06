package com.moneybuddy.moneylog.challenge.network;

import com.moneybuddy.moneylog.challenge.dto.ChallengeCardResponse;
import com.moneybuddy.moneylog.challenge.dto.ChallengeCreateRequest;
import com.moneybuddy.moneylog.challenge.dto.ChallengeFilterRequest;
import com.moneybuddy.moneylog.challenge.dto.ChallengeStatusRequest;
import com.moneybuddy.moneylog.challenge.dto.ChallengeStatusResponse;
import com.moneybuddy.moneylog.challenge.dto.RecommendedChallengeResponse;
import com.moneybuddy.moneylog.challenge.dto.UserChallengeRequest;
import com.moneybuddy.moneylog.challenge.dto.UserChallengeResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ChallengeApiService {
    @GET("/view/ongoing")
    Call<List<ChallengeCardResponse>> getOngoingChallenges();

    @GET("/view/completed")
    Call<List<ChallengeCardResponse>> getCompletedChallenges();

    @GET("/shared")
    Call<List<ChallengeCardResponse>> getSharedChallenges();

    @GET("/recommended/mobti")
    Call<List<RecommendedChallengeResponse>> getRecommendedChallenges();


    @POST("/ongoing/filter")
    Call<List<ChallengeCardResponse>> filterOngoingChallenges(@Body ChallengeFilterRequest request);

    @POST("/completed/filter")
    Call<List<ChallengeCardResponse>> filterCompletedChallenges(@Body ChallengeFilterRequest request);

    @POST("/shared/filter")
    Call<List<ChallengeCardResponse>> filterSharedChallenges(@Body ChallengeFilterRequest request);

    @POST("/recommended/mobti/filter")
    Call<List<ChallengeCardResponse>> filterRecommendedChallenges(@Body ChallengeFilterRequest request);

    @POST("/create")
    Call<ResponseBody> createChallenge(@Body ChallengeCreateRequest request);

    @POST("/join")
    Call<UserChallengeResponse> joinChallenge(@Body UserChallengeRequest request);

    @POST("/status")
    Call<ChallengeStatusResponse> updateChallengeStatus(
            @Body ChallengeStatusRequest request
    );
}