package com.example.feature_produtor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_produtor.adapter.FilterAdapter;
import com.example.feature_produtor.adapter.LessonsCardAdapter;
import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.model.postegres.Segment;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;
import com.example.feature_produtor.api.ApiPostgres;
// Importação do Diálogo (Mantenha se estiver no mesmo pacote, ou ajuste o caminho)
import com.example.feature_produtor.ResultCardDialogFragment;


import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomePageWorkerFragment extends Fragment
        implements LessonsCardAdapter.OnLessonClickListener,
        FilterAdapter.OnSegmentClickListener {

    private static final String TAG = "HomePageWorkerFragment";

    private ImageView perfil, boxIa, iconConfig, iconNotificacao;
    private RecyclerView recyclerTipoConteudo, recyclerCursosAndamento;
    private LessonsCardAdapter lessonsCardAdapter;
    private FilterAdapter filterAdapter;
    private ApiPostgres apiService;

    // --- CICLO DE VIDA E INICIALIZAÇÃO ---
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiService = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_page_worker, container, false);

        initViews(view);
        setupBottomNav(view);
        setupRecyclers();
        setupClickListeners();
        fetchData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ** PONTO CRUCIAL: Exibe o diálogo ao retornar da tela de FlashCard/Quiz **
        checkAndShowResultDialog();
    }

    // --- LÓGICA DO DIÁLOGO DE RESULTADOS ---
    private void checkAndShowResultDialog() {
        Bundle args = getArguments();

        // Verifica se há argumentos de resultados (acertos/erros)
        if (args != null && args.containsKey("acertos_count")) {

            Log.d(TAG, "Argumentos de resultado encontrados. Exibindo diálogo.");

            String cursoNome = args.getString("curso_nome", "Curso Finalizado");
            int acertos = args.getInt("acertos_count", 0);
            int erros = args.getInt("erros_count", 0);

            try {
                // Cria e exibe o diálogo (DESCOMENTADO E ATIVO)
                ResultCardDialogFragment dialog =
                        ResultCardDialogFragment.newInstance(cursoNome, acertos, erros);

                // Usa getChildFragmentManager() para DialogFragment dentro de um Fragmento
                dialog.show(getChildFragmentManager(), "ResultDialog");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao tentar mostrar ResultCardDialogFragment. Certifique-se que a classe existe e está importada.", e);
                Toast.makeText(getContext(), "Erro ao mostrar resultados: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Limpa os argumentos APÓS USAR. ESSENCIAL para que o diálogo não apareça
            // novamente em rotações ou retornos futuros sem novos resultados.
            getArguments().clear();
        } else {
            Log.d(TAG, "Nenhum argumento de resultado encontrado.");
        }
    }


    // --- MÉTODOS DE SETUP E LÓGICA ---

    private void initViews(View view) {
        perfil = view.findViewById(R.id.icon_perfil);
        boxIa = view.findViewById(R.id.boxIA);
        iconConfig = view.findViewById(R.id.icon_configuracoes);
        iconNotificacao = view.findViewById(R.id.icon_notificacao);
        recyclerCursosAndamento = view.findViewById(R.id.recycler_cursos_andamento);
        recyclerTipoConteudo = view.findViewById(R.id.recycler_tipo_conteudo);
    }

    private void setupBottomNav(View view) {
        WorkerBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.LessonsWorker,
                    R.id.HomePageWorker,
                    R.id.GoalsPageWorker
            );
            bottom.setActive(WorkerBottomNavView.Item.HOME, false);
        }
    }

    private void setupRecyclers() {
        lessonsCardAdapter = new LessonsCardAdapter(this, getContext());
        filterAdapter = new FilterAdapter(this);

        recyclerCursosAndamento.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerCursosAndamento.setAdapter(lessonsCardAdapter);

        recyclerTipoConteudo.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTipoConteudo.setAdapter(filterAdapter);
    }

    private void fetchData() {
        fetchPrograms();
        fetchSegments();
    }

    private void fetchPrograms() {
        apiService.getAllPrograms().enqueue(new Callback<List<Program>>() {
            @Override
            public void onResponse(@NonNull Call<List<Program>> call,
                                   @NonNull Response<List<Program>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Program> allPrograms = response.body();

                    List<Program> inProgressPrograms = allPrograms.stream()
                            .filter(p -> p.getProgressPercentage() < 100)
                            .collect(Collectors.toList());

                    // Opcional: List<Program> completedPrograms = allPrograms.stream()
                    //     .filter(p -> p.getProgressPercentage() >= 100)
                    //     .collect(Collectors.toList());

                    lessonsCardAdapter.submitList(inProgressPrograms);

                } else {
                    Log.e(TAG, "Erro ao carregar cursos: Código " + response.code());
                    Toast.makeText(getContext(),
                            "Erro ao carregar cursos: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Program>> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro de conexão (Cursos): " + t.getMessage(), t);
                Toast.makeText(getContext(),
                        "Erro de conexão (Cursos): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchSegments() {
        apiService.getAllSegments().enqueue(new Callback<List<Segment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Segment>> call,
                                   @NonNull Response<List<Segment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterAdapter.submitList(response.body());
                } else {
                    Log.e(TAG, "Erro ao carregar filtros: Código " + response.code());
                    Toast.makeText(getContext(),
                            "Erro ao carregar filtros: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Segment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro de conexão (Filtros): " + t.getMessage(), t);
                Toast.makeText(getContext(),
                        "Erro de conexão (Filtros): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        NavController nav = NavHostFragment.findNavController(this);

        perfil.setOnClickListener(v -> nav.navigate(R.id.Profileworker));
        boxIa.setOnClickListener(v -> nav.navigate(R.id.ChatBotPageWorker));
        iconConfig.setOnClickListener(v -> nav.navigate(R.id.FlashCardStudy));
        iconNotificacao.setOnClickListener(v -> nav.navigate(R.id.CardNotificacao));
    }

    // --- MÉTODOS DA INTERFACE (LISTENERS) ---

    @Override
    public void onLessonClick(Program item) {

        int progress = item.getProgressPercentage();

        Bundle bundle = new Bundle();
        bundle.putInt("programId", item.getId());

        if (getView() == null) {
            Toast.makeText(getContext(), "Erro de navegação. Fragmento não anexado.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progress >= 100) {
            Toast.makeText(getContext(), "Revisando: " + item.getName(), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.FlashCardStudy, bundle);
        } else {
            NavHostFragment.findNavController(this).navigate(R.id.StepsLessonWorker, bundle);
        }
    }

    @Override
    public void onSegmentClick(Segment nome) {
        Bundle bundle = new Bundle();
        bundle.putString("segment", nome.getName());

        Toast.makeText(getContext(), "Filtrando por: " + nome.getName(), Toast.LENGTH_SHORT).show();
        if (getView() != null) {
            NavHostFragment.findNavController(this).navigate(R.id.LessonsWorker, bundle);
        }
    }
}