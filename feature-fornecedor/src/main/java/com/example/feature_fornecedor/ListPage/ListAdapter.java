package com.example.feature_fornecedor.ListPage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_fornecedor.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private List<Worker> workers;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Worker worker);
    }

    public ListAdapter(List<Worker> workers, Context context, OnItemClickListener listener) {
        this.workers = workers;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Worker worker = workers.get(position);
        holder.bind(worker);
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView email;
        private OnItemClickListener listener;

        public ListViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            email = view.findViewById(R.id.email);
        }

        public void bind(Worker worker) {
            name.setText(worker.getName());
            email.setText(worker.getEmail());

            itemView.setOnClickListener(v -> listener.onItemClick(worker));
        }
    }

}
