package com.moneybuddy.moneylog.challenge.network;

import com.moneybuddy.moneylog.challenge.dto.ChallengeCardResponse;
import com.moneybuddy.moneylog.challenge.dto.ChallengeCreateRequest;
import com.moneybuddy.moneylog.challenge.dto.ChallengeDetailResponse;
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
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChallengeApiService {
    @GET("challenge/view/ongoing")
    Call<List<ChallengeCardResponse>> getOngoingChallenges();

    @GET("challenge/view/completed")
    Call<List<ChallengeCardResponse>> getCompletedChallenges();

    @GET("challenge/shared")
    Call<List<ChallengeCardResponse>> getSharedChallenges();

    @GET("challenge/recommended/mobti")
    Call<List<RecommendedChallengeResponse>> getRecommendedChallenges();


    @POST("challenge/ongoing/filter")
    Call<List<ChallengeCardResponse>> filterOngoingChallenges(@Body ChallengeFilterRequest request);

    @POST("challenge/completed/filter")
    Call<List<ChallengeCardResponse>> filterCompletedChallenges(@Body ChallengeFilterRequest request);

    @POST("challenge/shared/filter")
    Call<List<ChallengeCardResponse>> filterSharedChallenges(@Body ChallengeFilterRequest request);

    @POST("challenge/recommended/mobti/filter")
    Call<List<ChallengeCardResponse>> filterRecommendedChallenges(@Body ChallengeFilterRequest request);

    @POST("challenge/create")
    Call<ResponseBody> createChallenge(@Body ChallengeCreateRequest request);

    @POST("challenge/join")
    Call<UserChallengeResponse> joinChallenge(@Body UserChallengeRequest request);

    @POST("challenge/status")
    Call<ChallengeStatusResponse> updateChallengeStatus(
            @Body ChallengeStatusRequest request
    );

    @GET("/challenges/{challengeId}")
    Call<ChallengeDetailResponse> getChallengeDetail(@Path("challengeId") Long challengeId);
}