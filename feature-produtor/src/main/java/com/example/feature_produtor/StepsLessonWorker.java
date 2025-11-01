package com.example.feature_produtor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.core.network.RetrofitClientMongo;
import com.example.core.network.RetrofitClientPostgres; // <-- IMPORTANTE: Cliente Postgres
import com.example.core.network.RetrofitClientRedis;
import com.example.feature_produtor.adapter.StepsLessonAdapter;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.api.ApiPostgres; // <-- Assumindo que você criou ApiPostgres
import com.example.feature_produtor.api.ApiRedis;
import com.example.feature_produtor.api.ProgressApiHelper;
import com.example.core.model.mongo.Class;
import com.example.core.model.Program;
import com.example.feature_produtor.model.redis.StepResponse;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;


import com.google.android.material.textfield.TextInputEditText;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StepsLessonWorker extends Fragment implements StepsLessonAdapter.OnStepClickListener {

    private static final String TAG = "StepsLessonWorker";

    private Bundle bundle;
    private ImageView perfil;
    private TextInputEditText pesquisa;
    private ImageView config;
    private ImageView notificacao;
    private RecyclerView recyclerEtapas;
    private Button comecar;
    private StepsLessonAdapter stepsLessonAdapter;
    private int currentProgramProgress = 0;
    private int totalClasses = 0;
    private double progressPerClass = 0.0;

    private TextView descricao;

    private List<Class> allLessons = new ArrayList<>();

    private Integer programId;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getArguments();

        if (bundle != null) {
            programId = bundle.getInt("programId", -1);

            //recebe o prpgresso atual do curso clicado
            currentProgramProgress = getArguments().getInt("currentProgress", 0);

            if (programId == -1) {
                programId = null;
            }
        }

        if (programId == null && isAdded()) {
            Toast.makeText(getContext(), "Erro: ID do curso não encontrado.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "programId está nulo. Verifique o argumento 'programId' recebido.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steps_lesson_worker, container, false);

        // Mapeamento de Views
        perfil = view.findViewById(R.id.perfil);
        pesquisa = view.findViewById(R.id.txtPesquisa);
        config = view.findViewById(R.id.config);
        notificacao = view.findViewById(R.id.icon_notifi);
        recyclerEtapas = view.findViewById(R.id.recycler_etapas);
        comecar = view.findViewById(R.id.btComeçar);
        descricao = view.findViewById(R.id.descricao);

        stepsLessonAdapter = new StepsLessonAdapter(this, requireContext());

        recyclerEtapas.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false));
        recyclerEtapas.setAdapter(stepsLessonAdapter);

        if (programId != null) {
            fetchProgramDetails(programId);

            fetchClassesByProgramId(programId);
        }

        setupClickListeners(programId);
        setupSearchListener();

        return view;
    }



    //pegando o id do worker
    private Integer getWorkerIdFromLocalStore() {
        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int workerId = sp.getInt("user_id", -1);
        return workerId != -1 ? workerId : null;
    }

    //atualizando progresso do curso
//    private void updateProgramProgress(int programId, int percentage) {
//        Integer workerId = getWorkerIdFromLocalStore();
//        if (workerId == null) return;
//
//        ApiPostgres client = RetrofitClientPostgres
//                .getInstance(requireContext())
//                .create(ApiPostgres.class);
//
//        ProgressUpdatePayload request = new ProgressUpdatePayload(programId, percentage);
//
//        client.updateProgramProgress(workerId, request)
//                .enqueue(new Callback<Void>() {
//                    @Override
//                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
//                        if (response.isSuccessful()) {
//                            Log.d("PROGRESS", "Progresso atualizado para " + percentage + "% no programa " + programId);
//                        } else {
//                            Log.e("PROGRESS", "Falha ao atualizar progresso. Code: " + response.code());
//                        }
//                    }
//                    @Override
//                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
//                        Log.e("PROGRESS", "Erro de conexão ao atualizar progresso: " + t.getMessage());
//                    }
//                });
//    }


    //pega a descrição do curso
    private void fetchProgramDetails(Integer id) {
        ApiPostgres apiPostgres = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        Call<Program> call = apiPostgres.getProgramById(id);

        call.enqueue(new Callback<Program>() {
            @Override
            public void onResponse(@NonNull Call<Program> call, @NonNull Response<Program> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Program program = response.body();


                    // Atualiza a Descrição
                    if (descricao != null && program.getDescription() != null) {
                        descricao.setText(program.getDescription());
                    } else if (descricao != null) {
                        descricao.setText("Descrição não disponível para este curso.");
                    }

                } else {
                    Log.e(TAG, "Falha ao buscar Program: Código " + response.code());
                    if (isAdded() && descricao != null) {
                        Toast.makeText(getContext(), "Erro ao carregar descrição: " + response.code(), Toast.LENGTH_LONG).show();
                        descricao.setText("Erro ao carregar descrição.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Program> call, @NonNull Throwable t) {
                if (!isAdded()) return;

                Log.e(TAG, "Erro de conexão ao buscar Program: " + t.getMessage(), t);
                if (isAdded() && descricao != null) {
                    Toast.makeText(getContext(), "Erro de conexão (Descrição): " + t.getMessage(), Toast.LENGTH_LONG).show();
                    descricao.setText("Falha de conexão.");
                }
            }
        });
    }

    private void fetchClassesByProgramId(Integer id) {
        ApiMongo apiMongo = RetrofitClientMongo
                .getInstance(requireContext())
                .create(ApiMongo.class);


        Log.d(TAG, "Tentando buscar classes para programId: " + id);
        Call<List<Class>> call = apiMongo.getClassByProgramId(id);

        call.enqueue(new Callback<List<Class>>() {
            @Override
            public void onResponse(@NonNull Call<List<Class>> call, @NonNull Response<List<Class>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allLessons = response.body();

                    totalClasses = allLessons.size();
                    if (totalClasses > 0) {
                        // 100% dividido pelo número de aulas
                        progressPerClass = 100.0 / totalClasses;
                        Log.d(TAG, "Progresso por etapa (Total): " + progressPerClass + "%");
                    }

                    Log.d(TAG, "Classes carregadas com sucesso. Total: " + totalClasses);

                    stepsLessonAdapter.submitList(allLessons);
                } else {
                    Log.e(TAG, "Falha ao buscar Classes: Código " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Class>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Erro de conexão ao buscar Classes: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WorkerBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.LessonsWorker,
                    R.id.HomePageWorker,
                    R.id.GoalsPageWorker
            );
        }
    }


    private void setupClickListeners(Integer programId) {
        perfil.setOnClickListener(v -> Navigation.findNavController(v).navigate(com.example.core.R.id.Profile));

        config.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Configurações clicadas", Toast.LENGTH_SHORT).show();
        });

        comecar.setOnClickListener(v -> {
            if (!allLessons.isEmpty() && getView() != null) {
                ApiRedis apiRedis = RetrofitClientRedis
                        .getInstance(requireContext())
                        .create(ApiRedis.class);

                SharedPreferences p = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                Integer workerId = p.getInt("user_id",  -1);

                Log.d(TAG, "Tentando buscar a etapa para programId=" + programId +" e workerId=" + workerId );
                Call<StepResponse> call = apiRedis.getStep(workerId, programId);

                call.enqueue(new Callback<StepResponse>() {
                    @Override
                    public void onResponse(Call<StepResponse> call, Response<StepResponse> response) {
                        StepResponse step = response.body();

                        int stepNumber = 1;
                        if (step.getStatus() == 200) {
                            stepNumber = Integer.parseInt(step.getValue());
                        }

                        Log.d(TAG, "Número da etapa: "+stepNumber);

                        Class firstLesson = allLessons.get(stepNumber - 1);

                        Log.d(TAG, "Step: " + firstLesson);

                        onStepClick(firstLesson, stepNumber);
                    }

                    @Override
                    public void onFailure(Call<StepResponse> call, Throwable throwable) {
                        Log.e(TAG, "Falha na requisição de step no Redis: " + throwable.getMessage() + throwable);
                    }
                });

            } else {
                Toast.makeText(getContext(), "Nenhuma etapa carregada para começar.", Toast.LENGTH_SHORT).show();
            }
        });


        config.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Configurações clicadas", Toast.LENGTH_SHORT).show();
        });

        notificacao.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notificações clicadas", Toast.LENGTH_SHORT).show();
        });

    }

    private void setupSearchListener() {
        if (pesquisa != null) {
            pesquisa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLessons(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterLessons(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<Class> filteredList = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(allLessons);
        } else {
            for (Class lesson : allLessons) {
                if (lesson.getTitle() != null && lesson.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(lesson);
                }
            }
        }
        stepsLessonAdapter.submitList(filteredList);
    }

    @Override
    public void onStepClick(Class item, Integer stepNumber) {
        if (programId == null || allLessons.isEmpty()) return;

        // divisão de progresso (1/4, 1/4 e 2/4 para atividades)
        double initialProgress = progressPerClass / 4.0;

        // somente 25% do progresso da etapa é concluido nessa tela
        double remainingStepProgress = progressPerClass * 0.75;

        int newProgress = (int) Math.round(currentProgramProgress + initialProgress);
        newProgress = Math.min(newProgress, 100);

        Log.d(TAG, "Step Clicked: " + item.getTitle() + " | Initial Gain: " + initialProgress + "%");

        int clickedIndex = allLessons.indexOf(item);

        if (clickedIndex >= 0) {
            Integer workerId = getWorkerIdFromLocalStore();
            if (workerId != null && programId != null) {
                ProgressApiHelper.updateProgramProgress(requireContext(), programId, currentProgramProgress, workerId);
            }
            currentProgramProgress = newProgress; // Atualiza o valor interno
        }

        Integer classId = item.getId();

        bundle.putInt("stepId", classId);
        bundle.putInt("stepNumber", stepNumber);
        bundle.putInt("programId", programId);

        // Passa o valor restante (75%) e o novo progresso total
        bundle.putDouble("remainingStepProgress", remainingStepProgress);
        bundle.putInt("currentProgramProgress", currentProgramProgress);

        if(getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.ContentLessonWorker, bundle);
        }

    }
}