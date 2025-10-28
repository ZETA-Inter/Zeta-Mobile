package com.example.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.dto.response.ProgramResponse;

import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder> {

    private List<ProgramResponse> programs;
    private Context context;
    private ProgramAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ProgramResponse program);
    }

    public ProgramAdapter(List<ProgramResponse> programs, Context context, ProgramAdapter.OnItemClickListener listener) {
        this.programs = programs;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramAdapter.ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(com.example.core.R.layout.item_lessons_worker, parent, false);
        return new ProgramAdapter.ProgramViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramAdapter.ProgramViewHolder holder, int position) {
        ProgramResponse program = programs.get(position);
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    public void updateData(List<ProgramResponse> newPrograms) {
        this.programs.clear();
        this.programs.addAll(newPrograms);
        notifyDataSetChanged();
    }

    public static class ProgramViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView description;
        private ProgramAdapter.OnItemClickListener clickListener;

        public ProgramViewHolder(View view, ProgramAdapter.OnItemClickListener listener) {
            super(view);
            // 🔧 Corrigido: IDs agora correspondem ao seu XML item_lessons_worker
            name = view.findViewById(com.example.core.R.id.cursoTitulo);
            description = view.findViewById(com.example.core.R.id.cursoDescr);
            this.clickListener = listener;
        }
    }
}
