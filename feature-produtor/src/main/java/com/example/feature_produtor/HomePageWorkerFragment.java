package com.example.feature_produtor;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;



import com.example.feature_produtor.adapter.LessonsAdapter;
import com.example.feature_produtor.model.LessonsItem;

import java.util.ArrayList;
import java.util.List;


public class HomePageWorkerFragment extends Fragment implements LessonsAdapter.OnLessonClickListener { // Removemos a interface do FilterAdapter

//ATENÇÃO ESSA CLASSE POSSUI APENAS UM MOLDE DE COMO SERIA A CONFIURAÇÃO DA TELA
// ESSE CODIGO NÃO TEM CONGURENCIA NENHUMA COM OS BANCOS
    private RecyclerView recyclerCursosAndamento;
    //private RecyclerView recyclerNovosConteudos;

    private LessonsAdapter cursosAndamentoAdapter;
    //private LessonsAdapter novosConteudosAdapter;

   //filtros
    private TextView buttonTreinamento;
    private TextView buttonLeis;
    private TextView buttonRegras;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page_worker, container, false);


        // 1. Inicializa Views dos Filtros
        buttonTreinamento = view.findViewById(R.id.button_filter_treinamento);
        buttonLeis = view.findViewById(R.id.button_filter_leis);
        buttonRegras = view.findViewById(R.id.button_filter_regras);

        // Inicializa RecyclerViews
        recyclerCursosAndamento = view.findViewById(R.id.recycler_cursos_andamento);
        //recyclerNovosConteudos = view.findViewById(R.id.recycler_novos_conteudos);

        //setupCursosAndamentoRecyclerView();
        // setupNovosConteudosRecyclerView();

        //chama click de filtros
        //setupFilterButtons();

        return view;
    }

    // LÓGICA DOS BOTÕES DE FILTRO

//    private void setupFilterButtons() {
//        // Usa o mesmo listener para os 3 botões, mas passa o texto do botão como categoria
//        View.OnClickListener filterClickListener = v -> {
//            String filterName = ((TextView) v).getText().toString();
//            navigateToFilteredList(filterName);
//        };
//
//        buttonTreinamento.setOnClickListener(filterClickListener);
//        buttonLeis.setOnClickListener(filterClickListener);
//        buttonRegras.setOnClickListener(filterClickListener);
//    }
//
//    private void navigateToFilteredList(String filterName) {
//        Bundle bundle = new Bundle();
//        bundle.putString("FILTER_CATEGORY", filterName);
//
//        Toast.makeText(getContext(), "Navegando para: " + filterName, Toast.LENGTH_SHORT).show();
//
//        try {
//
//            //NavHostFragment.findNavController(this).navigate(R.id.action_home_to_filtered_list, bundle);
//        } catch (Exception e) {
//            Toast.makeText(getContext(), "Erro de navegação: Verifique o NavGraph.", Toast.LENGTH_LONG).show();
//        }
//    }
//



    // MÉTODOS DE SETUP DE RECYCLERVIEW


//    private void setupCursosAndamentoRecyclerView() {
//
//        //List<LessonsItem> initialList = getCursosEmAndamentoData();
//
//        //
//        cursosAndamentoAdapter = new LessonsAdapter(initialList, this);
//        recyclerCursosAndamento.setAdapter(cursosAndamentoAdapter);
//
//        //Horizontal
//        recyclerCursosAndamento.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//    }

    // Configuração para a seção "Novos Conteúdos"
//    private void setupNovosConteudosRecyclerView() {
//        List<LessonsItem> initialList = getNovosConteudosData();
//
//        novosConteudosAdapter = new LessonsAdapter(initialList, this);
//        recyclerNovosConteudos.setAdapter(novosConteudosAdapter);
//
//        recyclerNovosConteudos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//    }

    //Logica mongo (molde, ainda será desenvolvido)

//    private List<LessonsItem> getCursosEmAndamentoData() {
//        // Lógica de pegar dados do MongoDB para "Cursos em Andamento"
//        List<LessonsItem> list = new ArrayList<>();
//
//        // Exemplo MOCK:
//        list.add(new LessonsItem("C1", "Transporte Seguro", "Todo cuidado é bom", R.drawable.ic_caminhao, 3));
//        list.add(new LessonsItem("C2", "Primeiros Socorros", "Essencial para o trabalho", R.drawable.ic_medico, 5));
//
//        return list;
//    }
//
//    private List<LessonsItem> getNovosConteudosData() {
//        // Lógica de pegar dados do MongoDB para "Novos Conteúdos"
//        List<LessonsItem> list = new ArrayList<>();
//
//        // Exemplo MOCK:
//        list.add(new LessonsItem("N1", "Novidade da Semana", "Breve descrição do novo curso", R.drawable.ic_estrela, 1));
//
//        return list;
//    }



    @Override
    public void onLessonClick(LessonsItem item) {
        navigateToCourseDetail(item);
    }

    private void navigateToCourseDetail(LessonsItem course) {

        NavHostFragment.findNavController(this).navigate(R.id.LessonsWorker);

    }
}