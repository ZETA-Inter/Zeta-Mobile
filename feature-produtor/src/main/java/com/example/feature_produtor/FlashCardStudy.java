package com.example.feature_produtor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.adapter.FlashCardAdapter;
import com.example.feature_produtor.model.mongo.FlashCard;
import com.example.feature_produtor.FlashCardItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

public class FlashCardStudy extends Fragment
        implements FlashCardItemTouchHelperCallback.ItemTouchHelperAdapter {

    private RecyclerView recyclerView;
    private FlashCardAdapter adapter;
    private List<FlashCard> flashCardList;
    private LinearLayout mainRootLayout;
    private ItemTouchHelper itemTouchHelper;
    private FlashCardItemTouchHelperCallback touchHelperCallback;

    // Contadores e Título Mock
    private int acertosCount = 0;
    private int errosCount = 0;
    private static final String CURSO_MOCK_TITLE = "Rastreabilidade Bovina 🐂";

    // Cores (ajustadas para uso direto no código)
    private static final int COLOR_DEFAULT = Color.parseColor("#F0F0F0");
    private static final int COLOR_GREEN = Color.parseColor("#90EE90");
    private static final int COLOR_RED = Color.parseColor("#FF6347");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_flash_card_study, container, false);

        mainRootLayout = view.findViewById(R.id.main_root_layout);
        recyclerView = view.findViewById(R.id.recycler_flash_cards);

        flashCardList = new ArrayList<>();
        adapter = new FlashCardAdapter(flashCardList, requireContext());

        // 1. Configuração do RecyclerView
        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        // 2. Configuração do ItemTouchHelper
        touchHelperCallback = new FlashCardItemTouchHelperCallback(adapter, mainRootLayout);
        itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // 3. Configuração dos Botões
        Button btnLearned = view.findViewById(R.id.btn_learned);
        Button btnNotLearned = view.findViewById(R.id.btn_not_learned);

        btnLearned.setOnClickListener(v -> swipeProgrammatically(ItemTouchHelper.RIGHT));
        btnNotLearned.setOnClickListener(v -> swipeProgrammatically(ItemTouchHelper.LEFT));

        loadMockData();
        return view;
    }


    @Override
    public void onItemSwiped(int position, int direction) {

    }


    // FlashCardStudy.java

// ... (métodos e variáveis da classe) ...

    private void swipeProgrammatically(int direction) {
        final int topPosition = 0;
        final RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(topPosition);

        if (holder != null) {

            // 1. Configuração Visual
            // ... (código da cor e targetX/rotation, que está correto) ...
            int bgColor = (direction == ItemTouchHelper.RIGHT) ? COLOR_GREEN : COLOR_RED;
            mainRootLayout.setBackgroundColor(bgColor);

            float targetX = (direction == ItemTouchHelper.RIGHT) ? recyclerView.getWidth() : -recyclerView.getWidth();
            float rotation = (direction == ItemTouchHelper.RIGHT) ? 15f : -15f;

            // 2. Animação
            holder.itemView.animate()
                    .translationX(targetX)
                    .rotation(rotation)
                    .setDuration(300)
                    .withEndAction(() -> {

                        // 3. Ação Lógica (SÓ APÓS A ANIMAÇÃO)

                        // A. Atualiza os Contadores
                        if (direction == ItemTouchHelper.RIGHT) {
                            acertosCount++;
                        } else {
                            errosCount++;
                        }

                        // B. Remove/Move o item no Adapter
                        adapter.onItemSwiped(topPosition, direction);

                        // C. FORÇA O FOCO NA POSIÇÃO [0] APÓS A MOVIMENTAÇÃO
                        // Isso garante que o novo card no topo seja visível.
                        recyclerView.scrollToPosition(0);

                        // D. Checa o fim da pilha e navega
                        checkEndAndShowDialog();

                        // E. Reseta as propriedades visuais
                        mainRootLayout.setBackgroundColor(COLOR_DEFAULT);
                        holder.itemView.setTranslationX(0);
                        holder.itemView.setRotation(0f);
                    })
                    .start();
        }
    }

// ... (restante do código da classe) ...


    private void checkEndAndShowDialog() {
        if (adapter.getItemCount() == 0) {
            // TODOS OS CARDS FORAM PROCESSADOS!

            // 1. Encontra o NavController
            if (getView() != null) {
                NavController navController = Navigation.findNavController(getView());

                // 2. Cria o Bundle com os resultados
                Bundle args = new Bundle();
                args.putString("curso_nome", CURSO_MOCK_TITLE);
                args.putInt("acertos_count", acertosCount);
                args.putInt("erros_count", errosCount);

                // 3. NAVEGA para a HomeFragment
                // **** VOCÊ DEVE SUBSTITUIR ESTE ID ****
                // Use o ID do seu HomeFragment (Ex: R.id.homePageworkerFragment)
                try {
                    // Navega para a Home, garantindo que a tela FlashCardStudy seja removida do stack.
                    // Substitua R.id.HomeFragmentId pelo ID real do seu destino Home.
                    navController.navigate(R.id.HomePageWorker, args);

                } catch (Exception e) {
                    // Trate o erro de navegação aqui (ID não encontrado, etc.)
                    System.err.println("Erro ao navegar para a Home: " + e.getMessage());
                }
            }
        }
    }

// ... (O restante do código de FlashCardStudy.java permanece o mesmo)

    private void loadMockData() {
        flashCardList.clear();
        flashCardList.add(createCard("O que é rastreabilidade animal?", "Processo de identificar e acompanhar o animal."));
        flashCardList.add(createCard("Qual a importância do MAPA?", "Controle sanitário e certificação."));
        flashCardList.add(createCard("Métodos de identificação de bovinos?", "Brincos, tatuagens ou microchips."));
        flashCardList.add(createCard("O que é DTA?", "Documento de Transito Animal"));
        adapter.notifyDataSetChanged();
    }

    private FlashCard createCard(String front, String back) {
        FlashCard c = new FlashCard();
        c.setFront(front);
        c.setBack(back);
        return c;
    }
}