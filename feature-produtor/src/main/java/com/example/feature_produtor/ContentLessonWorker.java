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

import com.example.feature_produtor.model.mongo.Class;
import com.example.feature_produtor.api.ApiMongo;
import com.google.android.material.button.MaterialButton;
import com.example.core.network.RetrofitClientMongo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ContentLessonWorker extends Fragment {

    private ImageView btComeback;
    private TextView lessonTitleTextView;
    private TextView conteudo;
    private MaterialButton btContinuar;
    private ApiMongo apiMongo;

    private int currentStepId = -1;
    private Class currentLesson;

    // VARIÁVEIS DE PAGINAÇÃO
    private List<String> contentPages;
    private int currentPageIndex = 0;

    public ContentLessonWorker() {
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
        View view = inflater.inflate(R.layout.fragment_content_lesson_worker, container, false);

        // 1. Inicializa UI
        btComeback = view.findViewById(R.id.btComeback);
        lessonTitleTextView = view.findViewById(R.id.titulocontent);
        conteudo = view.findViewById(R.id.tvConteudo);
        btContinuar = view.findViewById(R.id.btComeçar);

        // 2. Configura Listeners
        setupClickListeners();

        // 3. Carrega Conteúdo se o ID for válido
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
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentStepId = bundle.getInt("stepId", -1);
        }
    }


    private void setupClickListeners() {
        btComeback.setOnClickListener(v -> {
            // Se estiver na primeira página, volta. Senão, volta uma página.
            if (currentPageIndex > 0 && contentPages != null && contentPages.size() > 1) {
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

            // Verifica se há MAIS PÁGINAS (itens da lista)
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
        // É crucial verificar se a aula foi carregada E se ela tem um ID válido.
        if (currentLesson != null && currentLesson.getId() != null) {

            Bundle continueBundle = new Bundle();
            // Usa o ID (class id) da aula carregada para passar para a próxima tela
            continueBundle.putInt("stepId", currentLesson.getId());

            if(getView() != null) {
                // Navega para a tela ActivityLessonWorker
                Navigation.findNavController(getView()).navigate(R.id.ActivityLessonWorker, continueBundle);
            }
        } else {
            // Se a aula não foi carregada corretamente, avisa o usuário
            Toast.makeText(getContext(), "Erro: ID da aula não disponível para continuar.", Toast.LENGTH_LONG).show();
        }
    }


    private void fetchLessonContent(int stepId) {
        Call<Class> call = apiMongo.getClassById(stepId);

        call.enqueue(new Callback<Class>() {
            @Override
            public void onResponse(@NonNull Call<Class> call, @NonNull Response<Class> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentLesson = response.body();

                    // 1. Atualiza o Título
                    lessonTitleTextView.setText(
                            currentLesson.getTitle() != null ? currentLesson.getTitle() : "Título não disponível");

                    // 2. Inicializa e exibe a primeira página
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

        // Prioriza a lista de conteúdo
        if (rawContentList != null) {
            for (String text : rawContentList) {
                if (text != null && !text.trim().isEmpty()) {
                    contentPages.add(text.trim());
                }
            }
        }

        // Se a lista de conteúdo estiver vazia, usa a descrição como única página
        if (contentPages.isEmpty() && descriptionFallback != null && !descriptionFallback.isEmpty()) {
            contentPages.add(descriptionFallback);
        }

        if (contentPages.isEmpty()) {
            conteudo.setText("Nenhum conteúdo principal disponível para esta aula.");
        } else {
            // Exibe a primeira página
            currentPageIndex = 0;
            displayCurrentPage();
        }
    }

    private void displayCurrentPage() {
        if (contentPages == null || contentPages.isEmpty() || currentPageIndex < 0 || currentPageIndex >= contentPages.size()) {
            conteudo.setText("Erro ao exibir a página.");
            return;
        }

        // Exibe o texto da página atual
        conteudo.setText(contentPages.get(currentPageIndex));

        // 1. Atualiza o texto do botão
        boolean isLastPage = currentPageIndex == contentPages.size() - 1;
        btContinuar.setText(isLastPage ? "Ir para Atividade" : "Próximo");

    }
}