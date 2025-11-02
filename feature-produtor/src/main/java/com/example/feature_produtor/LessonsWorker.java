package com.example.feature_produtor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.core.network.RetrofitClientIA;
import com.example.core.network.RetrofitClientPostgres;


import com.example.feature_produtor.adapter.LessonsCardAdapter;
import com.example.feature_produtor.api.ApiIA;
import com.example.feature_produtor.api.ApiPostgres;

import com.example.feature_produtor.dto.request.SearchRequest;
import com.example.feature_produtor.dto.response.SearchResponse;
//import com.example.feature_produtor.model.postegres.Program; centralização feita no core

import com.example.core.adapter.LessonsCardAdapter;
import com.example.feature_produtor.api.ApiPostgres;

import com.example.core.model.Program;

import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// NOVOS IMPORTS para Debounce
import android.os.Handler;
import android.os.Looper;


public class LessonsWorker extends Fragment implements LessonsCardAdapter.OnLessonClickListener {

    private ImageView perfil;
    private TextInputEditText searchView;
    private ImageView notificacao;
    private ImageView config;
    private RecyclerView recyclerCursos;

    private LessonsCardAdapter lessonsCardAdapter;

    private final List<Program> allPrograms = new ArrayList<>();
    private String initialSegmentFilter = null;

    private static final String TAG = "LessonsWorker";

    private ApiIA apiSearchAgent;
    private Call<SearchResponse> currentSearchCall;

    // Variável para o diálogo de carregamento
    private AlertDialog processingDialog;

    // NOVO: Gerenciadores para o Debounce
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private final long SEARCH_DELAY_MS = 800; // 0.8 segundos de atraso

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lessons_worker, container, false);

        // --- Configuração da Navegação e Filtro Inicial ---
        Bundle args = getArguments();
        if (args != null && args.containsKey("segment")) {
            initialSegmentFilter = args.getString("segment");
        }

        WorkerBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(nav, R.id.LessonsWorker, R.id.HomePageWorker, R.id.GoalsPageWorker);
            bottom.setActive(WorkerBottomNavView.Item.LESSONS, true);
        }

        // --- Inicialização de Views ---
        perfil = view.findViewById(R.id.perfil3);
        searchView = view.findViewById(R.id.txtPesquisa3);
        notificacao = view.findViewById(R.id.icon_notifica);
        config = view.findViewById(R.id.imageView5);
        recyclerCursos = view.findViewById(R.id.cursos);

        // --- Configuração do Adapter ---
        lessonsCardAdapter = new LessonsCardAdapter(this, getContext());
        recyclerCursos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerCursos.setAdapter(lessonsCardAdapter);

        // --- Inicialização do Serviço de Pesquisa da IA ---
        apiSearchAgent = RetrofitClientIA
                .getInstance(requireContext())
                .create(ApiIA.class);

        // --- Chamada de Métodos ---
        fetchPrograms();
        setupClickListeners();

        // NOVO: Ativa o listener de Debounce
        setupSearchListener();

        return view;
    }


    private void fetchPrograms() {
        // Esta rota é para buscar o catálogo completo de Programas
        ApiPostgres apiPostgres = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        //listar todos os cursos
        apiPostgres.getAllPrograms().enqueue(new Callback<List<Program>>() {
            @Override
            public void onResponse(@NonNull Call<List<Program>> call, @NonNull Response<List<Program>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Program> programs = response.body();

                    allPrograms.clear();
                    allPrograms.addAll(programs);

                    applyInitialFilter();

                } else {
                    Log.e(TAG, "Cursos Load FAILED. Code: " + response.code() + " URL: " + call.request().url());
                    Toast.makeText(requireContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Program>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
            }
        });
    }


    //aplicar o filtro que vem da tela  homepageworker
    private void applyInitialFilter() {
        if (initialSegmentFilter != null) {
            final String filterNameCleaned = initialSegmentFilter.trim().toLowerCase();
            Toast.makeText(requireContext(),
                    "Filtrando por: " + initialSegmentFilter,
                    Toast.LENGTH_SHORT).show();

            List<Program> filteredList = new ArrayList<>();

            for (Program program : allPrograms) {
                if (program.getSegment() != null && program.getSegment().getName() != null) {
                    String programSegmentNameCleaned = program.getSegment().getName().trim().toLowerCase();
                    if (programSegmentNameCleaned.equals(filterNameCleaned)) {
                        filteredList.add(program);
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Nenhum curso encontrado para o filtro: " + initialSegmentFilter,
                        Toast.LENGTH_LONG).show();
            }

            lessonsCardAdapter.submitList(filteredList);
            initialSegmentFilter = null;
        } else {
            lessonsCardAdapter.submitList(new ArrayList<>(allPrograms));
        }
    }

    /**
     * Listener com Debounce para dar tempo ao usuário para digitar.
     */
    private void setupSearchListener() {
        if (searchView != null) {
            searchView.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 1. Remove qualquer pesquisa pendente (reset do debounce)
                    if (searchRunnable != null) {
                        handler.removeCallbacks(searchRunnable);
                    }

                    final String query = s.toString().trim();

                    if (query.isEmpty()) {
                        // 2. LIMPEZA: Volta ao estado normal imediatamente
                        dismissProcessingDialog();
                        if (currentSearchCall != null) {
                            currentSearchCall.cancel();
                        }
                        lessonsCardAdapter.submitList(new ArrayList<>(allPrograms));
                        return;
                    }

                    // 3. Cria um novo Runnable que irá acionar a pesquisa
                    searchRunnable = () -> {
                        // A pesquisa só é disparada se o usuário parou de digitar por 800ms
                        searchProgramsWithIA(query);
                    };

                    // 4. Inicia o atraso de 800ms
                    handler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }


    //pesquisa com ia
    private void searchProgramsWithIA(String query) {
        // 1. Limpa a pesquisa anterior
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
        }

        // 2. MOSTRA O DIÁLOGO DE PROCESSAMENTO
        showProcessingDialog(query); // Passa a query para exibição

        // 3. Faz a chamada à API da IA
        SearchRequest request = new SearchRequest(query);
        currentSearchCall = apiSearchAgent.searchPrograms(request);

        currentSearchCall.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchResponse> call, @NonNull Response<SearchResponse> response) {
                // 4. FECHA O DIÁLOGO DE PROCESSAMENTO, independente do resultado
                dismissProcessingDialog();

                if (response.isSuccessful() && response.body() != null) {
                    String iaResponse = response.body().getResponseText();

                    // AÇÃO CHAVE: Exibe o resultado da IA no pop-up
                    showIaResponsePopup(iaResponse, query);

                } else {
                    Log.e(TAG, "Search IA FAILED. Code: " + response.code());
                    Toast.makeText(requireContext(), "Erro na pesquisa (IA): " + response.code(), Toast.LENGTH_LONG).show();
                }
                currentSearchCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<SearchResponse> call, @NonNull Throwable t) {
                // 4. FECHA O DIÁLOGO DE PROCESSAMENTO
                if (call.isCanceled()) {
                    Log.d(TAG, "Search IA: Request cancelado.");
                    return;
                }
                dismissProcessingDialog();

                Toast.makeText(requireContext(), "Erro de conexão (IA): " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de rede (IA): " + t.getMessage());
                currentSearchCall = null;
            }
        });

        lessonsCardAdapter.submitList(new ArrayList<>(allPrograms));
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS DE CONTROLE DE DIÁLOGOS
    // ----------------------------------------------------------------------------------

    /**
     * Cria e mostra o diálogo de "Processando...", bloqueando o conteúdo subjacente.
     * @param query A consulta atual para exibir no diálogo.
     */
    private void showProcessingDialog(String query) {
        // Se o diálogo já estiver aberto, apenas atualiza o texto se necessário, evita recriar.
        if (processingDialog != null && processingDialog.isShowing()) {
            TextView messageView = processingDialog.findViewById(R.id.loading_message);
            if (messageView != null) {
                messageView.setText("Pesquisando por: " + query + "...");
            }
            return;
        }

        // Criação do Diálogo (Se for a primeira vez ou se foi dispensado)
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Infla um layout simples para o carregamento
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Assume: R.layout.dialog_loading_simple existe e tem R.id.loading_message
        View dialogView = inflater.inflate(R.layout.dialog_loading_simple, null);
        TextView messageView = dialogView.findViewById(R.id.loading_message);
        if (messageView != null) {
            messageView.setText("Pesquisando por: " + query + "...");
        }

        builder.setView(dialogView);
        builder.setCancelable(true); // Permite fechar com o botão voltar

        // Define o comportamento ao cancelar (apertar Back)
        builder.setOnCancelListener(dialog -> {
            // Cancela a chamada API e o debounce
            if (currentSearchCall != null) {
                currentSearchCall.cancel();
            }
            if (searchRunnable != null) {
                handler.removeCallbacks(searchRunnable);
            }
            // Limpa o texto da busca para forçar o retorno ao estado normal
            searchView.setText("");
        });

        processingDialog = builder.create();

        // Opcional: Ajusta a janela do diálogo para ocupar mais tela
        if (processingDialog.getWindow() != null) {
            // processingDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        processingDialog.show();
    }

    /**
     * Fecha o diálogo de "Processando...".
     */
    private void dismissProcessingDialog() {
        if (processingDialog != null && processingDialog.isShowing()) {
            processingDialog.dismiss();
            processingDialog = null; // Reseta para que seja recriado na próxima busca
        }
    }

    /**
     * Cria e exibe o pop-up (AlertDialog) com a resposta da IA.
     */
    private void showIaResponsePopup(String iaResponse, String query) {
        if (requireContext() == null) return;

        // 1. Configura o construtor do Diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // 2. Infla o layout customizado para o pop-up
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Assume que 'dialog_ia_response.xml' existe e está no res/layout
        View dialogView = inflater.inflate(R.layout.dialog_ia_response, null);
        builder.setView(dialogView);

        // 3. Obtém as views do layout customizado
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        Button closeButton = dialogView.findViewById(R.id.dialog_close_button);

        // 4. Preenche o conteúdo
        titleTextView.setText("Resultado da Pesquisa: '" + query + "'");

        // Remove a formatação MarkDown da resposta da IA
        String cleanResponse = iaResponse
                .replace("**", "")
                .replace("\n\n", "\n")
                .trim();

        messageTextView.setText(cleanResponse);

        // 5. Cria e exibe o Diálogo
        final AlertDialog dialog = builder.create();

        // 6. Configura o botão de fechar
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Opcional: Define que o pop-up pode ser fechado clicando fora.
        dialog.setCanceledOnTouchOutside(true);

        // 7. Exibe o pop-up
        dialog.show();
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS DE NAVEGAÇÃO E CICLO DE VIDA
    // ----------------------------------------------------------------------------------

    //click no curso para o program id para poder pegar as etapas corretas (na classe class)
    @Override
    public void onLessonClick(Program item) {
        int programId = item.getId();

        Bundle bundle = new Bundle();
        bundle.putInt("programId", programId);

        if (getView() == null) return;


        NavHostFragment.findNavController(this).navigate(R.id.StepsLessonWorker, bundle);
    }

    private void setupClickListeners() {
        perfil.setOnClickListener(v -> Navigation.findNavController(v).navigate(com.example.core.R.id.Profile));
        notificacao.setOnClickListener(v -> {
            if (getView() != null) Navigation.findNavController(getView()).navigate(R.id.CardNotificacao);
        });
        config.setOnClickListener(v -> {
            // Implementar navegação
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Garante que o Runnable pendente seja removido (limpeza de memória)
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        // Garante que a chamada de pesquisa e o diálogo sejam cancelados ao sair da tela
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
        }
        dismissProcessingDialog();
    }
}