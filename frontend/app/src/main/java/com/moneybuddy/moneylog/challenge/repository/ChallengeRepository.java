package com.moneybuddy.moneylog.challenge.repository;

import android.content.SharedPreferences;
import android.util.Log;

import com.moneybuddy.moneylog.challenge.dto.ChallengeCardResponse;
import com.moneybuddy.moneylog.challenge.dto.ChallengeCreateRequest;
import com.moneybuddy.moneylog.challenge.dto.ChallengeDetailResponse;
import com.moneybuddy.moneylog.challenge.dto.ChallengeFilterRequest;
import com.moneybuddy.moneylog.challenge.dto.UserChallengeRequest;
import com.moneybuddy.moneylog.challenge.dto.UserChallengeResponse;
import com.moneybuddy.moneylog.challenge.model.ChallengeFilter;
import com.moneybuddy.moneylog.challenge.network.ChallengeApiService;
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

    public Call<List<ChallengeCardResponse>> filterChallenges(
            ChallengeFilter filter,
            String type,                 // 지출 / 저축 / 습관
            List<String> categories
    ) {
        ChallengeFilterRequest request = new ChallengeFilterRequest(type, categories);

        switch (filter) {
            case ONGOING:
                return apiService.filterOngoingChallenges(request);
            case COMPLETED:
                return apiService.filterCompletedChallenges(request);
            case RECOMMENDED:
                return apiService.filterRecommendedChallenges(request);
            default:
                return apiService.filterSharedChallenges(request);
        }
    }

    public void getTodoList(Callback<List<ChallengeCardResponse>> callback) {
        apiService.getOngoingChallenges().enqueue(new Callback<List<ChallengeCardResponse>>() {
            @Override
            public void onResponse(Call<List<ChallengeCardResponse>> call, Response<List<ChallengeCardResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // ▼▼▼ 디버깅용 로그 추가 ▼▼▼
                    List<ChallengeCardResponse> fullList = response.body();
                    Log.d("TODO_FILTER_DEBUG", "전체 진행 중 챌린지 개수: " + fullList.size());

                    List<ChallengeCardResponse> filtered = new ArrayList<>();
                    for (ChallengeCardResponse c : fullList) {
                        // 각 챌린지의 isAccountLinked 값을 직접 확인
                        Log.d("TODO_FILTER_DEBUG", "챌린지 '" + c.getTitle() + "'의 isAccountLinked: " + c.isAccountLinked());

                        if (c.isAccountLinked() != null && !c.isAccountLinked()) {
                            filtered.add(c);
                        }
                    }
                    Log.d("TODO_FILTER_DEBUG", "필터링 후 투두리스트 개수: " + filtered.size());
                    // ▲▲▲ 여기까지 ▲▲▲

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

    public void getChallengeDetail(Long challengeId, Callback<ChallengeDetailResponse> callback) {
        apiService.getChallengeDetail(challengeId).enqueue(callback);
    }
}
