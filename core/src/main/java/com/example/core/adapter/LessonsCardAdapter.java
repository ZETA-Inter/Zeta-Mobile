package com.example.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.R;
import com.example.core.model.Program;


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

        private final TextView percentageTextView;
        private final Context context;



        public LessonsViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            title = itemView.findViewById(R.id.cursoTitulo);
            description = itemView.findViewById(R.id.cursoDescr);
            img = itemView.findViewById(R.id.imageView3);


            // Mapeamento dos componentes de progresso do seu XML
            percentageTextView = itemView.findViewById(R.id.text_porcentagem);
        }

        public void bind(final Program item, final OnLessonClickListener listener) {
            title.setText(item.getName());
            description.setText(item.getDescription());


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
                    oldItem.getDescription().equals(newItem.getDescription());
        }
    }
}