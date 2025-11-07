package com.moneybuddy.moneylog.main.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

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
import com.moneybuddy.moneylog.challenge.activity.ChallengeCategoryActivity;
import com.moneybuddy.moneylog.challenge.activity.ChallengeCreateActivity;
import com.moneybuddy.moneylog.challenge.adapter.ChallengeAdapter;
import com.moneybuddy.moneylog.challenge.dto.ChallengeStatusRequest;
import com.moneybuddy.moneylog.challenge.dto.ChallengeStatusResponse;
import com.moneybuddy.moneylog.challenge.model.ChallengeFilter;
import com.moneybuddy.moneylog.challenge.network.ChallengeApiService;
import com.moneybuddy.moneylog.challenge.viewmodel.ChallengeViewModel;
import com.moneybuddy.moneylog.common.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainMenuChallengeFragment extends Fragment implements ChallengeAdapter.OnRepresentativeChallengeClickListener {
    private ChallengeViewModel viewModel;
    private ChallengeAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ChallengeApiService apiService;

    private ImageButton filterButton;

    private ActivityResultLauncher<Intent> categoryFilterLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiService = RetrofitClient.getService(getContext(), ChallengeApiService.class);

        categoryFilterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        ArrayList<String> cats = data.getStringArrayListExtra(ChallengeCategoryActivity.EXTRA_SELECTED_CATEGORIES);

                        viewModel.applyCategoryFilter(cats);
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
        viewModel = new ViewModelProvider(requireActivity()).get(ChallengeViewModel.class);

        setupViews(view);
        observeViewModel();
    }

    private void setupViews(View view) {
        RecyclerView rv = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout);
        RadioGroup rg = view.findViewById(R.id.radioGroup4);
        filterButton = view.findViewById(R.id.imageButton2);

        adapter = new ChallengeAdapter(getContext(), (challengeId, isChecked) -> {
            updateStatusToServer(challengeId, isChecked);
        }, this);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        long currentRepId = prefs.getLong("representative_challenge_id", -1L);
        adapter.setRepresentativeChallengeId(currentRepId);

        // 화면 로딩 직후 ongoing이 checked상태이므로 상단에 todolist 표시
        adapter.setShowHeader(true);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> viewModel.loadChallenges());
        view.findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(getContext(), ChallengeCreateActivity.class)));

        filterButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChallengeCategoryActivity.class);
            categoryFilterLauncher.launch(intent);
        });

        adapter.setCurrentFilter(ChallengeFilter.ONGOING);

        rg.setOnCheckedChangeListener((group, id) -> {
            ChallengeFilter newFilter;
            if (id == R.id.radioButton10) {
                newFilter = ChallengeFilter.ALL;
                adapter.setShowHeader(false);
            } else if (id == R.id.radioButton11) {
                newFilter = ChallengeFilter.RECOMMENDED;
                adapter.setShowHeader(false);
            } else if (id == R.id.radioButton12) {
                newFilter = ChallengeFilter.ONGOING;
                adapter.setShowHeader(true);
            } else { // R.id.radioButton13
                newFilter = ChallengeFilter.COMPLETED;
                adapter.setShowHeader(false);
            }

            // ViewModel에 저장된 '이전' 필터와 '새로운' 필터가 다를 때만,
            // 즉, 사용자가 직접 탭을 눌러서 바꿨을 때만 setFilter를 호출합니다.
            if (viewModel.getCurrentFilter() != newFilter) {
                adapter.setCurrentFilter(newFilter);
                viewModel.setFilter(newFilter);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getChallengeList().observe(getViewLifecycleOwner(), list -> adapter.submitList(list));
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> swipeRefresh.setRefreshing(loading));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show());
        viewModel.getTodoList().observe(getViewLifecycleOwner(), todos -> {
            if (viewModel.getCurrentFilter() == ChallengeFilter.ONGOING) {
                adapter.setTodoList(todos);
            }
        });
    }

    @Override
    public void onRepresentativeChallengeClick(Long challengeId) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("representative_challenge_id", challengeId);
        editor.apply();

        Toast.makeText(getContext(), "대표 챌린지로 설정되었습니다.", Toast.LENGTH_SHORT).show();

        adapter.setRepresentativeChallengeId(challengeId);
        adapter.notifyDataSetChanged();
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