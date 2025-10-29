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

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.core.network.RetrofitClientMongo;
import com.example.core.network.RetrofitClientPostgres; // <-- IMPORTANTE: Cliente Postgres
import com.example.feature_produtor.adapter.StepsLessonAdapter;
import com.example.feature_produtor.api.ApiMongo;
import com.example.feature_produtor.api.ApiPostgres; // <-- Assumindo que você criou ApiPostgres
import com.example.feature_produtor.model.mongo.Class;
import com.example.feature_produtor.model.postegres.Program; // <-- IMPORTAÇÃO CORRETA do modelo Program
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;


import com.google.android.material.textfield.TextInputEditText;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StepsLessonWorker extends Fragment implements StepsLessonAdapter.OnStepClickListener {

    private static final String TAG = "StepsLessonWorker";

    private ImageView perfil;
    private TextInputEditText pesquisa;
    private ImageView config;
    private ImageView notificacao;
    private RecyclerView recyclerEtapas;
    private Button começar;
    private StepsLessonAdapter stepsLessonAdapter;

    private TextView descricao;

    private final List<Class> allLessons = new ArrayList<>();

    private Integer programId;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            programId = getArguments().getInt("programId", -1);

            if (programId == -1) {
                programId = null;
            }
        }

        if (programId == null && isAdded()) { // Usando isAdded() para segurança, embora seja onCreate
            Toast.makeText(getContext(), "Erro: ID do curso não encontrado.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "programId está nulo. Verifique o argumento 'programId' recebido.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steps_lesson_worker, container, false);

        // Mapeamento de Views
        perfil = view.findViewById(R.id.perfil);
        pesquisa = view.findViewById(R.id.txtPesquisa);
        config = view.findViewById(R.id.config);
        notificacao = view.findViewById(R.id.icon_notifi);
        recyclerEtapas = view.findViewById(R.id.recycler_etapas);
        começar = view.findViewById(R.id.btComeçar);
        descricao = view.findViewById(R.id.descricao);

        stepsLessonAdapter = new StepsLessonAdapter(this, requireContext());

        recyclerEtapas.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)); // Usando requireContext()
        recyclerEtapas.setAdapter(stepsLessonAdapter);

        if (programId != null) {
            fetchProgramDetails(programId);

            fetchClassesByProgramId(programId);
        }

        setupClickListeners();
        setupSearchListener();

        return view;
    }

    private void fetchProgramDetails(Integer id) {
        ApiPostgres apiPostgres = RetrofitClientPostgres // <-- Usando o cliente Postgres
                .getInstance(requireContext())
                .create(ApiPostgres.class);

        Call<Program> call = apiPostgres.getProgramById(id);

        call.enqueue(new Callback<Program>() {
            @Override
            public void onResponse(@NonNull Call<Program> call, @NonNull Response<Program> response) {
                if (!isAdded()) return; // <-- Segurança

                if (response.isSuccessful() && response.body() != null) {
                    Program program = response.body();

                    if (program.getDescription() != null) {
                        descricao.setText(program.getDescription());
                    } else {
                        descricao.setText("Descrição não disponível para este curso.");
                    }

                } else {
                    Log.e(TAG, "Falha ao buscar Program: Código " + response.code());
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Erro ao carregar descrição: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                    descricao.setText("Erro ao carregar descrição.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Program> call, @NonNull Throwable t) {
                if (!isAdded()) return; // <-- Segurança

                Log.e(TAG, "Erro de conexão ao buscar Program: " + t.getMessage(), t);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Erro de conexão (Descrição): " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
                descricao.setText("Falha de conexão.");
            }
        });
    }
    
    private void fetchClassesByProgramId(Integer id) {
        ApiMongo apiMongo = RetrofitClientMongo
                .getInstance(requireContext())
                .create(ApiMongo.class);


            Log.d(TAG, "Tentando buscar classes para programId: " + id);
            Call<List<Class>> call = apiMongo.getClassByProgramId(id);

            call.enqueue(new Callback<List<Class>>() {
                @Override
                public void onResponse(@NonNull Call<List<Class>> call, @NonNull Response<List<Class>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Class> classesByProgram = response.body();
                        Log.d(TAG, "Classes carregadas com sucesso. Total: " + classesByProgram.size());

                        stepsLessonAdapter.submitList(classesByProgram);
                    } else {
                        Log.e(TAG, "Falha ao buscar Classes: Código " + response.code());
                    }
                }

            @Override
            public void onFailure(@NonNull Call<List<Class>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Erro de conexão ao buscar Classes: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WorkerBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.LessonsWorker,
                    R.id.HomePageWorker,
                    R.id.GoalsPageWorker
            );
        }
    }


    private void setupClickListeners() {
        perfil.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.Profileworker));

        config.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Configurações clicadas", Toast.LENGTH_SHORT).show();
        });

        começar.setOnClickListener(v -> {
            if (!allLessons.isEmpty() && getView() != null) {
                Class firstLesson = allLessons.get(0);

                onStepClick(firstLesson);

            } else {
                Toast.makeText(getContext(), "Nenhuma etapa carregada para começar.", Toast.LENGTH_SHORT).show();
            }
        });

        notificacao.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notificações clicadas", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearchListener() {
        if (pesquisa != null) {
            pesquisa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLessons(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterLessons(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<Class> filteredList = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(allLessons);
        } else {
            for (Class lesson : allLessons) {
                if (lesson.getTitle() != null && lesson.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(lesson);
                }
            }
        }
        stepsLessonAdapter.submitList(filteredList);
    }

    @Override
    public void onStepClick(Class item) {
        Bundle bundle = new Bundle();
        Integer lessonId = item.getId();

        bundle.putInt("stepId", lessonId);

        if(getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.ContentLessonWorker, bundle);
        }
    }
}