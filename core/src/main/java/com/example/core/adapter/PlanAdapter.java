package com.example.core.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.R;
import com.example.core.dto.response.PlanResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<PlanResponse> plans;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(PlanResponse plan);
    }

    public PlanAdapter(List<PlanResponse> plans, OnItemClickListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanResponse plan = plans.get(position);

        holder.tvTitle.setText(plan.getPlanName());
        holder.tvPrice.setText(plan.getValue() == 0 ? "FREE" : String.format("R$%,.2f", plan.getValue()));

        holder.functionalitiesRV.setLayoutManager(
                new LinearLayoutManager(holder.itemView.getContext(),
                        LinearLayoutManager.VERTICAL, false)
        );

        holder.functionalitiesRV.setAdapter(new FunctionalityAdapter(plan.getFunctionalities()));

        // Cores de borda
        int primary = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_dark);
        int transparent = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent);

        // Aplica a cor da borda se for o item selecionado
        holder.card.setStrokeColor(position == selectedPosition ? primary : transparent);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null)
                listener.onItemClick(plan);
        });
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvTitle, tvPrice;
        RecyclerView functionalitiesRV;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvTitle = itemView.findViewById(R.id.tvBasicTitle);
            tvPrice = itemView.findViewById(R.id.tvBasicPrice);
            functionalitiesRV = itemView.findViewById(R.id.functionaltiesRV);
        }
    }
}
