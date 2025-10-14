package com.example.feature_produtor.adapter;

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

import com.example.feature_produtor.R;
import com.example.feature_produtor.model.postegres.Program; // Importa a classe Program correta


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
        Program currentProgram = getItem(position); // Usando Program
        holder.bind(currentProgram, listener);
    }

    public static class LessonsViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView description;
        private final ImageView img;
        private final Context context;

        public LessonsViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            title = itemView.findViewById(R.id.cursoTitulo);
            description = itemView.findViewById(R.id.cursoDescr);
            img = itemView.findViewById(R.id.imageView3);
        }

        public void bind(final Program item, final OnLessonClickListener listener) { // Usando Program
            // Atualizado para usar getName() e getDescription() de Program
            title.setText(item.getName());
            description.setText(item.getDescription());

            // Lógica para carregar a imagem (se houver, precisa do Glide/Picasso)
//            String imageUrl = getImageUrlFromProgram(item);
//
//            if (imageUrl != null && !imageUrl.isEmpty()) {
//                Glide.with(context)
//                        .load(imageUrl)
//                        .into(img);
//            }

            itemView.setOnClickListener(v -> listener.onLessonClick(item));
        }


//        private String getImageUrlFromProgram(Program item) {
//            // Implemente a lógica para obter a URL da imagem de Program aqui, se necessário.
//            return null;
//        }
    }



    private static class LessonsItemDiffCallback extends DiffUtil.ItemCallback<Program> { // Usando Program
        @Override
        public boolean areItemsTheSame(@NonNull Program oldItem, @NonNull Program newItem) { // Usando Program

            if (oldItem.getId() == null || newItem.getId() == null) {
                // Alternativa se o ID for nulo, compara pelo nome
                return oldItem.getName().equals(newItem.getName());
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Program oldItem, @NonNull Program newItem) { // Usando Program
            // Comparando nome e descrição
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription());
        }
    }
}