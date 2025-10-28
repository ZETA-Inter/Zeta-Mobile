package com.example.feature_produtor; // Ajuste o pacote conforme necessário

import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.adapter.FlashCardAdapter;

public class FlashCardItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final FlashCardAdapter adapter;
    private final LinearLayout rootLayout; // Layout principal para mudar a cor

    // Cores (as mesmas usadas na solução anterior)
    private static final int COLOR_DEFAULT = Color.parseColor("#F0F0F0");
    private static final int COLOR_GREEN = Color.parseColor("#90EE90");
    private static final int COLOR_RED = Color.parseColor("#FF6347");

    public interface ItemTouchHelperAdapter {
        void onItemSwiped(int position, int direction);
    }

    public FlashCardItemTouchHelperCallback(FlashCardAdapter adapter, LinearLayout rootLayout) {
        this.adapter = adapter;
        this.rootLayout = rootLayout;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Habilita apenas o deslize (swipe) horizontal
        int dragFlags = 0;
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // Não usamos arrastar e soltar (drag and drop)
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Chamado quando o card é descartado
        rootLayout.setBackgroundColor(COLOR_DEFAULT); // Volta a cor para o padrão

        if (adapter instanceof ItemTouchHelperAdapter) {
            ((ItemTouchHelperAdapter) adapter).onItemSwiped(viewHolder.getAdapterPosition(), direction);
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        // **Lógica da Animação da Cor de Fundo**
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            // dX é a distância que o card foi deslizado
            float width = (float) viewHolder.itemView.getWidth();
            float ratio = Math.abs(dX) / width;
            ratio = Math.min(ratio, 1.0f); // Limita o ratio a 1.0

            int startColor = COLOR_DEFAULT;
            int endColor;

            if (dX > 0) {
                // Deslizando para a direita (Aprendi -> Verde)
                endColor = COLOR_GREEN;
            } else {
                // Deslizando para a esquerda (Esqueci -> Vermelho)
                endColor = COLOR_RED;
            }

            // Calcula a cor interpolada
            int color = (int) new ArgbEvaluator().evaluate(ratio, startColor, endColor);
            rootLayout.setBackgroundColor(color);

            // Adiciona um pequeno efeito de rotação (opcional)
            float maxRotation = 10f; // Rotação máxima em graus
            viewHolder.itemView.setRotation(maxRotation * (dX / width));
        } else {
            // Se o card voltar para o centro, a cor volta para o padrão
            rootLayout.setBackgroundColor(COLOR_DEFAULT);
            viewHolder.itemView.setRotation(0f);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}