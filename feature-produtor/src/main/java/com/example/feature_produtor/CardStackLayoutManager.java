package com.example.feature_produtor; // Crie este pacote

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;


public class CardStackLayoutManager extends RecyclerView.LayoutManager {

    // Número máximo de cartas visíveis na pilha.
    private static final int MAX_VISIBLE_CARDS = 3;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true; // Permitir que o RecyclerView cuide da medição
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        // 1. Desanexa e recicla todas as views existentes.
        detachAndScrapAttachedViews(recycler);

        int itemCount = getItemCount();

        // 2. Itera para posicionar apenas os cards visíveis
        for (int i = 0; i < itemCount; i++) {
            // Apenas layout para os cards do topo (mais recentes)
            if (i < MAX_VISIBLE_CARDS) {
                View view = recycler.getViewForPosition(i);
                addView(view);

                // Medir a view com margens (IMPORTANTE)
                measureChildWithMargins(view, 0, 0);

                // Centraliza a view dentro do RecyclerView (USANDO O TAMANHO DO PAI - O RECYCLERVIEW)
                int parentWidth = getWidth();
                int parentHeight = getHeight();

                int cardWidth = getDecoratedMeasuredWidth(view);
                int cardHeight = getDecoratedMeasuredHeight(view);

                // Centraliza o card (e não o item_flashcard.xml inteiro)
                int left = (parentWidth - cardWidth) / 2;
                int top = (parentHeight - cardHeight) / 2;

                layoutDecoratedWithMargins(view, left, top, left + cardWidth, top + cardHeight);
            } else {
                // Recicla os cards que estão muito para trás
                recycler.recycleView(recycler.getViewForPosition(i));
            }
        }
    }
}