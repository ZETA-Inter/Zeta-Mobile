package com.example.feature_produtor;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.core.TipoUsuario;
import com.example.feature_produtor.model.mongo.Content;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.feature_produtor.adapter.*;
import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.model.postegres.Segment;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.feature_produtor.api.*;
import com.example.feature_produtor.ui.bottomnav.WorkerBottomNavView;

public class HomePageWorkerFragment extends Fragment implements LessonsCardAdapter.OnLessonClickListener, FilterAdapter.OnSegmentClickListener{

    private ImageView perfil;
    private ImageView boxIa;
    private ImageView iconConfig;
    private RecyclerView recyclerTipoConteudo;
    private RecyclerView recyclerCursosAndamento;

    private LessonsCardAdapter lessonsCardAdapter;
    private FilterAdapter filterAdapter;

    private ApiPostgres apiPostgres;
    private ImageView iconNotificacao;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page_worker, container, false);



        WorkerBottomNavView bottom = view.findViewById(com.example.feature_produtor.R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    com.example.feature_produtor.R.id.LessonsWorker,    // atividades
                    com.example.feature_produtor.R.id.HomePageWorker,       // home
                    com.example.feature_produtor.R.id.GoalsPageWorker  // metas
            );


            bottom.setActive(WorkerBottomNavView.Item.HOME, false);
        }




        // 1. Inicializando variáveis
        perfil=view.findViewById(R.id.icon_perfil);
        boxIa=view.findViewById(R.id.boxIA);
        iconConfig=view.findViewById(R.id.icon_configuracoes);
        iconNotificacao= view.findViewById(R.id.icon_notificacao);
        recyclerCursosAndamento=view.findViewById(R.id.recycler_cursos_andamento);
        recyclerTipoConteudo =view.findViewById(R.id.recycler_tipo_conteudo);






        // 2. Iniciando os Adapters
        lessonsCardAdapter = new LessonsCardAdapter(this,getContext());
        filterAdapter = new FilterAdapter(this);

        // 3. Configurando RecyclerViews
        recyclerCursosAndamento.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        recyclerCursosAndamento.setAdapter(lessonsCardAdapter);

        // Configurando RecyclerView de filtro
        recyclerTipoConteudo.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
        recyclerTipoConteudo.setAdapter(filterAdapter);

        String token = "TokenUserZeta1234";

        // Configura o OkHttpClient com o Interceptor para adicionar o cabeçalho Authorization
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Tempo para estabelecer a conexão (30s)
                .readTimeout(30, TimeUnit.SECONDS)    // Tempo para ler a resposta (30s)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(request);
                })
                .build();


        // 4. Iniciando Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-postgresql-zeta-fide.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiPostgres = retrofit.create(ApiPostgres.class);


        // 5. Fazendo chamada para a API (recycler cursos andamento)
        Call<List<Program>> call = apiPostgres.getAllPrograms();

        call.enqueue(new Callback<List<Program>>() {
            @Override
            public void onResponse(Call<List<Program>> call, Response<List<Program>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Program> lessons = response.body();
                    lessonsCardAdapter.submitList(lessons);

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar cursos: " + response.code(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<List<Program>> call, Throwable t) {
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });


        Call<List<Segment>> segmentCall = apiPostgres.getAllSegments();

        segmentCall.enqueue(new Callback<List<Segment>>() {
            @Override
            public void onResponse(Call<List<Segment>> call, Response<List<Segment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Segment> segmentsTypes = response.body();
                    filterAdapter.submitList(segmentsTypes);
                } else {
                    Toast.makeText(getContext(), "Erro ao carregar filtros: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Segment>> call, Throwable t) {
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        setupClickListeners();
        return view;

    }

    private void setupClickListeners() {
        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.Profileworker);
            }
        });

        boxIa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.ChatBotPageWorker);

            }
        });

        iconConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigation.findNavController(v).navigate(R.id.);

            }
        });
        iconNotificacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getView() != null) {

                    Navigation.findNavController(getView()).navigate(R.id.CardNotificacao);
                }
            }
        });


    }



    @Override
    public void onLessonClick(Program item) {
        // Criar um Bundle para empacotar o id
        Bundle bundle = new Bundle();

        //Adicionar infos do curso (Program) ao Bundle.
        Integer programId = item.getId(); // Renomeado para 'programId' para maior clareza
        bundle.putString("programId", String.valueOf(programId));

        //Navega para o destino, passando o Bundle como argumento
        // Assumindo que 'ContentLessonWorker' mostra os detalhes/etapas do curso (Program)
        if(getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.StepsLessonWorker, bundle);
        }

    }


    @Override
    public void onSegmentClick(Segment nome) {
        // Criar um Bundle para empacotar o id
        Bundle bundle = new Bundle();

        //Adicionar tipo ao Bundle.
        String filterType = nome.getName();
        bundle.putString("segment", filterType);

        //tiar depois de testes
        Toast.makeText(getContext(), "Filtrando por: " + filterType, Toast.LENGTH_SHORT).show();

        //Navega para o destino, passando o Bundle como argumento
        Navigation.findNavController(getView()).navigate(R.id.LessonsWorker, bundle);


    }



}