package com.example.feature_produtor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.feature_produtor.adapter.*;
import com.example.feature_produtor.api.*;
import com.example.feature_produtor.model.postegres.Program;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LessonsWorker extends Fragment implements LessonsCardAdapter.OnLessonClickListener {

    private ImageView perfil;
    private SearchView searchView;
    private ImageView notificacao;
    private ImageView config;
    private RecyclerView recyclerCursos;
    private LessonsCardAdapter lessonsCardAdapter;

    // Lista que armazena todos os cursos buscados da API
    private List<Program> allPrograms = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lessons_worker, container, false); // Assegure-se que o nome do layout está correto


        perfil = view.findViewById(R.id.perfil3);
        searchView = view.findViewById(R.id.txtPesquisa3);
        notificacao = view.findViewById(R.id.icon_notifica);
        config = view.findViewById(R.id.imageView5);
        recyclerCursos = view.findViewById(R.id.cursos);

        // 2. Inicialização do Adapter
        lessonsCardAdapter = new LessonsCardAdapter(this, getContext());

        // 3. Configuração do RecyclerView
        recyclerCursos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerCursos.setAdapter(lessonsCardAdapter);


        // 4. Iniciação do Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-postgresql-zeta-fide.onrender.com/") // Use a mesma URL base
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiPostgres apiPostgres = retrofit.create(ApiPostgres.class);


        // 5. Chamada para a API (Busca todos os Programs/Cursos)
        Call<List<Program>> call = apiPostgres.getAllPrograms();

        call.enqueue(new Callback<List<Program>>() {
            @Override
            public void onResponse(@NonNull Call<List<Program>> call, @NonNull Response<List<Program>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Program> programs = response.body();

                    // Armazena a lista completa na variável 'allPrograms' para a pesquisa
                    allPrograms.clear();
                    allPrograms.addAll(programs);

                    // Exibe a lista completa no RecyclerView
                    lessonsCardAdapter.submitList(new ArrayList<>(allPrograms));

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Program>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });


        setupClickListeners();
        setupSearchListener();

        return view;
    }


    private void setupClickListeners() {

        perfil.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.PerfilWorker);
        });


        notificacao.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notificações clicadas", Toast.LENGTH_SHORT).show();
        });

        config.setOnClickListener(v -> {
            //Navigation.findNavController(v).navigate(R.id.configDestino);
        });


    }

    private void setupSearchListener() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Opcional: filtro é feito em onQueryTextChange
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Chamamos o filtro sempre que o texto muda
                    filterPrograms(newText);
                    return true;
                }
            });
        }
    }

    // Lógica para filtrar a lista de cursos (Program)
    private void filterPrograms(String query) {
        // Converte a consulta para minúsculas para pesquisa sem distinção entre maiúsculas/minúsculas
        String lowerCaseQuery = query.toLowerCase();

        // Lista para os resultados filtrados
        List<Program> filteredList = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
            // Se a pesquisa estiver vazia, exibe a lista completa
            filteredList.addAll(allPrograms);
        } else {
            // Itera sobre a lista completa (allPrograms)
            for (Program program : allPrograms) {
                // Verifica se o nome ou descrição do programa contém o texto da pesquisa
                if ((program.getName() != null && program.getName().toLowerCase().contains(lowerCaseQuery)) ||
                        (program.getDescription() != null && program.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(program);
                }
            }
        }

        // Atualiza o RecyclerView com a lista filtrada
        lessonsCardAdapter.submitList(filteredList);
    }

    @Override
    public void onLessonClick(Program item) {
        // Lógica para quando um card de curso é clicado
        // Criar um Bundle para empacotar o id do Program (curso)
        Bundle bundle = new Bundle();

        // Adicionar o ID do curso (Program) ao Bundle.
        Integer programId = item.getId();
        bundle.putString("programId", String.valueOf(programId));

        // Navega para o destino, passando o Bundle como argumento
        // Assumindo que o destino é uma tela de detalhes do curso
        if(getView() != null) {
            // Substitua 'StepsLessonWorker' pelo destino correto para ver as etapas do curso
            Navigation.findNavController(getView()).navigate(R.id.ContentLessonWorker, bundle);
        }
    }
}