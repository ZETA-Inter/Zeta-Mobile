package com.example.feature_produtor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.example.core.client.ApiPostgresClient;
import com.example.core.model.mongo.FlashCard;
import com.example.core.network.RetrofitClientMongo;
import com.example.core.ui.CircularProgressView;
import com.example.core.dto.response.ProgramWorkerResponseDTO;
import com.example.core.network.RetrofitClientPostgres;

import com.example.feature_produtor.adapter.FilterAdapter;
import com.example.core.adapter.LessonsCardProgressAdapter;
import com.example.core.model.Segment;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.dto.response.ClassesDetaisResponseDTO;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;
import com.example.feature_produtor.api.ApiPostgres;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomePageWorker extends Fragment
        implements LessonsCardProgressAdapter.OnLessonClickListener,
        FilterAdapter.OnSegmentClickListener {

    private static final String TAG = "HomePageWorkerFragment";
    private ImageView perfil, boxIa, iconConfig, iconNotificacao;
    private RecyclerView recyclerTipoConteudo, recyclerCursosAndamento, recyclerCursosConcluidos;

    private CircularProgressView circularProgressGoals, circularProgressPrograms;
    private LessonsCardProgressAdapter andamentoLessonsAdapter;
    private LessonsCardProgressAdapter concludedLessonsAdapter;
    private FilterAdapter filterAdapter;

    private View loadingAndamentoLayout;

    private final List<ProgramWorkerResponseDTO> allPrograms = new ArrayList<>();
    private final List<ProgramWorkerResponseDTO> concludedPrograms = new ArrayList<>();

    private static final String PREF_NAME = "user_session";
    private static final String KEY_WORKER_ID = "user_id";

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
        setupCircularProgress();
        fetchData();
    }

    private void setupCircularProgress() {
        circularProgressGoals = requireView().findViewById(R.id.circularProgressGoals);
        circularProgressPrograms = requireView().findViewById(R.id.circularProgressPrograms);
        fetchProgress();
    }

    private void fetchProgress() {
        Integer workerId = getWorkerIdFromLocalStore();
        fetchGoalProgress(workerId);
        fetchProgramProgress(workerId);
    }

    private void fetchProgramProgress(Integer workerId) {
        ApiPostgresClient api = RetrofitClientPostgres.getApiService(requireContext());

        Call<Integer> call = api.findOverallProgramsProgressById(workerId);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressPrograms.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de cursos", t);
            }
        });
    }

    private void fetchGoalProgress(Integer workerId) {
        ApiPostgresClient api = RetrofitClientPostgres.getApiService(requireContext());

        Call<Integer> call = api.findOverallGoalsProgressById(workerId);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressGoals.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de metas", t);
            }
        });
    }

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

                    // Ajuste dentro de fetchPrograms -> onResponse
                    List<ProgramWorkerResponseDTO> inProgress = programs.stream()
                            .filter(p -> p.getProgressPercentage() < 100) // Qualquer coisa abaixo de 100%
                            .collect(Collectors.toList());

                    List<ProgramWorkerResponseDTO> completed = programs.stream()
                            .filter(p -> p.getProgressPercentage() >= 100 )
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
            // Concluído: Navega para a tela de flashcard (revisãooo)

            String cursoNome = item.getName();
            fetchProgramDetailsAndNavigate(programId, cursoNome);

//            if (flashcards != null && !flashcards.isEmpty()) {
//                // FlashCard PRECISA ser Parcelable
//                bundle.putParcelableArrayList("flashcards_list", new ArrayList<>(flashcards));
//                bundle.putString("curso_nome", cursoNome); // Passando o nome real
//            }

            //NavHostFragment.findNavController(this).navigate(R.id.FlashCardStudy, bundle);

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


    private void fetchProgramDetailsAndNavigate(int programId, String programName) {

        Toast.makeText(getContext(), "Carregando flashcards...", Toast.LENGTH_SHORT).show();

        // Use a API correta para buscar os detalhes completos
        ApiMongo apiMongo = RetrofitClientMongo
                .getInstance(requireContext())
                .create(ApiMongo.class);

        // Chama o endpoint COMPLETO usando o NOVO DTO
        // Lembre-se: 'getProgramDetailsById' deve ser definido na sua interface ApiPostgresClient
        apiMongo.getClassesDetailsById(programId).enqueue(new Callback<ClassesDetaisResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ClassesDetaisResponseDTO> call, @NonNull Response<ClassesDetaisResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {

                    ClassesDetaisResponseDTO programDetails = response.body();
                    // Acessa a lista de FlashCards usando o getter do DTO COMPLETO
                    List<FlashCard> flashcards = programDetails.getFlashcards();

                    // 1. Cria o Bundle com os dados REAIS
                    Bundle bundle = new Bundle();
                    bundle.putString("curso_nome", programName);

                    if (flashcards != null && !flashcards.isEmpty()) {
                        // FlashCard PRECISA ser Parcelable (Requisito CRÍTICO)
                        bundle.putParcelableArrayList("flashcards_list", new ArrayList<>(flashcards));

                        // 2. Navega com os dados
                        NavHostFragment.findNavController(HomePageWorker.this).navigate(R.id.FlashCardStudy, bundle);
                    } else {
                        Toast.makeText(getContext(), "Nenhum flashcard encontrado para este curso.", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Log.e(TAG, "Falha ao buscar detalhes do programa: " + response.code());
                    Toast.makeText(getContext(), "Erro ao carregar detalhes do curso: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            // CORREÇÃO: Adicionando o callback onFailure
            @Override
            public void onFailure(@NonNull Call<ClassesDetaisResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro de conexão ao buscar detalhes do programa", t);
                Toast.makeText(getContext(), "Erro de conexão. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
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

            bottom.bindNavController(nav, R.id.LessonsWorker, R.id.HomePageWorker, R.id.GoalsPageWorker);
            bottom.setActive(WorkerBottomNavView.Item.HOME, false);
        }
    }

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


        Uri deeplink = Uri.parse("app://Core/Profile");
        perfil.setOnClickListener(v -> nav.navigate(deeplink));
        boxIa.setOnClickListener(v -> nav.navigate(R.id.ChatBotWorker));

        iconConfig.setOnClickListener(v -> { nav.navigate(R.id.FlashCardStudy);});
        iconNotificacao.setOnClickListener(v -> nav.navigate(R.id.CardNotificacao));


    }

}