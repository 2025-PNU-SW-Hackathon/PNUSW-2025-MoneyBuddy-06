package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.dto.*;
import com.moneybuddy.moneylog.model.ChallengeFilter;
import com.moneybuddy.moneylog.network.ChallengeApiService;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChallengeRepository {
    private final ChallengeApiService apiService;
    public ChallengeRepository(ChallengeApiService apiService) { this.apiService = apiService; }

    public Call<?> getChallenges(ChallengeFilter filter) {
        switch (filter) {
            case ONGOING: return apiService.getOngoingChallenges();
            case COMPLETED: return apiService.getCompletedChallenges();
            case RECOMMENDED: return apiService.getRecommendedChallenges();
            default: return apiService.getSharedChallenges();
        }
    }

    public Call<List<ChallengeCardResponse>> filterChallenges(ChallengeFilter filter, List<String> categories) {
        ChallengeFilterRequest request = new ChallengeFilterRequest(categories);
        switch (filter) {
            case ONGOING: return apiService.filterOngoingChallenges(request);
            case COMPLETED: return apiService.filterCompletedChallenges(request);
            case RECOMMENDED: return apiService.filterRecommendedChallenges(request);
            default: return apiService.filterSharedChallenges(request);
        }
    }

    public void getTodoList(Callback<List<ChallengeCardResponse>> callback) {
        apiService.getOngoingChallenges().enqueue(new Callback<List<ChallengeCardResponse>>() {
            @Override
            public void onResponse(Call<List<ChallengeCardResponse>> call, Response<List<ChallengeCardResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChallengeCardResponse> filtered = new ArrayList<>();
                    for (ChallengeCardResponse c : response.body()) {
                        if (c.isAccountLinked() != null && !c.isAccountLinked()) filtered.add(c);
                    }
                    callback.onResponse(call, Response.success(filtered));
                } else {
                    callback.onResponse(call, response);
                }
            }
            @Override
            public void onFailure(Call<List<ChallengeCardResponse>> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void createChallenge(ChallengeCreateRequest request, Callback<ResponseBody> callback) {
        apiService.createChallenge(request).enqueue(callback);
    }

    public void joinChallenge(UserChallengeRequest request, Callback<UserChallengeResponse> callback) {
        apiService.joinChallenge(request).enqueue(callback);
    }
}
