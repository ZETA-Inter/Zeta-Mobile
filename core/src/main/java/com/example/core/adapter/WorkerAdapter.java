package com.example.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_fornecedor.ListPage.ListAdapter;
import com.example.feature_fornecedor.ListPage.Worker;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>{

    private List<Worker> workers;
    private Context context;
    private WorkerAdapter.OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Worker worker);
    }

    public WorkerAdapter(List<Worker> workers, Context context, WorkerAdapter.OnItemClickListener listener) {
        this.workers = workers;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkerAdapter.WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(com.example.feature_fornecedor.R.layout.item_worker, parent, false);
        return new WorkerAdapter.WorkerViewHolder(view, listener);
    }


    @Override
    public void onBindViewHolder(@NonNull WorkerAdapter.WorkerViewHolder holder, int position) {
        Worker worker = workers.get(position);
        holder.bind(worker);
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    public void updateData(List<Worker> newWorkers) {
        this.workers.clear();
        this.workers.addAll(newWorkers);
        notifyDataSetChanged();
    }


    public static class WorkerViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView email;
        private WorkerAdapter.OnItemClickListener clickListener;

        public WorkerViewHolder(View view, WorkerAdapter.OnItemClickListener listener) {
            super(view);
            name = view.findViewById(com.example.feature_fornecedor.R.id.name);
            email = view.findViewById(com.example.feature_fornecedor.R.id.email);
            this.clickListener = listener;
        }

        public void bind(final Worker worker) {
            name.setText(worker.getName());
            email.setText(worker.getEmail());

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(worker);
                }
            });
        }
    }
}
