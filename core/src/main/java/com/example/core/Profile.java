package com.example.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.adapter.LessonsCardProgressAdapter;
import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.ProgramWorkerResponseDTO;
import com.example.core.network.RetrofitClientPostgres;
import com.example.core.ui.CircularProgressView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends Fragment implements LessonsCardProgressAdapter.OnLessonClickListener {

    private ApiPostgresClient api;
    private static final String TAG = "ProfileFragment";

    private RecyclerView recyclerCursosAndamento;

    private View loadingAndamentoLayout;

    private LessonsCardProgressAdapter andamentoLessonsAdapter;

    private CircularProgressView circularProgressGoals, circularProgressPrograms;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        api = RetrofitClientPostgres.getApiService(getContext());

        recyclerCursosAndamento = view.findViewById(R.id.rv_doing_programs);
        loadingAndamentoLayout = view.findViewById(R.id.layout_cursos_andamento_loading);
        circularProgressGoals = view.findViewById(R.id.circularProgressGoals);
        circularProgressPrograms = view.findViewById(R.id.circularProgressPrograms);

        andamentoLessonsAdapter = new LessonsCardProgressAdapter(this, getContext());
        recyclerCursosAndamento.setAdapter(andamentoLessonsAdapter);

        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int id = sp.getInt("user_id", -1);
        String kind = sp.getString("tipo_usuario", null);
        String name = sp.getString("nmae", "Usuário");

        if (id <= 0 || kind == null) {
            Toast.makeText(getContext(), "Parâmetros inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        ((TextView)view.findViewById(R.id.nome_worker)).setText(name);

        if ("COMPANY".equals(kind)) {
            fetchCompanyProgress(id);
            fetchCompanyPrograms(id);
        } else {
            fetchWorkerProgress(id);
            fetchWorkerPrograms(id);
        }
    }


    private void fetchCompanyProgress(int companyId) {
        fetchCompanyGoalProgress(companyId);
        fetchCompanyProgramProgress(companyId);
    }

    private void fetchCompanyProgramProgress(int companyId) {
        api.findAverageProgressPercentageById(companyId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressPrograms.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de cursos", t);
            }
        });
    }

    private void fetchCompanyGoalProgress(int companyId) {
        api.findAverageFinishedGoalsById(companyId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressGoals.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de metas", t);
            }
        });
    }

    private void fetchWorkerProgress(int workerId) {
        fetchWorkerGoalProgress(workerId);
        fetchWorkerProgramProgress(workerId);
    }

    private void fetchWorkerGoalProgress(int workerId) {
        api.findOverallGoalsProgressById(workerId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressGoals.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de metas", t);
            }
        });
    }

    private void fetchWorkerProgramProgress(int workerId) {
        api.findOverallProgramsProgressById(workerId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressPrograms.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de cursos", t);
            }
        });
    }

    private void fetchCompanyPrograms(int companyId) {
        recyclerCursosAndamento.setVisibility(View.GONE);
        loadingAndamentoLayout.setVisibility(View.VISIBLE);

        //chama o endpoint que lista os programs por id do worker
        api.listActualProgramsByCompanyId(companyId).enqueue(new Callback<List<ProgramWorkerResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Response<List<ProgramWorkerResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadingAndamentoLayout.setVisibility(View.GONE);
                    recyclerCursosAndamento.setVisibility(View.VISIBLE);
                    List<ProgramWorkerResponseDTO> programs = response.body();

                    // Filtra usando Streams para clareza
                    List<ProgramWorkerResponseDTO> inProgress = programs.stream()
                            .filter(p -> p.getProgressPercentage() > 0 && p.getProgressPercentage() < 100)
                            .collect(Collectors.toList());

                    // Submete às Recyclers
                    andamentoLessonsAdapter.submitList(new ArrayList<>(inProgress));
                }


                else {
                    loadingAndamentoLayout.setVisibility(View.VISIBLE);
                    recyclerCursosAndamento.setVisibility(View.GONE);
                    Log.e(TAG, "Falha ao carregar programas. CODE: " + response.code() + " URL: " + call.request().url());
                    //   Toast.makeText(getContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                    // se o erro for 404 td bem pq o worker pode simplismente não ter iniciado nenhum curso ainda
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Throwable t) {
                loadingAndamentoLayout.setVisibility(View.VISIBLE);
                recyclerCursosAndamento.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
            }
        });
    }

    private void fetchWorkerPrograms(Integer workerId) {
        recyclerCursosAndamento.setVisibility(View.GONE);
        loadingAndamentoLayout.setVisibility(View.VISIBLE);

        //chama o endpoint que lista os programs por id do worker
        api.listActualProgramsById(workerId).enqueue(new Callback<List<ProgramWorkerResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Response<List<ProgramWorkerResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadingAndamentoLayout.setVisibility(View.GONE);
                    recyclerCursosAndamento.setVisibility(View.VISIBLE);
                    List<ProgramWorkerResponseDTO> programs = response.body();

                    // Filtra usando Streams para clareza
                    List<ProgramWorkerResponseDTO> inProgress = programs.stream()
                            .filter(p -> p.getProgressPercentage() > 0 && p.getProgressPercentage() < 100)
                            .collect(Collectors.toList());

                    // Submete às Recyclers
                    andamentoLessonsAdapter.submitList(new ArrayList<>(inProgress));
                }


                else {
                    loadingAndamentoLayout.setVisibility(View.VISIBLE);
                    recyclerCursosAndamento.setVisibility(View.GONE);
                    Log.e(TAG, "Falha ao carregar programas. CODE: " + response.code() + " URL: " + call.request().url());
                    //   Toast.makeText(getContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                    // se o erro for 404 td bem pq o worker pode simplismente não ter iniciado nenhum curso ainda
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Throwable t) {
                loadingAndamentoLayout.setVisibility(View.VISIBLE);
                recyclerCursosAndamento.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
            }
        });
    }

    private void logHttpError(Response<?> resp) {
        Log.e(TAG, "HTTP " + resp.code() + " - " + resp.message());
    }

    @Override
    public void onLessonClick(ProgramWorkerResponseDTO item) {

    }
}
