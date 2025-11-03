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
        return new LessonsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonsViewHolder holder, int position) {
        Program currentProgram = getItem(position);
        holder.bind(currentProgram, listener);
    }

    public static class LessonsViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView description;

        public LessonsViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.cursoTitulo);
            description = itemView.findViewById(R.id.cursoDescr);
        }

        public void bind(final Program item, final OnLessonClickListener listener) {
            title.setText(item.getName());
            description.setText(item.getDescription());

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
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription());
        }
    }
}