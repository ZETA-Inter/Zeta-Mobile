package com.example.feature_produtor.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.R;
import com.example.core.model.mongo.FlashCard;
// Importação da interface que criamos no callback para comunicação
import com.example.feature_produtor.FlashCardItemTouchHelperCallback;

import java.util.List;


public class FlashCardAdapter extends RecyclerView.Adapter<FlashCardAdapter.FlashCardViewHolder>
        implements FlashCardItemTouchHelperCallback.ItemTouchHelperAdapter {

    private final List<FlashCard> flashCardList;
    private final Context context;
    private final SparseBooleanArray flippedPositions = new SparseBooleanArray();

    public FlashCardAdapter(List<FlashCard> flashCardList, Context context) {
        this.flashCardList = flashCardList;
        this.context = context;
    }

    @NonNull
    @Override
    public FlashCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new FlashCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashCardViewHolder holder, int position) {
        FlashCard flashCard = flashCardList.get(position);
        // Garante que o estado de flip seja resetado para novos cards, se for o caso
        boolean isFlipped = flippedPositions.get(position, false);
        holder.bind(flashCard, isFlipped, position);
    }

    @Override
    public int getItemCount() {
        return flashCardList.size();
    }


    @Override
    public void onItemSwiped(int position, int direction) {
        if (position >= 0 && position < flashCardList.size()) {

            FlashCard cardSwiped = flashCardList.get(position);

            if (direction == ItemTouchHelper.RIGHT) {
                // APRENDI (Deslize para a direita): Remove permanentemente da lista atual
                flashCardList.remove(position);
                notifyItemRemoved(position); // Remove da vista

            } else if (direction == ItemTouchHelper.LEFT) {
                // ESQUECI (Deslize para a esquerda): Move para o final para revisão

                // 1. Remove da posição atual
                flashCardList.remove(position);

                // 2. Adiciona no final
                flashCardList.add(cardSwiped);

                // 3. Notificação: Usamos notifyItemMoved para que o card suma suavemente
                // e o restante da pilha se mova.
                int newPosition = flashCardList.size() - 1;
                notifyItemMoved(position, newPosition); // Move da posição 'position' para o final

                // Limpa o estado de flip do card que foi movido
                flippedPositions.put(newPosition, false);
            }

            // Limpa o estado de flip da posição que foi removida/movida (posição antiga)
            flippedPositions.delete(position);
        }
    }

    public class FlashCardViewHolder extends RecyclerView.ViewHolder {

        private final TextView textFront;
        private final TextView textBack;
        private final LinearLayout cardFront;
        private final LinearLayout cardBack;
        private final CardView cardContainer;

        public FlashCardViewHolder(@NonNull View itemView) {
            super(itemView);
            textFront = itemView.findViewById(R.id.txtFront);
            textBack = itemView.findViewById(R.id.txtBack);
            cardFront = itemView.findViewById(R.id.card_front);
            cardBack = itemView.findViewById(R.id.card_back);
            cardContainer = itemView.findViewById(R.id.flash_root);

            float scale = itemView.getResources().getDisplayMetrics().density;
            int distance = 8000;
            cardFront.setCameraDistance(distance * scale);
            cardBack.setCameraDistance(distance * scale);
        }

        public void bind(final FlashCard item, boolean isFlipped, final int position) {
            textFront.setText(item.getFront());
            textBack.setText(item.getBack());

            // estado inicial
            if (isFlipped) {
                cardFront.setVisibility(View.GONE);
                cardBack.setVisibility(View.VISIBLE);
                cardFront.setRotationY(180f);
                cardBack.setRotationY(0f);
            } else {
                cardFront.setVisibility(View.VISIBLE);
                cardBack.setVisibility(View.GONE);
                cardFront.setRotationY(0f);
                cardBack.setRotationY(-180f);
            }

            cardContainer.setOnClickListener(v -> {
                boolean currentlyFlipped = flippedPositions.get(position, false);
                flipCard(currentlyFlipped, position);
            });
        }

        private void flipCard(boolean currentlyFlipped, int position) {
            // Lógica de flip... (perfeita, mantida)
            if (!currentlyFlipped) {
                // front -> back
                ObjectAnimator hide = ObjectAnimator.ofFloat(cardFront, "rotationY", 0f, 90f);
                hide.setDuration(200);
                hide.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cardFront.setVisibility(View.GONE);
                        cardBack.setVisibility(View.VISIBLE);
                        cardBack.setRotationY(-90f);
                        ObjectAnimator show = ObjectAnimator.ofFloat(cardBack, "rotationY", -90f, 0f);
                        show.setDuration(200);
                        show.start();
                    }
                });
                hide.start();
                flippedPositions.put(position, true);
            } else {
                // back -> front
                ObjectAnimator hide = ObjectAnimator.ofFloat(cardBack, "rotationY", 0f, 90f);
                hide.setDuration(200);
                hide.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cardBack.setVisibility(View.GONE);
                        cardFront.setVisibility(View.VISIBLE);
                        cardFront.setRotationY(-90f);
                        ObjectAnimator show = ObjectAnimator.ofFloat(cardFront, "rotationY", -90f, 0f);
                        show.setDuration(200);
                        show.start();
                    }
                });
                hide.start();
                flippedPositions.put(position, false);
            }
        }
    }
}