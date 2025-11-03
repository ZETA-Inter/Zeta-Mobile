package com.example.feature_produtor;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.core.model.mongo.Activity;
import com.example.core.network.RetrofitClientMongo;

import com.example.core.network.RetrofitClientIA;

import com.example.core.network.RetrofitClientRedis;

import com.example.feature_produtor.adapter.AnswerAdapter;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.api.ApiRedis;
import com.example.feature_produtor.api.ProgressApiHelper;


import com.example.feature_produtor.dto.request.StepRequest;
import com.example.feature_produtor.dto.response.StepResponse;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLessonWorker extends Fragment implements AnswerAdapter.OnAnswerSelectedListener {

    private Bundle bundle;
    private static final String TAG = "ActivityLessonWorker";
    private static final String PREF_NAME = "user_session";
    private static final String KEY_WORKER_ID = "user_id";

    private ImageView btComeback;
    private TextView tvAtividade;
    private TextView txtPergunta;
    private RecyclerView recyclerAnswers;
    private MaterialButton btContinuar;

    private ApiMongo apiMongo;

    private Integer programId = 0;
    private int classId = -1;

    private int currentProgramProgress = 0;
    private double progressValue = 0.0;
    private double pointsPerQuestion = 0.0;
    private List<Activity.Question> allQuestions;
    private int currentQuestionIndex = 0;

    private AnswerAdapter answerAdapter;
    private Activity.Question.Answer selectedAnswer = null;

    private ImageView logo;

    private ImageView chatbot;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRetrofit();
        processArguments();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_lesson_worker, container, false);

        bundle = getArguments();

        btComeback = view.findViewById(R.id.btComeback2);
        tvAtividade = view.findViewById(R.id.tvAtividade);
        txtPergunta = view.findViewById(R.id.txtPergunta);
        recyclerAnswers = view.findViewById(R.id.recycler_answers);
        btContinuar = view.findViewById(R.id.btContinuar2);
        chatbot = view.findViewById(R.id.chatbot);
        logo = view.findViewById(R.id.icon_logo_boi2);

        answerAdapter = new AnswerAdapter(this);
        recyclerAnswers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerAnswers.setAdapter(answerAdapter);


        setupClickListeners(programId);

        // 4. Carrega Conteúdo
        if (classId != -1 && apiMongo != null) {
            fetchActivityByClassId(classId);
        } else {
            txtPergunta.setText("Erro ao carregar atividade. ID da aula ausente.");
            btContinuar.setEnabled(false);
        }

        btContinuar.setEnabled(false);

        return view;
    }

    private void initRetrofit() {
        apiMongo = RetrofitClientMongo
                .getInstance(requireContext())
                .create(ApiMongo.class);
    }

    private void processArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            classId = bundle.getInt("stepId", -1);
            programId = bundle.getInt("programId", -1);
            if (programId == -1) programId = null;

            progressValue = bundle.getDouble("activityProgressValue", 0.0);
            currentProgramProgress = bundle.getInt("currentProgramProgress", 0);
        }
    }

    private Integer getWorkerIdFromLocalStore() {
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int workerId = sp.getInt(KEY_WORKER_ID, -1);
        return workerId != -1 ? workerId : null;
    }

    private void fetchActivityByClassId(int id) {

        Log.d(TAG, "ClassId para a busca das activities: " + id);

        Call<List<Activity>> call = apiMongo.getActivityByClassId(id);

        call.enqueue(new Callback<List<Activity>>() {
            @Override
            public void onResponse(@NonNull Call<List<Activity>> call, @NonNull Response<List<Activity>> response) {
                // SUCESSO OU FALHA NA RESPOSTA HTTP
                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body();

                    // junta todas as questions dentro da activity 0
                    allQuestions = activities.stream()
                            .flatMap(activity -> activity.getQuestions().stream())
                            .collect(Collectors.toList());

                    // TRATAMENTO DO CONTEÚDO RECEBIDO
                    if (!allQuestions.isEmpty()) {
                        // CÁLCULO DO VALOR DE PROGRESSO POR PERGUNTA (Porcentagem)
                        if (progressValue > 0 && allQuestions.size() > 0) {
                            progressValue = progressValue / allQuestions.size();
                        } else {
                            progressValue = 0.0;
                        }

                        // NOVO CÁLCULO: PONTOS POR PERGUNTA (100 pontos totais)
                        pointsPerQuestion = 100.0 / allQuestions.size(); // Ex: 100 / 4 = 25.0

                        currentQuestionIndex = 0;
                        displayCurrentQuestion();
                    } else {
                        // Conteúdo vazio (Nenhuma pergunta)
                        txtPergunta.setText("Falha ao carregar atividade: Nenhuma pergunta encontrada.");
                        btContinuar.setEnabled(false);
                    }
                } else { // CORREÇÃO AQUI: O else pertence ao if(response.isSuccessful())
                    txtPergunta.setText("Falha ao carregar atividade: " + response.code());
                    btContinuar.setEnabled(false);
                }
            } // Fim do onResponse

            @Override
            public void onFailure(@NonNull Call<List<Activity>> call, @NonNull Throwable t) {
                txtPergunta.setText("Falha de conexão ao carregar atividade.");
                btContinuar.setEnabled(false);
            }
        });
    }



    private void displayCurrentQuestion() {
        if (allQuestions == null || allQuestions.isEmpty()) return;

        Activity.Question question = allQuestions.get(currentQuestionIndex);

        resetQuestionState();

        tvAtividade.setText("Pergunta " + (currentQuestionIndex + 1) + " de " + allQuestions.size());
        txtPergunta.setText(question.getQuestion());

        if (question.getAnswers() != null) {
            answerAdapter.submitList(question.getAnswers());
        } else {
            answerAdapter.submitList(null);
            txtPergunta.setText(txtPergunta.getText() + "\n(Sem alternativas disponíveis)");
        }

        // 4. Atualiza o texto do botão
        boolean isLastQuestion = currentQuestionIndex == allQuestions.size() - 1;
        btContinuar.setText(isLastQuestion ? "Finalizar Etapa" : "Próxima Pergunta");
    }

    private void resetQuestionState() {
        selectedAnswer = null;

        if (answerAdapter != null) {
            answerAdapter.setSelectedPosition(RecyclerView.NO_POSITION);
        }

        if (btContinuar != null) {
            btContinuar.setEnabled(false);
        }
    }



    private void saveNextStepAndNavigate() {
        ApiRedis apiRedis = RetrofitClientRedis
                .getInstance(requireContext())
                .create(ApiRedis.class);

        SharedPreferences p = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int workerId = p.getInt("user_id",  -1);
        int stepNumber = bundle.getInt("stepNumber", 1);

        StepRequest request = new StepRequest(workerId, programId, stepNumber + 1);
        Call<StepResponse> call = apiRedis.saveStep(request);

        call.enqueue(new Callback<StepResponse>() {
            @Override
            public void onResponse(Call<StepResponse> call, Response<StepResponse> response) {
                if (response.isSuccessful() && response.body() != null && getView() != null) {
                    StepResponse step = response.body();

                    if (step.getStatus() != null && step.getStatus() == 200) {

                        // LÓGICA DE PROGRESSÃO:
                        // O número de etapas concluídas é 'stepNumber' (a etapa que acabou de ser concluída).
                        int completedSteps = stepNumber;

                        Log.d(TAG, "Etapa " + stepNumber + " salva com sucesso no Redis. Progresso Final: " + currentProgramProgress + "%");

                        // CHAMA O POPUP ANTES DE NAVEGAR
                        showConclusionPopupAndNavigate(completedSteps);


                    } else {
                        Log.w(TAG, "Falha ao salvar etapa no Redis: " + step.getError());
                        Toast.makeText(getContext(), "Erro interno: Falha ao marcar a próxima etapa. Tente novamente.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Erro HTTP do Redis: " + response.code());
                    Toast.makeText(getContext(), "Erro de servidor (Redis): " + response.code() + ". Tente novamente.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<StepResponse> call, Throwable throwable) {
                Log.e(TAG, "Erro de conexão ao salvar etapa no Redis: " + throwable.getMessage());
                Toast.makeText(getContext(), "Erro de conexão ao finalizar. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }



    private void showConclusionPopupAndNavigate(int completedSteps) {
        if (getContext() == null || getView() == null) return;

        // 1. Inflar o layout customizado para o corpo do dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.card_conclusion_step, null); // ASSUMIR que você criou este layout!

        TextView tvMessage = dialogView.findViewById(R.id.tv_popup_message);
        ImageView ivIcon = dialogView.findViewById(R.id.iv_popup_icon);
        MaterialButton btFinish = dialogView.findViewById(R.id.bt_popup_finish);


        if (tvMessage != null) {
            tvMessage.setText(completedSteps + " etapas concluídas");
        }
        // Se você já tem o ícone no `drawable`, descomente a linha abaixo
        // if (ivIcon != null) {
        //     ivIcon.setImageResource(R.drawable.ic_conclusion_step);
        // }

        // 3. Criar o AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme); // Assumir um tema Material
        builder.setView(dialogView);
        builder.setCancelable(false); // Não permite fechar clicando fora

        Navigation.findNavController(getView()).navigate(R.id.HomePageWorker);

        AlertDialog dialog = builder.create();

        // 4. Configurar o botão para navegar ao fechar
        if (btFinish != null) {
            btFinish.setOnClickListener(v -> {
                dialog.dismiss();

                // NAVEGAÇÃO FINAL APÓS O POPUP
                Toast.makeText(getContext(), "Etapa concluída! Progresso Total: " + currentProgramProgress + "%", Toast.LENGTH_LONG).show();
            });
        }

        dialog.show();
    }





    private void setupClickListeners(Integer programId) {
        btComeback.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        logo.setOnClickListener(v->
                Navigation.findNavController(v).navigate(R.id.HomePageWorker));

        chatbot.setOnClickListener(v->
                Navigation.findNavController(v).navigate(R.id.ChatBotWorker));

        btContinuar.setOnClickListener(v -> {
            if (selectedAnswer == null) return;

            // Se a resposta for incorreta, não mudamos nada, apenas resetamos o estado.
            if (!selectedAnswer.isCorrect()) {
                Toast.makeText(getContext(), "Resposta incorreta. Tente novamente.", Toast.LENGTH_SHORT).show();
                resetQuestionState();
                return;
            }


            // Resposta correta: Inicia a lógica de salvamento encadeada

            final int percentageGain = (int) Math.round(progressValue); // Ganho em % do curso
            final int pointsGain = (int) Math.round(pointsPerQuestion); // Ganho em Pontos (25 no exemplo)

            Integer workerId = getWorkerIdFromLocalStore();

            if (workerId != null && programId != null) {

                // CHAMA 1: SALVAR PROGRESSO NO POSTGRES (Obrigatório antes de avançar)
                // Usando a NOVA ASSINATURA do ProgressApiHelper
                ProgressApiHelper.updateProgramProgress(
                        requireContext(),
                        programId,
                        percentageGain,
                        pointsGain,
                        currentProgramProgress, // NOVO ARGUMENTO: Progresso total atual
                        workerId,
                        new ProgressApiHelper.ProgressUpdateCallback() {
                            @Override
                            public void onProgressUpdated(int newPercentage) {
                                // SUCESSO POSTGRES: ATUALIZA ESTADO E DECIDE PRÓXIMO PASSO
                                currentProgramProgress = newPercentage; // newPercentage é o total do curso

                                if (currentQuestionIndex < allQuestions.size() - 1) {
                                    // AINDA TEM PERGUNTAS: AVANÇA
                                    currentQuestionIndex++;
                                    displayCurrentQuestion();
                                } else {

                                    saveNextStepAndNavigate();
                                }
                            }

                            @Override
                            public void onError(String message) {
                                // FALHA POSTGRES: ALERTA E MANTÉM NA PERGUNTA
                                Toast.makeText(getContext(), "Erro ao salvar progresso da atividade. Tente novamente.", Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        });

    }

    @Override
    public void onAnswerSelected(Activity.Question.Answer selectedAnswer, int position) {
        this.selectedAnswer = selectedAnswer;

        answerAdapter.setSelectedPosition(position);

        if (btContinuar != null) {
            btContinuar.setEnabled(true);
        }
    }
}