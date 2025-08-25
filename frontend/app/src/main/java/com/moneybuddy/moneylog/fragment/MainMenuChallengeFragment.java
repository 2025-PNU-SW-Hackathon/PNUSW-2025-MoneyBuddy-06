package com.moneybuddy.moneylog.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.activity.ChallengeCategoryActivity;
import com.moneybuddy.moneylog.activity.ChallengeCreateActivity;
import com.moneybuddy.moneylog.adapter.ChallengeAdapter;
import com.moneybuddy.moneylog.dto.ChallengeStatusRequest;
import com.moneybuddy.moneylog.dto.ChallengeStatusResponse;
import com.moneybuddy.moneylog.model.ChallengeFilter;
import com.moneybuddy.moneylog.network.ApiClient;
import com.moneybuddy.moneylog.network.ChallengeApiService;
import com.moneybuddy.moneylog.viewmodel.ChallengeViewModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenuChallengeFragment extends Fragment {
    private ChallengeViewModel viewModel;
    private ChallengeAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ChallengeApiService apiService;

    private ActivityResultLauncher<Intent> categoryFilterLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiService = ApiClient.getApiService(getContext());

        categoryFilterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        ArrayList<String> cats = data.getStringArrayListExtra(ChallengeCategoryActivity.EXTRA_SELECTED_CATEGORIES);
                        if (cats != null && !cats.isEmpty()) {
                            viewModel.applyCategoryFilter(cats);
                        } else {
                            viewModel.loadChallenges();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_main_menu_challenge, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChallengeViewModel.class);
        setupViews(view);
        observeViewModel();
    }

    private void setupViews(View view) {
        RecyclerView rv = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout);
        RadioGroup rg = view.findViewById(R.id.radioGroup4);

        adapter = new ChallengeAdapter(getContext(), (challengeId, isChecked) -> {
            updateStatusToServer(challengeId, isChecked);
        });

        // 화면 로딩 직후 ongoing이 checked상태이므로 상단에 todolist 표시
        adapter.setShowHeader(true);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> viewModel.loadChallenges());
        view.findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(getContext(), ChallengeCreateActivity.class)));

        view.findViewById(R.id.imageButton2).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChallengeCategoryActivity.class);
            categoryFilterLauncher.launch(intent);
        });

        rg.setOnCheckedChangeListener((group, id) -> {
            ChallengeFilter filter;
            if (id == R.id.radioButton10) {
                filter = ChallengeFilter.ALL;
                adapter.setShowHeader(false);
            } else if (id == R.id.radioButton11) {
                filter = ChallengeFilter.RECOMMENDED;
                adapter.setShowHeader(false);
            } else if (id == R.id.radioButton12) {
                filter = ChallengeFilter.ONGOING;
                adapter.setShowHeader(true);
            } else { // R.id.radioButton13 (완료)
                filter = ChallengeFilter.COMPLETED;
                adapter.setShowHeader(false);
            }
            viewModel.setFilter(filter);
        });
    }

    private void observeViewModel() {
        viewModel.getChallengeList().observe(getViewLifecycleOwner(), list -> adapter.submitList(list));
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> swipeRefresh.setRefreshing(loading));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show());
        viewModel.getTodoList().observe(getViewLifecycleOwner(), todos -> {
            if (viewModel.getCurrentFilter() == ChallengeFilter.RECOMMENDED) {
                adapter.setTodoList(todos);
            }
        });
    }

    private void updateStatusToServer(Long challengeId, boolean isCompleted) {
        if (apiService == null) return;

        ChallengeStatusRequest request = new ChallengeStatusRequest();
        request.setChallengeId(challengeId);
        request.setTodayCompleted(isCompleted);

        apiService.updateChallengeStatus(request).enqueue(new Callback<ChallengeStatusResponse>() {
            @Override
            public void onResponse(Call<ChallengeStatusResponse> call, Response<ChallengeStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_SUCCESS", "상태 업데이트 성공: " + response.body().getMessage());
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("API_ERROR", "응답 실패: " + response.code());
                    Toast.makeText(getContext(), "상태 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChallengeStatusResponse> call, Throwable t) {
                Log.e("API_FAILURE", "통신 실패: " + t.getMessage());
                Toast.makeText(getContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}