package com.example.feature_produtor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar; // Importe ProgressBar
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.R;
import com.example.feature_produtor.model.postegres.Program;


public class LessonsCardAdapter extends ListAdapter<Program, LessonsCardAdapter.LessonsViewHolder> {

    public interface OnLessonClickListener {
        void onLessonClick(Program item);
    }

    private final OnLessonClickListener listener;
    private final Context context;

    public LessonsCardAdapter(OnLessonClickListener listener, Context context) {
        super(new LessonsItemDiffCallback());
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public LessonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_lessons_item, parent, false);
        return new LessonsViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonsViewHolder holder, int position) {
        Program currentProgram = getItem(position);
        holder.bind(currentProgram, listener);
    }

    public static class LessonsViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView description;
        private final ImageView img;

        // Novos campos para a lógica de progresso
        private final ProgressBar progressBar;
        private final TextView percentageTextView;
        private final TextView percentageSymbolTextView;

        private final Context context;

        public LessonsViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            title = itemView.findViewById(R.id.cursoTitulo);
            description = itemView.findViewById(R.id.cursoDescr);
            img = itemView.findViewById(R.id.imageView3);

            // Mapeamento dos componentes de progresso do seu XML
            progressBar = itemView.findViewById(R.id.progressBar);
            percentageTextView = itemView.findViewById(R.id.porcentagem);
            percentageSymbolTextView = itemView.findViewById(R.id.text_pocentagem);
        }

        public void bind(final Program item, final OnLessonClickListener listener) {
            title.setText(item.getName());
            description.setText(item.getDescription());

            // --- Lógica de Progresso e Visibilidade ---
            // Assume que Program.getProgressPercentage() está disponível e retorna 0 se não iniciado.
            final int progress = item.getProgressPercentage();

            if (progress > 0) {
                // Curso iniciado: Torna a barra e o texto VÍSIVEIS
                progressBar.setVisibility(View.VISIBLE);
                percentageTextView.setVisibility(View.VISIBLE);
                percentageSymbolTextView.setVisibility(View.VISIBLE);

                // Atualiza o valor e a barra
                progressBar.setProgress(progress);
                percentageTextView.setText(String.valueOf(progress));

            } else {
                // Curso não iniciado (progresso = 0): Oculta a barra e o texto
                progressBar.setVisibility(View.GONE);
                percentageTextView.setVisibility(View.GONE);
                percentageSymbolTextView.setVisibility(View.GONE);
            }
            // --- Fim da Lógica de Progresso ---

            // Configura o clique
            itemView.setOnClickListener(v -> listener.onLessonClick(item));
        }
    }

    private static class LessonsItemDiffCallback extends DiffUtil.ItemCallback<Program> {
        @Override
        public boolean areItemsTheSame(@NonNull Program oldItem, @NonNull Program newItem) {
            if (oldItem.getId() == null || newItem.getId() == null) {
                return oldItem.getName().equals(newItem.getName());
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Program oldItem, @NonNull Program newItem) {
            // Adicionado progressPercentage na comparação para que o Adapter atualize a barra
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getProgressPercentage() == newItem.getProgressPercentage();
        }
    }
}