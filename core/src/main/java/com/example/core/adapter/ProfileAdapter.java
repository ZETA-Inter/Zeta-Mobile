package com.example.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ProfileAdapter<T> extends RecyclerView.Adapter<ProfileAdapter.GenericViewHolder> {

    public interface Binder<T> {
        void bind(@NonNull View itemView, @NonNull T item, int position);
    }

    private final Context context;
    private final List<T> items;
    private final @LayoutRes int layoutId;
    private final Binder<T> binder;

    public ProfileAdapter(@NonNull Context context,
                          @NonNull List<T> items,
                          @LayoutRes int layoutId,
                          @NonNull Binder<T> binder) {
        this.context = context;
        this.items = items;
        this.layoutId = layoutId;
        this.binder = binder;
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new GenericViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        binder.bind(holder.itemView, items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void update(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public static class GenericViewHolder extends RecyclerView.ViewHolder {
        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
