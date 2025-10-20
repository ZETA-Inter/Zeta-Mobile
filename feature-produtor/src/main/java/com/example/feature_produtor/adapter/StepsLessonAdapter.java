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
import com.example.feature_produtor.model.postegres.Segment;

public class StepsLessonAdapter extends ListAdapter<Segment, StepsLessonAdapter.LessonStepViewHolder> {

    public interface OnStepClickListener {
        void onStepClick(Segment item);
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
        Segment currentSegment = getItem(position);
        int stepNumber = position + 1;
        boolean isLastItem = position == getItemCount() - 1;
        holder.bind(currentSegment, listener, stepNumber, isLastItem);
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

        public void bind(final Segment item, final OnStepClickListener listener, int stepNumber, boolean isLastItem) {

            // Define o número da etapa
            num.setText("Etapa " + stepNumber);

            etapaDescricao.setText(item.getName());
            itemView.setOnClickListener(v -> listener.onStepClick(item));
        }
    }


    private static class LessonStepDiffCallback extends DiffUtil.ItemCallback<Segment> {

        // Verifica se é o mesmo card (ID)
        @Override
        public boolean areItemsTheSame(@NonNull Segment oldItem, @NonNull Segment newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        // Compara itens para ver o que precisa ser atualizado
        @Override
        public boolean areContentsTheSame(@NonNull Segment oldItem, @NonNull Segment newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    }
}
