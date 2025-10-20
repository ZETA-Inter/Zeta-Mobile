// Arquivo: com/example/feature_fornecedor/ListPage/ListAdapter.java
package com.example.feature_fornecedor.ListPage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_fornecedor.R;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private final List<Worker> workers;
    private final Context context;
    private OnWorkerClickListener listener;

    // interface custom para RecyclerView
    public interface OnWorkerClickListener {
        void onItemClick(Worker worker);
    }

    public ListAdapter(@NonNull List<Worker> workers, @NonNull Context context, @NonNull OnWorkerClickListener listener){
        this.workers = new ArrayList<>(workers);
        this.context = context;
        this.listener = listener;
    }

    public void setOnWorkerClickListener(OnWorkerClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker, parent, false);
        return new ListViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.bind(workers.get(position));
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    public void updateData(@NonNull List<Worker> newWorkers) {
        this.workers.clear();
        this.workers.addAll(newWorkers);
        notifyDataSetChanged();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView email;
        private OnWorkerClickListener clickListener;

        ListViewHolder(@NonNull View itemView, OnWorkerClickListener listener){
            super(itemView);
            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            this.clickListener = listener;
        }

        void bind(@NonNull final Worker worker) {
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
