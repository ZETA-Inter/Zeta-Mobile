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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.core.network.RetrofitClientPostgres;


import com.example.feature_produtor.adapter.LessonsCardAdapter;
import com.example.feature_produtor.api.ApiPostgres;

import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final List<Program> allPrograms = new ArrayList<>();
    private String initialSegmentFilter = null;

    private static final String TAG = "LessonsWorker";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lessons_worker, container, false);

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
//variavies
        perfil = view.findViewById(R.id.perfil3);
        searchView = view.findViewById(R.id.txtPesquisa3);
        notificacao = view.findViewById(R.id.icon_notifica);
        config = view.findViewById(R.id.imageView5);
        recyclerCursos = view.findViewById(R.id.cursos);

        //adapter
        lessonsCardAdapter = new LessonsCardAdapter(this, getContext());
        recyclerCursos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerCursos.setAdapter(lessonsCardAdapter);


        //chmada metodos
        fetchPrograms();
        setupClickListeners();
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
                    Toast.makeText(getContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Program>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
            }
        });
    }


    //aplicar o filtro que vem da tela  homepageworker
    private void applyInitialFilter() {
        if (initialSegmentFilter != null) {
            final String filterNameCleaned = initialSegmentFilter.trim().toLowerCase();
            Toast.makeText(getContext(),
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
                Toast.makeText(getContext(),
                        "Nenhum curso encontrado para o filtro: " + initialSegmentFilter,
                        Toast.LENGTH_LONG).show();
            }

            lessonsCardAdapter.submitList(filteredList);
            initialSegmentFilter = null;
        } else {
            lessonsCardAdapter.submitList(new ArrayList<>(allPrograms));
        }
    }


    private void setupSearchListener() {
        if (searchView != null) {
            searchView.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterPrograms(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterPrograms(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<Program> filteredList = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
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
    }
}