package com.example.feature_produtor;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.ListActivity;
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

import com.example.core.network.RetrofitClientMongo;
import com.example.core.network.RetrofitClientPostgres;
import com.example.core.network.RetrofitClientRedis;
import com.example.feature_produtor.adapter.AnswerAdapter;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.api.ApiRedis;
import com.example.feature_produtor.api.ApiPostgres;
import com.example.feature_produtor.dto.request.ProgressUpdatePayload;

import com.example.feature_produtor.adapter.AnswerAdapter;
import com.example.feature_produtor.model.mongo.Activity;
import com.example.feature_produtor.model.mongo.Activity.Question;
import com.example.feature_produtor.model.mongo.Activity.Question.Answer;
import com.example.feature_produtor.model.redis.StepRequest;
import com.example.feature_produtor.model.redis.StepResponse;
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

    private List<Question> allQuestions;
    private int currentQuestionIndex = 0;

    private AnswerAdapter answerAdapter;
    private Answer selectedAnswer = null;

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

            progressValue = bundle.getDouble("progressValue", 0.0);
            currentProgramProgress = bundle.getInt("currentProgress", 0);
        }
    }

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

    private void fetchActivityByClassId(int id) {

        Log.d(TAG, "ClassId para a busca das activities: " + id);

        Call<List<Activity>> call = apiMongo.getActivityByClassId(id);

        call.enqueue(new Callback<List<Activity>>() {
            @Override
            public void onResponse(@NonNull Call<List<Activity>> call, @NonNull Response<List<Activity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body();

                    // junta todas as questions dentro da activity 0
                    allQuestions = activities.stream()
                            .flatMap(activity -> activity.getQuestions().stream())
                            .collect(Collectors.toList());

                    if (!allQuestions.isEmpty()) {
                        currentQuestionIndex = 0;
                        displayCurrentQuestion();
                    } else {
                        txtPergunta.setText("Nenhuma pergunta encontrada para esta atividade.");
                        btContinuar.setEnabled(false);
                    }

                } else {
                    txtPergunta.setText("Falha ao carregar atividade: " + response.code());
                    btContinuar.setEnabled(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Activity>> call, @NonNull Throwable t) {
                txtPergunta.setText("Falha de conexão ao carregar atividade.");
                btContinuar.setEnabled(false);
            }
        });
    }


    private void displayCurrentQuestion() {
        if (allQuestions == null || allQuestions.isEmpty()) return;

        Question question = allQuestions.get(currentQuestionIndex);

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

    private void setupClickListeners(Integer programId) {
        btComeback.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        btContinuar.setOnClickListener(v -> {
            if (selectedAnswer == null) return;

            if (selectedAnswer.isCorrect()) {
                if (currentQuestionIndex < allQuestions.size() - 1) {
                    currentQuestionIndex++;
                    displayCurrentQuestion();
                } else {
                    if (getView() != null) {
                        ApiRedis apiRedis = RetrofitClientRedis
                                .getInstance(requireContext())
                                .create(ApiRedis.class);

                        SharedPreferences p = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                        int workerId = p.getInt("user_id",  -1);
                        int stepNumber = bundle.getInt("stepNumber", 0);
                        Log.d(TAG, "Tentando salvar a etapa="+stepNumber+" do programId=" + programId +" e do workerId=" + workerId );

                        StepRequest request = new StepRequest(workerId, programId, stepNumber);
                        Call<StepResponse> call = apiRedis.saveStep(request);

                        call.enqueue(new Callback<StepResponse>() {
                            @Override
                            public void onResponse(Call<StepResponse> call, Response<StepResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    StepResponse step = response.body();

                                    if (step.getStatus() != null && step.getStatus() == 200) {
                                        Log.d(TAG, "Etapa " + stepNumber + " salva com sucesso");
                                    } else {
                                        Log.w(TAG, "Falha ao salvar etapa: " + step.getError());
                                    }
                                } else {
                                    Log.e(TAG, "Erro HTTP: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<StepResponse> call, Throwable throwable) {
                                Log.e(TAG, "Erro na requisição de salvamento da etapa no Redis: " + throwable.getMessage());
                            }
                        });

                        finalizeActivityAndApplyProgress();

                        Navigation.findNavController(getView()).navigate(R.id.HomePageWorker);
                    }
                }

            } else {
                Toast.makeText(getContext(), "Resposta incorreta. Tente novamente.", Toast.LENGTH_SHORT).show();
                resetQuestionState();
            }
        });
    }
    private void finalizeActivityAndApplyProgress() {
        if (programId == null || progressValue == 0.0) {
            Log.e(TAG, "Falha ao finalizar: programId é nulo ou progressValue é 0.");
            Toast.makeText(getContext(), "Erro ao finalizar a etapa (dados de progresso ausentes).", Toast.LENGTH_LONG).show();
            if (getView() != null)
                Navigation.findNavController(getView()).navigate(R.id.StepsLessonWorker);
            return;
        }

        // Soma o progresso atual com o valor restante da atividade (os 50% finais)
        int finalProgress = (int) Math.round(currentProgramProgress + progressValue);
        finalProgress = Math.min(finalProgress, 100);

        // Envia a atualização para a API
        updateProgramProgress(programId, finalProgress);

        // Feedback para o usuário
        if (finalProgress >= 100) {
            Toast.makeText(getContext(), "Curso concluído! Parabéns!", Toast.LENGTH_LONG).show();
            // Volta para a Home ou para a lista de cursos (dependendo do fluxo)
            if (getView() != null) Navigation.findNavController(getView()).navigate(R.id.HomePageWorker);
        } else {
            Toast.makeText(getContext(), "Etapa concluída! Progresso: " + finalProgress + "%", Toast.LENGTH_SHORT).show();
            // Volta para a lista de Etapas
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.StepsLessonWorker);
            }
        }

    }
    @Override
    public void onAnswerSelected(Answer selectedAnswer, int position) {
        this.selectedAnswer = selectedAnswer;

        answerAdapter.setSelectedPosition(position);

        if (btContinuar != null) {
            btContinuar.setEnabled(true);
        }
    }
}