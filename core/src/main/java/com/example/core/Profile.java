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

import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.CompanyResponse;
import com.example.core.dto.response.WorkerResponse;
import com.example.core.network.RetrofitClientPostgres;
import com.example.core.ui.CircularProgressView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends Fragment {

    private ApiPostgresClient api;
    private static final String TAG = "ProfileFragment";

    private RecyclerView recyclerCursosAndamento;

    private View loadingAndamentoLayout;

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

        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int id = sp.getInt("user_id", -1);
        String kind = sp.getString("tipo_usuario", null);

        if (id <= 0 || kind == null) {
            Toast.makeText(getContext(), "Parâmetros inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("COMPANY".equals(kind)) {
            loadCompany(id, view);
        } else {
            loadWorker(id, view);
            fetchProgress(id);
            fetchPrograms(id);
        }
    }

    private void loadCompany(int companyId, View view) {
        api.findCompanyById(companyId).enqueue(new Callback<CompanyResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompanyResponse> call, @NonNull Response<CompanyResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    CompanyResponse c = resp.body();
                    ((TextView)view.findViewById(R.id.nome_worker)).setText(c.getName());
                } else {
                    logHttpError(resp);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CompanyResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "loadCompany", t);
            }
        });
    }

    private void loadWorker(int workerId, View view) {
        api.findWorkerById(workerId).enqueue(new Callback<WorkerResponse>() {
            @Override
            public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    WorkerResponse w = resp.body();
                    ((TextView)view.findViewById(R.id.nome_worker)).setText(w.getName());
                } else {
                    logHttpError(resp);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "loadWorker", t);
            }
        });
    }

    private void fetchProgress(int workerId) {
        fetchGoalProgress(workerId);
        fetchProgramProgress(workerId);
    }

    private void fetchGoalProgress(int workerId) {
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

    private void fetchProgramProgress(int workerId) {
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

    private void fetchPrograms(Integer workerId) {
        recyclerCursosAndamento.setVisibility(View.GONE);
        loadingAndamentoLayout.setVisibility(View.VISIBLE);

        ApiPostgres apiPostgres = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        //chama o endpoint que lista os programs por id do worker
        apiPostgres.listWorkerProgramsWithProgress(workerId).enqueue(new Callback<List<ProgramWorkerResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Response<List<ProgramWorkerResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadingAndamentoLayout.setVisibility(View.GONE);
                    recyclerCursosAndamento.setVisibility(View.VISIBLE);
                    List<ProgramWorkerResponseDTO> programs = response.body();

                    // Limpa as listas
                    allPrograms.clear();
                    concludedPrograms.clear();

                    // Filtra usando Streams para clareza
                    List<ProgramWorkerResponseDTO> inProgress = programs.stream()
                            .filter(p -> p.getProgressPercentage() > 0 && p.getProgressPercentage() < 100)
                            .collect(Collectors.toList());

                    List<ProgramWorkerResponseDTO> completed = programs.stream()
                            .filter(p -> p.getProgressPercentage() >= 100)
                            .collect(Collectors.toList());

                    allPrograms.addAll(inProgress);
                    concludedPrograms.addAll(completed);

                    // Submete às Recyclers
                    andamentoLessonsAdapter.submitList(new ArrayList<>(allPrograms));
                    concludedLessonsAdapter.submitList(new ArrayList<>(concludedPrograms));
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
}
