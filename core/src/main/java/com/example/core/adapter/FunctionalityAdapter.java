package com.example.core.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.R;
import com.example.core.dto.response.PlanResponse;

import java.util.List;

public class FunctionalityAdapter extends RecyclerView.Adapter<FunctionalityAdapter.FunctionalityViewHolder> {

    private List<PlanResponse.Functionalities> functionalities;

    public FunctionalityAdapter(List<PlanResponse.Functionalities> functionalities) {
        this.functionalities = functionalities;
    }

    @NonNull
    @Override
    public FunctionalityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan_functionality, parent, false);
        FunctionalityViewHolder holder = new FunctionalityViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionalityViewHolder holder, int position) {
        PlanResponse.Functionalities functionality = functionalities.get(position);

        holder.tvBasicItem.setText("• " + functionality.getName());
    }

    @Override
    public int getItemCount() {
        return functionalities.size();
    }

    public class FunctionalityViewHolder extends RecyclerView.ViewHolder {

        TextView tvBasicItem;

        public FunctionalityViewHolder(@NonNull View functionalityView) {
            super(functionalityView);
            tvBasicItem = functionalityView.findViewById(R.id.tvBasicItem);
        }
    }
}
