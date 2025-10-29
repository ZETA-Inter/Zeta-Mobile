package com.example.feature_produtor;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.network.RetrofitClientMongo;
import com.example.feature_produtor.adapter.AnswerAdapter;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.model.mongo.Activity;
import com.example.feature_produtor.model.mongo.Activity.Question;
import com.example.feature_produtor.model.mongo.Activity.Question.Answer;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLessonWorker extends Fragment implements AnswerAdapter.OnAnswerSelectedListener {

    private ImageView btComeback;
    private TextView tvAtividade;
    private TextView txtPergunta;
    private RecyclerView recyclerAnswers;
    private MaterialButton btContinuar;

    private ApiMongo apiMongo;
    private int classId = -1;
    private List<Question> allQuestions;
    private int currentQuestionIndex = 0;

    private AnswerAdapter answerAdapter;
    private Answer selectedAnswer = null;

    public ActivityLessonWorker() {
    }

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

        btComeback = view.findViewById(R.id.btComeback2);
        tvAtividade = view.findViewById(R.id.tvAtividade);
        txtPergunta = view.findViewById(R.id.txtPergunta);
        recyclerAnswers = view.findViewById(R.id.recycler_answers);
        btContinuar = view.findViewById(R.id.btContinuar2);

        answerAdapter = new AnswerAdapter(this);
        recyclerAnswers.setAdapter(answerAdapter);

        setupClickListeners(view);

        // 4. Carrega Conteúdo
        if (classId != -1 && apiMongo != null) {
            fetchActivityByClassId(classId);
        } else {
            // Exibir erro na TextView, sem Toast
            txtPergunta.setText("Erro ao carregar atividade. ID da aula ausente.");
            btContinuar.setEnabled(false);
        }

        // NOVO: Inicialmente, o botão está desabilitado até que uma resposta seja selecionada
        btContinuar.setEnabled(false);

        return view;
    }

    // --- Inicialização ---

    private void initRetrofit() {
        // Usando requireContext() aqui é seguro, pois está no onCreate
        apiMongo = RetrofitClientMongo
                .getInstance(requireContext())
                .create(ApiMongo.class);
    }

    private void processArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Recebe o ID da Class (Aula)
            classId = bundle.getInt("stepId", -1);
        }
    }


    private void fetchActivityByClassId(int id) {
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
                        // O botão é habilitado no displayCurrentQuestion() ou onAnswerSelected()
                        displayCurrentQuestion();
                    } else {
                        txtPergunta.setText("Nenhuma pergunta encontrada para esta atividade.");
                        btContinuar.setEnabled(false);
                    }

                } else {
                    btContinuar.setEnabled(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Activity>> call, @NonNull Throwable t) {
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

        boolean isLastQuestion = currentQuestionIndex == allQuestions.size() - 1;
        btContinuar.setText(isLastQuestion ? "Finalizar Atividade" : "Próxima Pergunta");
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

    private void setupClickListeners(View view) {
        btComeback.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        btContinuar.setOnClickListener(v -> {
            if (selectedAnswer == null) {
                return;
            }

            Question currentQuestion = allQuestions.get(currentQuestionIndex);

            if (selectedAnswer.isCorrect()) {
                if (currentQuestionIndex < allQuestions.size() - 1) {
                    currentQuestionIndex++;
                    displayCurrentQuestion();
                } else {
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigate(R.id.HomePageWorker);
                    }
                }

            } else {
                txtPergunta.setText(" Resposta Incorreta. Tente novamente: " + currentQuestion.getQuestion());

                resetQuestionState();
            }
        });
    }


    @Override
    public void onAnswerSelected(Answer selectedAnswer, int position) {
        this.selectedAnswer = selectedAnswer;

        // Notifica o adapter para garantir que apenas o item correto esteja selecionado
        answerAdapter.setSelectedPosition(position);

        // NOVO: Habilita o botão "Continuar" assim que uma resposta for selecionada
        if (btContinuar != null) {
            btContinuar.setEnabled(true);
        }
    }
}