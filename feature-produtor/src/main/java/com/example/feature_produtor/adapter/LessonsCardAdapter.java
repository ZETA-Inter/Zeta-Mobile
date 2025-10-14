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
import com.example.feature_produtor.model.Program;


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
        Class currentClass = getItem(position);
        holder.bind(currentClass, listener);
    }

    public static class LessonsViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
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

        public void bind(final Class item, final OnLessonClickListener listener) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());

//            // 1. Lógica para obter a URL da imagem
//            String imageUrl = getImageUrlFromClass(item);
//
//            // 2. Usando o GLIDE para carregar a imagem da URL
//            if (imageUrl != null && !imageUrl.isEmpty()) {
//                Glide.with(context)
//                        .load(imageUrl)
//                        .into(img);
//            }

            itemView.setOnClickListener(v -> listener.onLessonClick(item));
        }


//        private String getImageUrlFromClass(Class item) {
//            return null;
        //metodo para pegar a imagem que não está na classe class
//        }
    }



    private static class LessonsItemDiffCallback extends DiffUtil.ItemCallback<Class> {
        @Override
        public boolean areItemsTheSame(@NonNull Class oldItem, @NonNull Class newItem) {

            if (oldItem.getId() == null || newItem.getId() == null) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Class oldItem, @NonNull Class newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getDescription().equals(newItem.getDescription());
            // oldItem.getImageUrl().equals(newItem.getImageUrl());
        }
    }
}
