package com.example.feature_produtor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_produtor.model.mongo.Class;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.api.ApiPostgres;
import com.example.feature_produtor.dto.request.ProgressUpdatePayload;

import com.google.android.material.button.MaterialButton;
import com.example.core.network.RetrofitClientMongo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ContentLessonWorker extends Fragment {

    private static final String TAG = "ContentLessonWorker";
    private static final String PREF_NAME = "user_session";
    private static final String KEY_WORKER_ID = "user_id";

    private ImageView btComeback;
    private TextView lessonTitleTextView;
    private TextView conteudo;
    private MaterialButton btContinuar;
    private ApiMongo apiMongo;

    private Integer programId = null;
    private int currentStepId = -1;
    private Class currentLesson;

    private Bundle bundle;

    private double remainingProgressValue = 0.0;
    private int currentProgramProgress = 0;

    private List<String> contentPages;
    private int currentPageIndex = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        initRetrofit();
        processArguments();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_lesson_worker, container, false);

        btComeback = view.findViewById(R.id.btComeback);
        lessonTitleTextView = view.findViewById(R.id.titulocontent);
        conteudo = view.findViewById(R.id.tvConteudo);
        btContinuar = view.findViewById(R.id.btComeçar);

        setupClickListeners();

        if (currentStepId != -1 && apiMongo != null) {
            fetchLessonContent(currentStepId);
        } else {
            lessonTitleTextView.setText("Erro de ID");
            conteudo.setText("Não foi possível carregar a etapa. O ID da aula não foi fornecido.");
        }

        return view;
    }

    private void initRetrofit() {
        apiMongo = RetrofitClientMongo
                .getInstance(requireContext())
                .create(ApiMongo.class);
    }

    private void processArguments() {
        if (bundle != null) {
            currentStepId = bundle.getInt("stepId", -1);
            programId = bundle.getInt("programId", -1);
            if (programId == -1) programId = null;

            // RECEBE OS VALORES PARA REPASSAR À PRÓXIMA TELA
            remainingProgressValue = bundle.getDouble("remainingProgressValue", 0.0);
            currentProgramProgress = bundle.getInt("currentProgramProgress", 0);

            Log.d(TAG, "Progresso recebido: " + currentProgramProgress + " | Aumento restante: " + remainingProgressValue);
        }
    }


    private void setupClickListeners() {
        btComeback.setOnClickListener(v -> {
            // Lógica de voltar página ou voltar para a lista de etapas
            if (contentPages != null && contentPages.size() > 1 && currentPageIndex > 0) {
                currentPageIndex--;
                displayCurrentPage();
            } else {
                Navigation.findNavController(v).navigateUp();
            }
        });

        btContinuar.setOnClickListener(v -> {
            if (contentPages == null || contentPages.isEmpty()) {
                Toast.makeText(getContext(), "Conteúdo não carregado.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentPageIndex < contentPages.size() - 1) {
                // AVANÇA PÁGINA
                currentPageIndex++;
                displayCurrentPage();

            } else {
                // ÚLTIMA PÁGINA: NAVEGA PARA A PRÓXIMA TELA (ATIVIDADE)
                navigateToNextScreen();
            }
        });
    }


    private void navigateToNextScreen() {
        if (currentLesson != null && currentLesson.getId() != null && programId != null) {

            bundle.putDouble("progressValue", remainingProgressValue);
            bundle.putInt("currentProgress", currentProgramProgress);

            if(getView() != null) {
                // Navega para a tela ActivityLessonWorker
                Navigation.findNavController(getView()).navigate(R.id.ActivityLessonWorker, bundle);
            }
        } else {
            Log.e(TAG, "Falha na navegação. programId: " + programId + ", lessonId: " + (currentLesson != null ? currentLesson.getId() : "null"));
            Toast.makeText(getContext(), "Erro: Dados do curso incompletos para continuar.", Toast.LENGTH_LONG).show();
        }
    }


    private void fetchLessonContent(int stepId) {
        Call<Class> call = apiMongo.getClassById(stepId);

        call.enqueue(new Callback<Class>() {
            @Override
            public void onResponse(@NonNull Call<Class> call, @NonNull Response<Class> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentLesson = response.body();

                    lessonTitleTextView.setText(
                            currentLesson.getTitle() != null ? currentLesson.getTitle() : "Título não disponível");

                    initializeContentPages(currentLesson.getContent(), currentLesson.getDescription());

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar conteúdo: " + response.code(), Toast.LENGTH_LONG).show();
                    conteudo.setText("Erro ao buscar o conteúdo da API: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Class> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                conteudo.setText("Falha na conexão com o servidor. Verifique a internet.");
            }
        });
    }


    private void initializeContentPages(List<String> rawContentList, String descriptionFallback) {
        contentPages = new ArrayList<>();

        if (rawContentList != null) {
            for (String text : rawContentList) {
                if (text != null && !text.trim().isEmpty()) {
                    contentPages.add(text.trim());
                }
            }
        }

        if (contentPages.isEmpty() && descriptionFallback != null && !descriptionFallback.isEmpty()) {
            contentPages.add(descriptionFallback);
        }

        if (contentPages.isEmpty()) {
            conteudo.setText("Nenhum conteúdo principal disponível para esta aula.");
        } else {
            currentPageIndex = 0;
            displayCurrentPage();
        }
    }

    private void displayCurrentPage() {
        if (contentPages == null || contentPages.isEmpty() || currentPageIndex < 0 || currentPageIndex >= contentPages.size()) {
            conteudo.setText("Erro ao exibir a página.");
            return;
        }

        conteudo.setText(contentPages.get(currentPageIndex));

        boolean isLastPage = currentPageIndex == contentPages.size() - 1;
        btContinuar.setText(isLastPage ? "Ir para Atividade" : "Próximo");
    }

    // --- MÉTODOS DE PROGRESSO (APENAS IMPLEMENTAÇÃO DE BASE) ---
    private Integer getWorkerIdFromLocalStore() {
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int workerId = sp.getInt(KEY_WORKER_ID, -1);
        return workerId != -1 ? workerId : null;
    }

    private void updateProgramProgress(int programId, int percentage) {
        Integer workerId = getWorkerIdFromLocalStore();
        if (workerId == null) return;

        ApiPostgres client = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        ProgressUpdatePayload request = new ProgressUpdatePayload(programId, percentage);

        client.updateProgramProgress(workerId, request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Progresso atualizado para " + percentage + "% no programa " + programId);
                        } else {
                            Log.e(TAG, "Falha ao atualizar progresso. Code: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e(TAG, "Erro de conexão ao atualizar progresso: " + t.getMessage());
                    }
                });
    }
}