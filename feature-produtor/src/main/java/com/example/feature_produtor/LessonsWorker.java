package com.example.feature_produtor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.feature_produtor.adapter.*;
import com.example.feature_produtor.api.*;
import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;
import com.google.android.material.textfield.TextInputEditText;

// IMPORTANTE: Use o RetrofitClient do módulo core
import com.example.core.network.RetrofitClientPostgres;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Necessário para a filtragem moderna

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonsWorker extends Fragment implements LessonsCardAdapter.OnLessonClickListener {

    private ImageView perfil;
    private TextInputEditText searchView;
    private ImageView notificacao;
    private ImageView config;
    private RecyclerView recyclerCursos;
    private LessonsCardAdapter lessonsCardAdapter;
    // Lista completa de programas carregados da API
    private final List<Program> allPrograms = new ArrayList<>();

    // Variável para armazenar o filtro recebido
    private String initialSegmentFilter = null;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lessons_worker, container, false);

        // 1. **VERIFICA O ARGUMENTO DE FILTRO ANTES DE TUDO**
        Bundle args = getArguments();
        if (args != null && args.containsKey("segment")) {
            initialSegmentFilter = args.getString("segment");
            // Opcional: Limpar o argumento para evitar que ele persista
            // args.remove("segment");
        }

        // 2. Configuração do BottomNav (sem mudanças)
        WorkerBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.LessonsWorker,
                    R.id.HomePageWorker,
                    R.id.GoalsPageWorker
            );
            bottom.setActive(WorkerBottomNavView.Item.LESSONS, false);
        }

        // 3. Mapeamento de Views e Configuração de RecyclerView (sem mudanças)
        perfil = view.findViewById(R.id.perfil3);
        searchView = view.findViewById(R.id.txtPesquisa3);
        notificacao = view.findViewById(R.id.icon_notifica);
        config = view.findViewById(R.id.imageView5);
        recyclerCursos = view.findViewById(R.id.cursos);


        lessonsCardAdapter = new LessonsCardAdapter(this, getContext());
        recyclerCursos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerCursos.setAdapter(lessonsCardAdapter);


        // 4. Chamada da API
        fetchPrograms();

        setupClickListeners();
        setupSearchListener();

        return view;
    }


    private void fetchPrograms() {
        ApiPostgres apiPostgres = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        apiPostgres.getAllPrograms().enqueue(new Callback<List<Program>>() {
            @Override
            public void onResponse(@NonNull Call<List<Program>> call, @NonNull Response<List<Program>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Program> programs = response.body();

                    // 1. Armazena a lista completa
                    allPrograms.clear();
                    allPrograms.addAll(programs);

                    // 2. **APLICA O FILTRO INICIAL SE EXISTIR**
                    applyInitialFilter();

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Program>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    private void applyInitialFilter() {
        if (initialSegmentFilter != null) {

            // 1. Limpa o nome do filtro: remove espaços em branco extras e padroniza para minúsculas
            final String filterNameCleaned = initialSegmentFilter.trim().toLowerCase();

            Toast.makeText(getContext(),
                    "Tentando filtrar por: " + initialSegmentFilter,
                    Toast.LENGTH_SHORT).show();

            // 2. Lógica de filtragem mais segura e robusta:
            List<Program> filteredList = new ArrayList<>();

            for (Program program : allPrograms) {
                // 3. Verificação de segurança (Null Check)
                if (program.getSegment() != null && program.getSegment().getName() != null) {

                    // 4. Limpa e padroniza o nome do segmento do programa
                    String programSegmentNameCleaned = program.getSegment().getName().trim().toLowerCase();

                    // 5. Compara os nomes limpos
                    if (programSegmentNameCleaned.equals(filterNameCleaned)) {
                        filteredList.add(program);
                    }
                }
            }

            // 6. Exibe o resultado e a lista
            if (filteredList.isEmpty()) {
                Toast.makeText(getContext(),
                        "Nenhum curso encontrado para o filtro: " + initialSegmentFilter,
                        Toast.LENGTH_LONG).show();
            }

            lessonsCardAdapter.submitList(filteredList);

            // Limpa o filtro inicial
            initialSegmentFilter = null;

        } else {
            // Se não houver filtro, exibe a lista completa
            lessonsCardAdapter.submitList(new ArrayList<>(allPrograms));
        }
    }

// ... (restante do código)


    // --- Métodos de Click Listeners, Busca e onLessonClick (sem mudanças relevantes) ---

    private void setupClickListeners() {
        perfil.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.Profileworker);
        });

        notificacao.setOnClickListener(v -> {
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.CardNotificacao);
            }
        });

        config.setOnClickListener(v -> {
            // Navegação futura
        });
    }

    private void setupSearchListener() {
        if (searchView != null) {
            searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Ao pesquisar, a filtragem de segmento é ignorada
                    filterPrograms(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void filterPrograms(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<Program> filteredList = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
            // Ao limpar a busca, exibe a lista completa novamente (allPrograms)
            filteredList.addAll(allPrograms);
        } else {
            for (Program program : allPrograms) {
                if ((program.getName() != null && program.getName().toLowerCase().contains(lowerCaseQuery)) ||
                        (program.getDescription() != null && program.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(program);
                }
            }
        }
        lessonsCardAdapter.submitList(filteredList);
    }

    @Override
    public void onLessonClick(Program item) {

        // 1. Declara e inicializa a variável de progresso
        int progress = item.getProgressPercentage();

        Bundle bundle = new Bundle();
        bundle.putInt("programId", item.getId());

        // 2. CORREÇÃO DE LÓGICA: Se a View for nula, saia.
        if (getView() == null) {
            // Opcional: Avisar o usuário se o fragmento não estiver pronto
            Toast.makeText(getContext(), "Erro de navegação: Tente novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Lógica Condicional: Se 100%, vai para FlashCardStudy; senão, continua a lição
        if (progress >= 100) {
            Toast.makeText(getContext(), "Revisando: " + item.getName(), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.FlashCardStudy, bundle);
        } else {
            NavHostFragment.findNavController(this).navigate(R.id.StepsLessonWorker, bundle);
        }
    }
}