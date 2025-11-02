package com.example.feature_produtor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.R;
import com.example.core.model.mongo.Class; // IMPORTANTE: Importar o modelo Class

// Extende ListAdapter de Class
public class StepsLessonAdapter extends ListAdapter<Class, StepsLessonAdapter.LessonStepViewHolder> {

    // A interface usa o modelo Class
    public interface OnStepClickListener {
        void onProgressUpdated(int newPercentage);

        void onError(String message);

        void onStepClick(Class item, Integer stepNumber);
    }

    private final OnStepClickListener listener;
    private final Context context;

    public StepsLessonAdapter(OnStepClickListener listener, Context context) {
        super(new LessonStepDiffCallback());
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public LessonStepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_steps_lesson_item, parent, false);
        return new LessonStepViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonStepViewHolder holder, int position) {
        Class currentClass = getItem(position);
        int stepNumber = position + 1;
        boolean isLastItem = position == getItemCount() - 1;
        holder.bind(currentClass, listener, stepNumber, isLastItem);
    }

    public static class LessonStepViewHolder extends RecyclerView.ViewHolder {

        private final TextView etapaDescricao;
        private final TextView num;
        private final Context context;

        public LessonStepViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            etapaDescricao = itemView.findViewById(R.id.etapaTitulo);
            num = itemView.findViewById(R.id.num);
        }

        public void bind(final Class item, final OnStepClickListener listener, int stepNumber, boolean isLastItem) {

            // Define o número da etapa
            num.setText("Etapa " + stepNumber);

            // Usa item.getTitle() para o título, conforme o modelo Class
            etapaDescricao.setText(item.getTitle());
            itemView.setOnClickListener(v -> listener.onStepClick(item, stepNumber));
        }
    }


    private static class LessonStepDiffCallback extends DiffUtil.ItemCallback<Class> {

        // Verifica se é o mesmo card (ID)
        @Override
        public boolean areItemsTheSame(@NonNull Class oldItem, @NonNull Class newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        // Compara itens para ver o que precisa ser atualizado
        @Override
        public boolean areContentsTheSame(@NonNull Class oldItem, @NonNull Class newItem) {
            // Compara o Título
            return oldItem.getTitle().equals(newItem.getTitle());
        }
    }
}