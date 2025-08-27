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
    @GET("challenges/view/ongoing")
    Call<List<ChallengeCardResponse>> getOngoingChallenges();

    @GET("challenges/view/completed")
    Call<List<ChallengeCardResponse>> getCompletedChallenges();

    @GET("challenges/shared")
    Call<List<ChallengeCardResponse>> getSharedChallenges();

    @GET("challenges/recommended/mobti")
    Call<List<RecommendedChallengeResponse>> getRecommendedChallenges();


    @POST("challenges/ongoing/filter")
    Call<List<ChallengeCardResponse>> filterOngoingChallenges(@Body ChallengeFilterRequest request);

    @POST("challenges/completed/filter")
    Call<List<ChallengeCardResponse>> filterCompletedChallenges(@Body ChallengeFilterRequest request);

    @POST("api/v1/challenges/shared/filter")
    Call<List<ChallengeCardResponse>> filterSharedChallenges(@Body ChallengeFilterRequest request);

    @POST("api/v1/challenges/recommended/mobti/filter")
    Call<List<ChallengeCardResponse>> filterRecommendedChallenges(@Body ChallengeFilterRequest request);

    @POST("api/v1/challenges/create")
    Call<ResponseBody> createChallenge(@Body ChallengeCreateRequest request);

    @POST("api/v1/challenges/join")
    Call<UserChallengeResponse> joinChallenge(@Body UserChallengeRequest request);

    @POST("api/v1/challenges/status")
    Call<ChallengeStatusResponse> updateChallengeStatus(
            @Body ChallengeStatusRequest request
    );

    @GET("api/v1/challenges/{challengeId}")
    Call<ChallengeDetailResponse> getChallengeDetail(@Path("challengeId") Long challengeId);
}