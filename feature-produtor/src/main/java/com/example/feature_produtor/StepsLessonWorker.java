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
import com.example.core.network.RetrofitClientPostgres;
import com.example.core.network.RetrofitClientIA;
import com.example.core.network.RetrofitClientRedis;
import com.example.feature_produtor.adapter.StepsLessonAdapter;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.api.ApiPostgres;
import com.example.feature_produtor.api.ApiRedis;
import com.example.feature_produtor.api.ProgressApiHelper;


import com.example.feature_produtor.dto.response.StepResponse;

import com.example.core.model.mongo.Class;
import com.example.core.model.Program;


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

    private int currentRequiredStep = 1;


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
            fetchCurrentStep(programId);
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

    // Adicione este novo método na classe StepsLessonWorker
    private void fetchCurrentStep(Integer programId) {
        if (programId == null || !isAdded()) return;
        Integer workerId = getWorkerIdFromLocalStore();
        if (workerId == null) return;

        // Garante que o workerId e programId são válidos antes de chamar a API
        if (workerId == -1 || programId == -1) {
            Log.e(TAG, "Worker ID ou Program ID inválido para buscar a etapa atual.");
            return;
        }

        ApiRedis apiRedis = RetrofitClientRedis
                .getInstance(requireContext())
                .create(ApiRedis.class);

        Call<StepResponse> call = apiRedis.getStep(workerId, programId);

        call.enqueue(new Callback<StepResponse>() {
            @Override
            public void onResponse(@NonNull Call<StepResponse> call, @NonNull Response<StepResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StepResponse step = response.body();
                    if (step.getStatus() != null && step.getStatus() == 200) {
                        try {
                            // Atualiza a variável de instância com a etapa que o usuário deve fazer
                            currentRequiredStep = Integer.parseInt(step.getValue());
                            Log.d(TAG, "Etapa obrigatória atualizada para: " + currentRequiredStep);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Erro ao converter número da etapa: " + e.getMessage());
                        }
                    }
                } else {
                    Log.w(TAG, "Falha ao buscar etapa no Redis: Código " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StepResponse> call, @NonNull Throwable throwable) {
                Log.e(TAG, "Falha de conexão ao buscar etapa no Redis: " + throwable.getMessage());
            }
        });
    }




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

                // 🌟 USA A VARIÁVEL DE INSTÂNCIA JÁ CARREGADA
                int stepNumberToStart = currentRequiredStep;

                Log.d(TAG, "Botão Começar: Acessando a etapa obrigatória: "+stepNumberToStart);

                // Garante que o índice da lista é válido (stepNumber - 1)
                if (stepNumberToStart >= 1 && stepNumberToStart <= allLessons.size()) {
                    Class lessonToStart = allLessons.get(stepNumberToStart - 1);

                    // Chama onStepClick, que iniciará o processo e navegará.
                    onStepClick(lessonToStart, stepNumberToStart);
                } else {
                    Toast.makeText(getContext(), "Progresso inválido. Por favor, reinicie o aplicativo.", Toast.LENGTH_LONG).show();
                }

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
    public void onProgressUpdated(int newPercentage) {
        // Não usado diretamente aqui, mas necessário pela interface.
    }

    @Override
    public void onError(String message) {
        // Tratamento de erro geral para qualquer progresso que falhe no Fragment
        if (isAdded()) {
            Toast.makeText(getContext(), "Erro ao salvar progresso inicial: " + message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Falha no salvamento do progresso inicial: " + message);
            // IMPORTANTE: NÃO HOUVE NAVEGAÇÃO, O USUÁRIO DEVE TENTAR NOVAMENTE OU CONTINUAR DE ONDE PAROU
        }
    }

    private void navigateToLesson(Class item, Integer stepNumber, double remainingStepProgress) {
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

    // StepsLessonWorker.java

    @Override
    public void onStepClick(Class item, Integer stepNumber) {
        if (programId == null || allLessons.isEmpty()) return;


        // Se o número da etapa clicada for MENOR que a etapa obrigatória atual,
        // significa que ela já foi concluída e o progresso avançou.
        if (stepNumber < currentRequiredStep) {
            Toast.makeText(getContext(),
                    "Essa etapa já foi feita!",
                    Toast.LENGTH_LONG).show();
            return; // Retorna após o aviso e navegação para revisão
        }


        // 2. REGRA DE BLOQUEIO: Etapa FUTURA
        if (stepNumber > currentRequiredStep) {
            Toast.makeText(getContext(),
                    "Você deve completar a Etapa " + currentRequiredStep + " primeiro para liberar a Etapa " + stepNumber + ".",
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Acesso negado: Tentou Etapa " + stepNumber + ", obrigatória é " + currentRequiredStep);
            return;
        }

        // AÇÃO PRINCIPAL: Etapa ATUAL (stepNumber == currentRequiredStep)

        Log.d(TAG, "Acesso permitido: Iniciando Etapa " + stepNumber);


        // Seu código original de API e navegação
        double initialGainValue = progressPerClass * 0.25;
        int percentageGain = (int) Math.round(initialGainValue);
        double remainingStepProgress = progressPerClass * 0.75;


        Integer workerId = getWorkerIdFromLocalStore();

        if (workerId != null && programId != null) {

            int pointsGain = 0;

            ProgressApiHelper.updateProgramProgress(
                    requireContext(),
                    programId,
                    percentageGain,
                    pointsGain,
                    currentProgramProgress,
                    workerId,
                    new ProgressApiHelper.ProgressUpdateCallback() {
                        @Override
                        public void onProgressUpdated(int newPercentage) {
                            currentProgramProgress = newPercentage;
                            // A navegação só ocorre SE a API for bem-sucedida.
                            navigateToLesson(item, stepNumber, remainingStepProgress);
                        }

                        @Override
                        public void onError(String message) {
                           showDialogDennied();
                        }
                    }
            );
        } else {
            Toast.makeText(getContext(), "Erro de ID: Worker ou Program ID inválido.", Toast.LENGTH_LONG).show();
        }
    }




    private void showDialogDennied() {
        // Garante que o Fragment ainda está anexado à Activity
        if (!isAdded()) return;

        //Cria e configura o AlertDialog/
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        // Infla o layout customizado
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_couse_denied, null);
        builder.setView(dialogView);

        // Configura o botão de fechar (OK)
        Button btnClose = dialogView.findViewById(R.id.btn_close_denied_dialog);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Torna o fundo transparente para que o CardView no XML seja visível
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            // Opcional: Navegar de volta para a Home após fechar o diálogo
            if (getView() != null) {
                Navigation.findNavController(getView()).navigateUp();
            }
        });

        dialog.show();
    }




}



