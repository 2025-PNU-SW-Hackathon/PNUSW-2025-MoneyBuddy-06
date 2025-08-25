package com.moneybuddy.moneylog.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.moneybuddy.moneylog.dto.*;
import com.moneybuddy.moneylog.model.ChallengeFilter;
import com.moneybuddy.moneylog.network.ApiClient;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChallengeViewModel extends AndroidViewModel {
    private final ChallengeRepository repository;
    private final MutableLiveData<List<Object>> challengeList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<List<ChallengeCardResponse>> todoList = new MutableLiveData<>();
    private final MutableLiveData<String> createResult = new MutableLiveData<>();
    private final MutableLiveData<String> joinResult = new MutableLiveData<>();
    private ChallengeFilter currentFilter = ChallengeFilter.ONGOING;

    public ChallengeViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChallengeRepository(ApiClient.getApiService(application));
        loadChallenges();
    }

    public LiveData<List<Object>> getChallengeList() { return challengeList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<List<ChallengeCardResponse>> getTodoList() { return todoList; }
    public LiveData<String> getCreateResult() { return createResult; }
    public LiveData<String> getJoinResult() { return joinResult; }
    public ChallengeFilter getCurrentFilter() { return currentFilter; }

    public void setFilter(ChallengeFilter filter) {
        this.currentFilter = filter;
        loadChallenges();
        if (filter == ChallengeFilter.RECOMMENDED) loadTodoList();
    }

    public void applyCategoryFilter(List<String> categories) {
        isLoading.setValue(true);
        repository.filterChallenges(currentFilter, categories).enqueue(new Callback<List<ChallengeCardResponse>>() {
            @Override
            public void onResponse(Call<List<ChallengeCardResponse>> call, Response<List<ChallengeCardResponse>> response) {
                if (response.isSuccessful()) challengeList.postValue(new ArrayList<>(response.body()));
                else errorMessage.postValue("필터링 실패: " + response.code());
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(Call<List<ChallengeCardResponse>> call, Throwable t) {
                errorMessage.postValue("네트워크 오류: " + t.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void loadChallenges() {
        isLoading.setValue(true);
        if (currentFilter == ChallengeFilter.RECOMMENDED) {
            ((Call<List<RecommendedChallengeResponse>>) repository.getChallenges(currentFilter)).enqueue(createCallback());
        } else {
            ((Call<List<ChallengeCardResponse>>) repository.getChallenges(currentFilter)).enqueue(createCallback());
        }
    }

    private void loadTodoList() {
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

    private <T> Callback<List<T>> createCallback() {
        return new Callback<List<T>>() {
            @Override
            public void onResponse(Call<List<T>> call, Response<List<T>> response) {
                if (response.isSuccessful()) challengeList.postValue(new ArrayList<>(response.body()));
                else errorMessage.postValue("데이터 로드 실패: " + response.code());
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(Call<List<T>> call, Throwable t) {
                errorMessage.postValue("네트워크 오류: " + t.getMessage());
                isLoading.postValue(false);
            }
        };
    }
}

