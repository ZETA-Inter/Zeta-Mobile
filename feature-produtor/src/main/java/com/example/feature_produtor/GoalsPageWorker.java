package com.example.feature_produtor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_produtor.adapter.GoalsAdapter;
import com.example.feature_produtor.api.ApiPostgres;
import com.example.feature_produtor.dto.response.GoalResponseDTO;
import com.example.feature_produtor.model.postegres.Goal;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalsPageWorker extends Fragment implements GoalsAdapter.OnGoalClickListener{

    private static final String TAG = "GoalsPageWorkerFragment";
    private static final String PREF_NAME = "user_session";
    private static final String KEY_WORKER_ID = "worker_id";

    private RecyclerView recyclerMetas;
    private TextInputEditText txtPesquisa;
    private GoalsAdapter goalsAdapter;

    private View loadingGoalsContainer;

    // Lista para armazenar todas as metas buscadas (necessário para a pesquisa)
    private final List<Goal> allGoals = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals_page_worker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecycler();
        setupBottomNav(view);
        setupSearchLogic();
        fetchGoals();
    }

    private void initViews(View view) {
        recyclerMetas = view.findViewById(R.id.recycler_metas);
        txtPesquisa = view.findViewById(R.id.txtPesquisa2);
        //se não houver meta atribuida a esse worker esse card fica visível
        loadingGoalsContainer = view.findViewById(R.id.loading_goals_container);

    }

    private void setupRecycler() {
        goalsAdapter = new GoalsAdapter(this);
        recyclerMetas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMetas.setAdapter(goalsAdapter);
    }

    private Integer getWorkerIdFromLocalStore() {
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int workerId = sp.getInt(KEY_WORKER_ID, -1);
        return workerId != -1 ? workerId : null;
    }

    // Buscando na api

    private void fetchGoals() {
        Integer workerId = getWorkerIdFromLocalStore();

        if (workerId == null) {
            Toast.makeText(getContext(), "Erro: ID do Worker não encontrado.", Toast.LENGTH_LONG).show();
            return;
        }
        loadingGoalsContainer.setVisibility(View.VISIBLE);
        recyclerMetas.setVisibility(View.GONE);

        ApiPostgres apiPostgres = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        apiPostgres.getGoalsByWorkerId(workerId).enqueue(new Callback<List<Goal>>() {
            @Override
            public void onResponse(@NonNull Call<List<Goal>> call, @NonNull Response<List<Goal>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    loadingGoalsContainer.setVisibility(View.GONE);
                    recyclerMetas.setVisibility(View.VISIBLE);


                    allGoals.clear();
                    allGoals.addAll(response.body());
                    goalsAdapter.submitList(new ArrayList<>(allGoals));
                } else {
                    loadingGoalsContainer.setVisibility(View.VISIBLE);
                    recyclerMetas.setVisibility(View.GONE);

                    Log.e(TAG, "Falha ao carregar metas. CODE: " + response.code());
                    Toast.makeText(getContext(), "Erro ao carregar metas: " + response.code(), Toast.LENGTH_LONG).show();
                    goalsAdapter.submitList(new ArrayList<>()); // Limpa a lista em caso de falha
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Goal>> call, @NonNull Throwable t) {

                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
                goalsAdapter.submitList(new ArrayList<>());
            }
        });
    }

    //  Barra de Pesquisa

    private void setupSearchLogic() {
        txtPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGoals(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void filterGoals(String query) {
        if (query.isEmpty()) {
            goalsAdapter.submitList(new ArrayList<>(allGoals)); // Se vazio, mostra todas
            return;
        }

        final String lowerCaseQuery = query.toLowerCase(Locale.getDefault());


        List<Goal> filteredList = allGoals.stream()
                .filter(goal -> goal.getGoal().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery))
                .collect(Collectors.toList());

        goalsAdapter.submitList(filteredList);
    }

    //  Configuração da Bottom Nav

    private void setupBottomNav(View view) {
        WorkerBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);

            bottom.bindNavController(nav, R.id.HomePageWorker, R.id.LessonsWorker, R.id.GoalsPageWorker);
            bottom.setActive(WorkerBottomNavView.Item.GOALS, false);
        }
    }








    @Override
    public void onGoalClicked(Goal goal) {

    }



}