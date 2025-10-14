package com.example.feature_produtor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.feature_produtor.adapter.*;
import com.example.feature_produtor.api.*;
import com.example.feature_produtor.model.postegres.Segment;
import com.google.android.material.textfield.TextInputEditText;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class StepsLessonWorker extends Fragment implements StepsLessonAdapter.OnStepClickListener {

    private ImageView perfil;
    private TextInputEditText pesquisa;
    private ImageView config;
    private ImageView notificacao;
    private RecyclerView recyclerEtapas;
    private Button começar;
    private StepsLessonAdapter stepsLessonAdapter;

    private List<Segment> allLessons = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steps_lesson_worker, container, false);

        perfil = view.findViewById(R.id.perfil);
        pesquisa = view.findViewById(R.id.txtPesquisa);
        config = view.findViewById(R.id.config);
        notificacao = view.findViewById(R.id.icon_notifi);
        recyclerEtapas = view.findViewById(R.id.recycler_etapas);
        começar = view.findViewById(R.id.btComeçar);

        //iniciando o adapter
        stepsLessonAdapter = new StepsLessonAdapter(this,getContext());

        //configurando RecyclerView
        recyclerEtapas.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false));
        recyclerEtapas.setAdapter(stepsLessonAdapter);


        //Iniciando Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-postgresql-zeta-fide.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiPostgres apiPostgres = retrofit.create(ApiPostgres.class);


        // Fazendo chamada para a api (recycler etapas curso)
        Call<List<Segment>> call = apiPostgres.getAllSegments();

        call.enqueue(new Callback<List<Segment>>() {
            @Override
            public void onResponse(Call<List<Segment>> call, Response<List<Segment>> response) { // Mude aqui também
                if (response.isSuccessful() && response.body() != null) {

                    List<Segment> steps = response.body(); // Mude aqui também

                    // Armazena a lista completa na variável 'allLessons' para a pesquisa
                    allLessons.clear();
                    allLessons.addAll(steps);

                    // Exibe a lista completa no RecyclerView
                    stepsLessonAdapter.submitList(new ArrayList<>(allLessons));

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar etapas: " + response.code(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<List<Segment>> call, Throwable t) { // Mude aqui também
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });


        setupClickListeners();
        setupSearchListener();

        return view;
    }

    private void setupClickListeners() {
        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.PerfilWorker);
            }
        });

        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigation.findNavController(v).navigate(R.id.);

            }
        });

        começar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigation.findNavController(v).navigate(R.id.);

            }
        });

        notificacao.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notificações clicadas", Toast.LENGTH_SHORT).show();
        });
    }

    //LOgica para pesquisar
    private void setupSearchListener() {
        if (pesquisa != null) {
            pesquisa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Chamamos o filtro sempre que o texto muda
                    filterLessons(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    //Lógica para filtrar a lista de lições
    private void filterLessons(String query) {
        // Converte a consulta para minúsculas para pesquisa sem distinção entre maiúsculas/minúsculas
        String lowerCaseQuery = query.toLowerCase();

        // Lista para os resultados filtrados
        List<Segment> filteredList = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
            // Se a pesquisa estiver vazia, exibe a lista completa
            filteredList.addAll(allLessons);
        } else {
            // Itera sobre a lista completa (allLessons)
            for (Segment segment : allLessons) {
                // Verifica se o nome do segmento (em minúsculas) contém o texto da pesquisa
                if (segment.getName() != null && segment.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(segment);
                }
            }
        }

        // Atualiza o RecyclerView com a lista filtrada
        stepsLessonAdapter.submitList(filteredList);
    }



    @Override
    public void onStepClick(Segment item) {
        // Criar um Bundle para empacotar o id
        Bundle bundle = new Bundle();

        //Adicionar infos do curso ao Bundle.
        Integer lessonId = item.getId();
        bundle.putString("stepId", String.valueOf(lessonId));

        //Navega para o destino, passando o Bundle como argumento
        // É importante garantir que getView() não seja null aqui, mas para um Fragment em onStepClick, geralmente está ok.
        if(getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.ContentLessonWorker, bundle);
        }
    }
}