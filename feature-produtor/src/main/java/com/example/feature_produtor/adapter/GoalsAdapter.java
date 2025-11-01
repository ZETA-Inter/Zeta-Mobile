package com.example.feature_produtor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.R;
import com.example.feature_produtor.model.postegres.Goal;

// Este adaptador não precisa de Listener por enquanto, pois é apenas de exibição.
public class GoalsAdapter extends ListAdapter<Goal, GoalsAdapter.GoalViewHolder> {

    public interface OnGoalLongClickListener {
        void onGoalLongClicked(Goal goal);
    }

    private final OnGoalLongClickListener listener;

    public GoalsAdapter(OnGoalLongClickListener listener) {
        super(new GoalDiffCallback());
        this.listener = listener;
    }


    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal currentGoal = getItem(position);
        holder.bind(currentGoal, listener);
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {

        private final TextView goalName;
        private final TextView goalDescription;
        private final ImageView goalStatusImage;
        private final View goalIndicatorLine; // << adicionar

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalName = itemView.findViewById(R.id.text_goal_name);
            goalDescription = itemView.findViewById(R.id.text_goal_description);
            goalStatusImage = itemView.findViewById(R.id.image_goal_status);
            goalIndicatorLine = itemView.findViewById(R.id.goal_indicator_line); // << adicionar
        }

        public void bind(final Goal item, final OnGoalLongClickListener listener) {
            goalName.setText(item.getGoalName());
            goalDescription.setText(item.getGoalDescription());

            if (item.isCompleted()) {
                goalStatusImage.setVisibility(View.VISIBLE);
                goalStatusImage.setImageResource(R.drawable.ic_successful);
            } else {
                goalStatusImage.setVisibility(View.VISIBLE);
                goalStatusImage.setImageResource(R.drawable.ic_failure);
            }

            itemView.setOnLongClickListener(v -> {
                listener.onGoalLongClicked(item);
                return true;
            });        }
    }


    private static class GoalDiffCallback extends DiffUtil.ItemCallback<Goal> {
        @Override
        public boolean areItemsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {

            return oldItem.getGoalName().equals(newItem.getGoalName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {
            return oldItem.getGoalName().equals(newItem.getGoalName()) &&
                    oldItem.isCompleted() == newItem.isCompleted();
        }
    }
}