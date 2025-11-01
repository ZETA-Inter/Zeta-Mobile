package com.example.feature_produtor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.R;

import com.example.core.model.Segment;
public class FilterAdapter extends ListAdapter<Segment, FilterAdapter.SegmentViewHolder> {


    public interface OnSegmentClickListener {
        void onSegmentClick(Segment nome);
    }

    private final OnSegmentClickListener listener;

    public FilterAdapter(OnSegmentClickListener listener) {
        super(new SegmentItemDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_filter_content, parent, false);
        return new SegmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SegmentViewHolder holder, int position) {
        Segment currentSegment = getItem(position);
        holder.bind(currentSegment, listener);
    }


    public static class SegmentViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewFiltro;

        public SegmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFiltro = itemView.findViewById(R.id.text_filtro);
        }

        public void bind(final Segment nome, final OnSegmentClickListener listener) {

            textViewFiltro.setText(nome.getName());
            itemView.setOnClickListener(v -> listener.onSegmentClick(nome));
        }
    }

    private static class SegmentItemDiffCallback extends DiffUtil.ItemCallback<Segment> {
        @Override
        public boolean areItemsTheSame(@NonNull Segment oldItem, @NonNull Segment newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Segment oldItem, @NonNull Segment newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    }
}
