package com.example.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.R;
import com.example.core.dto.response.ProgramWorkerResponseDTO;


public class LessonsCardProgressAdapter extends ListAdapter<ProgramWorkerResponseDTO, LessonsCardProgressAdapter.LessonsViewHolder> {

    public interface OnLessonClickListener {
        void onLessonClick(ProgramWorkerResponseDTO item);
    }

    private final OnLessonClickListener listener;
    private final Context context;

    public LessonsCardProgressAdapter(OnLessonClickListener listener, Context context) {
        // Usa um DiffCallback específico para este DTO
        super(new LessonsItemDiffCallback());
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public LessonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lessons_worker, parent, false); // Assumindo o mesmo layout de item
        return new LessonsViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonsViewHolder holder, int position) {
        ProgramWorkerResponseDTO currentProgram = getItem(position);
        holder.bind(currentProgram, listener);
    }

    public static class LessonsViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView description;

        private final TextView percentage;

        private final ProgressBar percentageBar;

        public LessonsViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            title = itemView.findViewById(R.id.cursoTitulo);
            description = itemView.findViewById(R.id.cursoDescr);


            percentage = itemView.findViewById(R.id.text_porcentagem);
            percentageBar = itemView.findViewById(R.id.percentageBar);
        }


        public void bind(final ProgramWorkerResponseDTO item, final OnLessonClickListener listener) {
            title.setText(item.getName());
            description.setText(item.getDescription());

            final int progress = item.getProgressPercentage();
            percentage.setText(String.valueOf(progress) + "%");
            percentageBar.setProgress(progress);
            itemView.setOnClickListener(v -> listener.onLessonClick(item));
        }
    }

    private static class LessonsItemDiffCallback extends DiffUtil.ItemCallback<ProgramWorkerResponseDTO> {
        @Override
        public boolean areItemsTheSame(@NonNull ProgramWorkerResponseDTO oldItem, @NonNull ProgramWorkerResponseDTO newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProgramWorkerResponseDTO oldItem, @NonNull ProgramWorkerResponseDTO newItem) {
            // Compara o conteúdo e o Progresso
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getProgressPercentage() == newItem.getProgressPercentage();
        }
    }
}