package com.example.feature_produtor;

import android.os.Bundle;
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

import com.example.feature_produtor.model.mongo.Class; // Modelo de dados da API Mongo
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.TimeUnit;

import com.example.feature_produtor.api.ApiMongo; // Interface da API

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ContentLessonWorker extends Fragment {

    private ImageView btComeback;
    private TextView lessonTitleTextView;
    private TextView conteudo;
    private MaterialButton btContinuar;
    private ApiMongo apiMongo;
    private int currentStepId = -1; // ID da etapa/conteúdo atual

    // O token deve ser incluído no cabeçalho "Authorization: Bearer <seu-token>"
    // O OkHttpClient já adiciona o "Bearer" se você incluir o tipo no AUTH_TOKEN
    private final String AUTH_TOKEN = "Bearer TokenUserZeta1234";
    private final String BASE_URL_MONGO = "https://api-mongodb-fjnf.onrender.com/"; // Adicionado "/" no final

    public ContentLessonWorker() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Assegure-se que o nome do layout está correto
        View view = inflater.inflate(R.layout.fragment_content_lesson_worker, container, false);

        // 1. Inicializa UI
        btComeback = view.findViewById(R.id.btComeback);
        lessonTitleTextView = view.findViewById(R.id.textView2);
        conteudo = view.findViewById(R.id.tvConteudo);
        btContinuar = view.findViewById(R.id.btComeçar);

        // 2. Processa Argumentos
        processArguments();

        // 3. Configura Listeners
        setupClickListeners(view);

        // 4. Carrega Conteúdo se o ID for válido
        if (currentStepId != -1) {
            fetchLessonContent(currentStepId);
        } else {
            lessonTitleTextView.setText("Erro de ID");
            conteudo.setText("Não foi possível carregar a etapa. ID ausente.");
        }

        return view;
    }

    // --- Configuração e Inicialização ---

    private void initRetrofit() {
        // Configura o OkHttpClient com autenticação
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", AUTH_TOKEN)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_MONGO)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiMongo = retrofit.create(ApiMongo.class);
    }

    private void processArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Recebe o ID da etapa/passo como Integer
            currentStepId = bundle.getInt("stepId", -1);
        }
    }

    private void setupClickListeners(View view) {
        // BOTÃO VOLTAR (Seta)
        btComeback.setOnClickListener(v -> {
            // Volta para a tela StepsLessonWorker (Etapas do curso / Módulos)
            Navigation.findNavController(v).navigate(R.id.StepsLessonWorker);
        });

        // BOTÃO CONTINUAR (btComeçar no XML)
        btContinuar.setOnClickListener(v -> {
            // Passa o ID da classe/etapa (class_id) para a próxima tela (ActivityLessonWorker)
            Bundle continueBundle = new Bundle();
            continueBundle.putInt("stepId", currentStepId);

            // Navega para a tela ActivityLessonworker
            Navigation.findNavController(v).navigate(R.id.ActivityLessonWorker, continueBundle);
        });
    }

    // --- Comunicação com a API ---

    private void fetchLessonContent(int stepId) {

        Call<Class> call = apiMongo.getDescription(stepId);

        call.enqueue(new Callback<Class>() {
            @Override
            public void onResponse(@NonNull Call<Class> call, @NonNull Response<Class> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Class content = response.body();

                    // Atualiza o Título
                    if (content.getTitle() != null) {
                        lessonTitleTextView.setText(content.getTitle());
                    }

                    // CORRIGIDO: Usa getDescription() para o corpo do texto
                    if (content.getDescription() != null) {
                        conteudo.setText(content.getDescription());
                    }

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar conteúdo: " + response.code(), Toast.LENGTH_LONG).show();
                    conteudo.setText("Erro ao buscar o conteúdo da API.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Class> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                conteudo.setText("Falha na conexão com o servidor.");
            }
        });
    }
}