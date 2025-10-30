package com.example.feature_produtor;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.example.feature_produtor.dto.response.ProgramWorkerResponseDTO;
import com.example.core.network.RetrofitClientPostgres;

import com.example.feature_produtor.adapter.FilterAdapter;
import com.example.feature_produtor.adapter.LessonsCardProgressAdapter;
import com.example.feature_produtor.model.postegres.Segment;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;
import com.example.feature_produtor.api.ApiPostgres;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomePageWorkerFragment extends Fragment
        implements LessonsCardProgressAdapter.OnLessonClickListener,
        FilterAdapter.OnSegmentClickListener {

    private static final String TAG = "HomePageWorkerFragment";
    private ImageView perfil, boxIa, iconConfig, iconNotificacao;
    private RecyclerView recyclerTipoConteudo, recyclerCursosAndamento, recyclerCursosConcluidos;
    private LessonsCardProgressAdapter andamentoLessonsAdapter;
    private LessonsCardProgressAdapter concludedLessonsAdapter;
    private FilterAdapter filterAdapter;

    private View loadingAndamentoLayout;

    private final List<ProgramWorkerResponseDTO> allPrograms = new ArrayList<>();
    private final List<ProgramWorkerResponseDTO> concludedPrograms = new ArrayList<>();

    private static final String PREF_NAME = "user_session";
    private static final String KEY_WORKER_ID = "worker_id";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_page_worker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //chamando os metodos
        checkAndShowResultDialog();
        initViews(view);
        setupBottomNav(view);
        setupRecyclers();
        setupClickListeners();
        fetchData();
    }


//pegar o workerid
    private Integer getWorkerIdFromLocalStore() {
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int workerId = sp.getInt(KEY_WORKER_ID, -1);
        return workerId != -1 ? workerId : null;
    }

    private void fetchData() {
        fetchPrograms();
        fetchSegments();
    }

    private void fetchPrograms() {
        Integer workerId = getWorkerIdFromLocalStore();

        if (workerId == null) {
            Toast.makeText(getContext(), "Erro: ID do Worker não encontrado.", Toast.LENGTH_LONG).show();
            return;
        }

        recyclerCursosAndamento.setVisibility(View.GONE);
        loadingAndamentoLayout.setVisibility(View.VISIBLE);

        ApiPostgres apiPostgres = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        //chama o endpoint que lista os programs por id do worker
        apiPostgres.listWorkerProgramsWithProgress(workerId).enqueue(new Callback<List<ProgramWorkerResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Response<List<ProgramWorkerResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadingAndamentoLayout.setVisibility(View.GONE);
                    recyclerCursosAndamento.setVisibility(View.VISIBLE);
                    List<ProgramWorkerResponseDTO> programs = response.body();

                    // Limpa as listas
                    allPrograms.clear();
                    concludedPrograms.clear();

                    // Filtra usando Streams para clareza
                    List<ProgramWorkerResponseDTO> inProgress = programs.stream()
                            .filter(p -> p.getProgressPercentage() > 0 && p.getProgressPercentage() < 100)
                            .collect(Collectors.toList());

                    List<ProgramWorkerResponseDTO> completed = programs.stream()
                            .filter(p -> p.getProgressPercentage() >= 100)
                            .collect(Collectors.toList());

                    allPrograms.addAll(inProgress);
                    concludedPrograms.addAll(completed);

                    // Submete às Recyclers
                    andamentoLessonsAdapter.submitList(new ArrayList<>(allPrograms));
                    concludedLessonsAdapter.submitList(new ArrayList<>(concludedPrograms));


                }


                else {
                    loadingAndamentoLayout.setVisibility(View.VISIBLE);
                    recyclerCursosAndamento.setVisibility(View.GONE);
                    Log.e(TAG, "Falha ao carregar programas. CODE: " + response.code() + " URL: " + call.request().url());
                 //   Toast.makeText(getContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                    // se o erro for 404 td bem pq o worker pode simplismente não ter iniciado nenhum curso ainda
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Throwable t) {
                loadingAndamentoLayout.setVisibility(View.VISIBLE);
                recyclerCursosAndamento.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
            }
        });
    }

    private void fetchSegments() {
        //lista os segments (filtros)

        ApiPostgres apiPostgres = RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        apiPostgres.getAllSegments().enqueue(new Callback<List<Segment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Segment>> call, @NonNull Response<List<Segment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterAdapter.submitList(response.body());
                } else {
                    Log.e(TAG, "Erro ao carregar filtros: Código " + response.code());
                    Toast.makeText(getContext(), "Erro ao carregar filtros: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Segment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro de conexão (Filtros): " + t.getMessage(), t);
                Toast.makeText(getContext(), "Erro de conexão (Filtros): " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

   // click do curso que passa o progresso o program id para etapas
    @Override
    public void onLessonClick(ProgramWorkerResponseDTO item) {
        int programId = item.getId();
        int currentProgress = item.getProgressPercentage();

        Bundle bundle = new Bundle();
        bundle.putInt("programId", programId);
        bundle.putInt("currentProgress", currentProgress);

        if (getView() == null) return;

        if (currentProgress >= 100) {
            // Concluído: Navega para a tela de flashcard
            Toast.makeText(getContext(), "Revisando: " + item.getName(), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.FlashCardStudy, bundle);
        } else {
            // Em Andamento: Navega para a tela de etapas
            NavHostFragment.findNavController(this).navigate(R.id.StepsLessonWorker, bundle);
        }
    }

    // click do filtro (vai para cursos filtrado)
    @Override
    public void onSegmentClick(Segment segment) {
        Bundle bundle = new Bundle();
        bundle.putString("segment", segment.getName());

        Toast.makeText(getContext(), "Filtrando por: " + segment.getName(), Toast.LENGTH_SHORT).show();
        if (getView() != null) {
            // Navega para a tela LessonsWorker com o filtro de segmento
            NavHostFragment.findNavController(this).navigate(R.id.LessonsWorker, bundle);
        }
    }



    // quando os flahs cards são concluidos abre esse dialog
    private void checkAndShowResultDialog() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("acertos_count")) {
            Log.d(TAG, "Argumentos de resultado encontrados. Exibindo diálogo.");

            String cursoNome = args.getString("curso_nome", "Curso Finalizado");
            int acertos = args.getInt("acertos_count", 0);
            int erros = args.getInt("erros_count", 0);

            try {
                ResultCardDialogFragment dialog = ResultCardDialogFragment.newInstance(cursoNome, acertos, erros);
                dialog.show(getChildFragmentManager(), "ResultDialog");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao tentar mostrar ResultCardDialogFragment.", e);
                Toast.makeText(getContext(), "Erro ao mostrar resultados: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            // Limpar os argumentos para evitar que o diálogo reapareça
            getArguments().clear();
        } else {
            Log.d(TAG, "Nenhum argumento de resultado encontrado.");
        }
    }

    //iniciando variavis
    private void initViews(View view) {
        perfil = view.findViewById(R.id.icon_perfil);
        boxIa = view.findViewById(R.id.boxIA);
        iconConfig = view.findViewById(R.id.icon_configuracoes);
        iconNotificacao = view.findViewById(R.id.icon_notificacao);

        recyclerCursosAndamento = view.findViewById(R.id.recycler_cursos_andamento);
        recyclerTipoConteudo = view.findViewById(R.id.recycler_tipo_conteudo);
        recyclerCursosConcluidos = view.findViewById(R.id.recycler_cursos_concluidos);
        loadingAndamentoLayout = view.findViewById(R.id.layout_cursos_andamento_loading);
    }

    // nav bar
    private void setupBottomNav(View view) {
        WorkerBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);

            bottom.bindNavController(nav, R.id.HomePageWorker, R.id.LessonsWorker, R.id.GoalsPageWorker);
            bottom.setActive(WorkerBottomNavView.Item.HOME, false);
        }
    }

    //iniciando os recilers
    private void setupRecyclers() {

        andamentoLessonsAdapter = new LessonsCardProgressAdapter(this, getContext());
        concludedLessonsAdapter = new LessonsCardProgressAdapter(this, getContext());
        filterAdapter = new FilterAdapter(this);

        // RecyclerView de Cursos em Andamento
        recyclerCursosAndamento.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerCursosAndamento.setAdapter(andamentoLessonsAdapter);

        // RecyclerView de Tipos de Conteúdo (Segmentos)
        recyclerTipoConteudo.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTipoConteudo.setAdapter(filterAdapter);

        // RecyclerView de Cursos Concluídos
        recyclerCursosConcluidos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerCursosConcluidos.setAdapter(concludedLessonsAdapter);
    }

    // clicks da nav bar superior
    private void setupClickListeners() {
        NavController nav = NavHostFragment.findNavController(this);

        perfil.setOnClickListener(v -> nav.navigate(R.id.Profileworker));
        boxIa.setOnClickListener(v -> nav.navigate(R.id.ChatBotPageWorker));
        iconConfig.setOnClickListener(v -> { });
        iconNotificacao.setOnClickListener(v -> nav.navigate(R.id.CardNotificacao));


    }

}