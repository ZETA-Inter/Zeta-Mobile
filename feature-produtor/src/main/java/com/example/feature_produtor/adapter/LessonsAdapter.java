package com.example.feature_produtor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.example.feature_produtor.R;
import com.example.feature_produtor.model.LessonsItem;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.LessonsViewHolder> {

    private List<LessonsItem> lessonsList;
    private final OnLessonClickListener listener; // O listener de clique

    // Interface para notificar o Fragment quando um card de curso for clicado
    public interface OnLessonClickListener {
        void onLessonClick(LessonsItem item);
    }

    public LessonsAdapter(List<LessonsItem> lessonsList, OnLessonClickListener listener) {
        this.lessonsList = lessonsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item de curso que definimos anteriormente
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_lessons_item, parent, false);
        return new LessonsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonsViewHolder holder, int position) {
        LessonsItem item = lessonsList.get(position);

        // Atribui os dados do LessonsItem aos Views do card
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());


        holder.quantModulos.setText(item.getQuantModulos() + " Módulos");

        // Seta a imagem (usando o recurso Drawable ID)
        holder.icon.setImageResource(item.getImageResourceId());

        // Configura o evento de clique no card
        holder.itemView.setOnClickListener(v -> {
            listener.onLessonClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return lessonsList.size();
    }


    public void updateList(List<LessonsItem> newList) {
        this.lessonsList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder: Mantém referências aos Views de cada card
    public static class LessonsViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView icon;

        TextView quantModulos;

        public LessonsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referência aos IDs do seu fragment_lessons_iten.xml
            title = itemView.findViewById(R.id.cursoTitulo);
            description = itemView.findViewById(R.id.description);
            icon = itemView.findViewById(R.id.imageView3);
            quantModulos = itemView.findViewById(R.id.cursoQuantModulo);
        }
    }
}