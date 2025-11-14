package com.moneybuddy.moneylog.challenge.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.moneybuddy.moneylog.challenge.dto.ChallengeCardResponse;
import com.moneybuddy.moneylog.challenge.dto.ChallengeCreateRequest;
import com.moneybuddy.moneylog.challenge.dto.ChallengeDetailResponse;
import com.moneybuddy.moneylog.challenge.dto.RecommendedChallengeResponse;
import com.moneybuddy.moneylog.challenge.dto.UserChallengeRequest;
import com.moneybuddy.moneylog.challenge.dto.UserChallengeResponse;
import com.moneybuddy.moneylog.challenge.model.ChallengeFilter;
import com.moneybuddy.moneylog.challenge.network.ChallengeApiService;
import com.moneybuddy.moneylog.challenge.repository.ChallengeRepository;
import com.moneybuddy.moneylog.common.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChallengeViewModel extends AndroidViewModel {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_REP_CHALLENGE_ID = "representative_challenge_id";
    private final ChallengeRepository repository;
    private final MutableLiveData<List<ChallengeCardResponse>> challengeList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<List<ChallengeCardResponse>> todoList = new MutableLiveData<>();
    private final MutableLiveData<String> createResult = new MutableLiveData<>();
    private final MutableLiveData<String> joinResult = new MutableLiveData<>();
    private final MutableLiveData<ChallengeDetailResponse> representativeChallenge = new MutableLiveData<>();
    private ChallengeFilter currentFilter = ChallengeFilter.ONGOING;

    private List<String> currentAppliedCategories = null;

    private final MutableLiveData<Boolean> _representativeChallengeCleared = new MutableLiveData<>(false);
    public LiveData<Boolean> getRepresentativeChallengeCleared() { return _representativeChallengeCleared; }


    public ChallengeViewModel(@NonNull Application application) {
        super(application);
        ChallengeApiService apiService = RetrofitClient.getService(application, ChallengeApiService.class);
        this.repository = new ChallengeRepository(apiService);
        loadChallenges();
        loadTodoList();
    }

    public LiveData<List<ChallengeCardResponse>> getChallengeList() { return challengeList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<List<ChallengeCardResponse>> getTodoList() { return todoList; }
    public LiveData<String> getCreateResult() { return createResult; }
    public LiveData<ChallengeDetailResponse> getRepresentativeChallenge() { return representativeChallenge; }
    public LiveData<String> getJoinResult() { return joinResult; }
    public ChallengeFilter getCurrentFilter() { return currentFilter; }

    // 1. setFilter 메서드 수정
    public void setFilter(ChallengeFilter filter) {
        this.currentFilter = filter;
        // 카테고리 필터 상태를 초기화하고
        this.currentAppliedCategories = null;
        // loadChallenges를 호출합니다.
        loadChallenges();

        if (filter == ChallengeFilter.ONGOING) loadTodoList();
    }

    public void applyCategoryFilter(List<String> categories) {
        // 사용자가 선택한 카테고리를 저장하고
        if (categories == null || categories.isEmpty()) {
            this.currentAppliedCategories = null;
        } else {
            this.currentAppliedCategories = categories;
        }
        // loadChallenges를 호출합니다.
        loadChallenges();
    }

    public void loadRepresentativeChallenge(Long challengeId) {
        repository.getChallengeDetail(challengeId, new Callback<ChallengeDetailResponse>() {
            @Override
            public void onResponse(Call<ChallengeDetailResponse> call, Response<ChallengeDetailResponse> response) {
                if (response.isSuccessful()) {
                    representativeChallenge.postValue(response.body());
                } else {
                    errorMessage.postValue("대표 챌린지 로드 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChallengeDetailResponse> call, Throwable t) {
                errorMessage.postValue("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void loadChallenges() {
        isLoading.setValue(true);

        // 저장된 카테고리 필터가 있는지 확인
        if (currentAppliedCategories != null && !currentAppliedCategories.isEmpty()) {
            repository.filterChallenges(currentFilter, currentAppliedCategories).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<List<ChallengeCardResponse>> call, Response<List<ChallengeCardResponse>> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        challengeList.postValue(new ArrayList<>(response.body()));
                    } else {
                        errorMessage.postValue("필터링 새로고침 실패: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<List<ChallengeCardResponse>> call, Throwable t) {
                    isLoading.postValue(false);
                    errorMessage.postValue("네트워크 오류: " + t.getMessage());
                }
            });

        } else {

            if (currentFilter == ChallengeFilter.RECOMMENDED) {
                // '추천' 탭 로직
                ((Call<List<RecommendedChallengeResponse>>) repository.getChallenges(currentFilter)).enqueue(new Callback<List<RecommendedChallengeResponse>>() {
                    @Override
                    public void onResponse(Call<List<RecommendedChallengeResponse>> call, Response<List<RecommendedChallengeResponse>> response) {
                        isLoading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<RecommendedChallengeResponse> recommendedList = response.body();
                            List<ChallengeCardResponse> convertedList = new ArrayList<>();
                            for (RecommendedChallengeResponse rec : recommendedList) {
                                convertedList.add(new ChallengeCardResponse(rec));
                            }
                            challengeList.postValue(convertedList);
                        } else {
                            errorMessage.postValue("데이터 로드 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<RecommendedChallengeResponse>> call, Throwable t) {
                        isLoading.postValue(false);
                        errorMessage.postValue("네트워크 오류: " + t.getMessage());
                    }
                });
            } else {
                // '전체', '진행 중', '진행 완료' 탭 로직
                ((Call<List<ChallengeCardResponse>>) repository.getChallenges(currentFilter)).enqueue(new Callback<List<ChallengeCardResponse>>() {
                    @Override
                    public void onResponse(Call<List<ChallengeCardResponse>> call, Response<List<ChallengeCardResponse>> response) {
                        isLoading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<ChallengeCardResponse> challenges = response.body();
                            challengeList.postValue(challenges);
                            if (currentFilter == ChallengeFilter.ONGOING) {
                                checkAndClearRepresentativeChallenge(challenges);
                            }
                        } else {
                            errorMessage.postValue("데이터 로드 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ChallengeCardResponse>> call, Throwable t) {
                        isLoading.postValue(false);
                        errorMessage.postValue("네트워크 오류: " + t.getMessage());
                    }
                });
            }
        }
    }

    private void checkAndClearRepresentativeChallenge(List<ChallengeCardResponse> ongoingChallenges) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long repId = prefs.getLong(KEY_REP_CHALLENGE_ID, -1L);

        if (repId == -1L) {
            return; // 대표 챌린지가 설정되지 않았으면 아무것도 안 함
        }

        boolean found = false;
        for (ChallengeCardResponse challenge : ongoingChallenges) {
            if (challenge.getChallengeId().equals(repId)) {
                found = true;
                break;
            }
        }

        if (!found) {
            // 진행 중인 챌린지 목록에 대표 챌린지가 없으면 해제
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_REP_CHALLENGE_ID, -1L);
            editor.apply();

            representativeChallenge.postValue(null);

            _representativeChallengeCleared.postValue(true); // UI 업데이트를 위해 신호 보내기
        }
    }

    public void loadTodoList() {
        repository.getTodoList(new Callback<List<ChallengeCardResponse>>() {
            @Override
            public void onResponse(Call<List<ChallengeCardResponse>> call, Response<List<ChallengeCardResponse>> response) {
                if (response.isSuccessful()) todoList.postValue(response.body());
                else errorMessage.postValue("오늘의 할 일 로드 실패");
            }
            @Override
            public void onFailure(Call<List<ChallengeCardResponse>> call, Throwable t) {
                errorMessage.postValue("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void createChallenge(ChallengeCreateRequest request) {
        repository.createChallenge(request, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                createResult.postValue(response.isSuccessful() ? "챌린지 생성 성공!" : "생성 실패: " + response.code());
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                createResult.postValue("네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void joinChallenge(Long challengeId) {
        repository.joinChallenge(new UserChallengeRequest(challengeId), new Callback<UserChallengeResponse>() {
            @Override
            public void onResponse(Call<UserChallengeResponse> call, Response<UserChallengeResponse> response) {
                joinResult.postValue(response.isSuccessful() ? "챌린지 참여 성공!" : "참여 실패: " + response.code());
            }
            @Override
            public void onFailure(Call<UserChallengeResponse> call, Throwable t) {
                joinResult.postValue("네트워크 오류: " + t.getMessage());
            }
        });
    }
}