package com.example.feature_produtor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.feature_produtor.R;
import com.example.core.model.mongo.Activity.Question.Answer;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.Objects;

public class AnswerAdapter extends ListAdapter<Answer, AnswerAdapter.AnswerViewHolder> {

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(Answer selectedAnswer, int position);
    }

    private final OnAnswerSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public AnswerAdapter(OnAnswerSelectedListener listener) {
        super(new AnswerDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_answer, parent, false);
        return new AnswerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        Answer currentAnswer = getItem(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(currentAnswer, listener, isSelected, position);
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;

        // Notifica o item antigo e o novo para atualização visual
        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    public Answer getSelectedAnswer() {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            return getItem(selectedPosition);
        }
        return null;
    }

    public static class AnswerViewHolder extends RecyclerView.ViewHolder {
        private final MaterialRadioButton radioButton;

        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencia o RadioButton do layout item_activity_answer.xml
            radioButton = (MaterialRadioButton) itemView;
        }

        public void bind(final Answer item, final OnAnswerSelectedListener listener, boolean isSelected, final int position) {
            radioButton.setText(item.getAnswer());
            radioButton.setChecked(isSelected);


            View.OnClickListener clickListener = v -> {
                listener.onAnswerSelected(item, position);
            };

            radioButton.setOnClickListener(clickListener);
            itemView.setOnClickListener(clickListener);
        }
    }

    private static class AnswerDiffCallback extends DiffUtil.ItemCallback<Answer> {
        @Override
        public boolean areItemsTheSame(@NonNull Answer oldItem, @NonNull Answer newItem) {
            // Assume que a resposta não muda (usa o texto como identificador se não houver ID)
            return Objects.equals(oldItem.getAnswer(), newItem.getAnswer());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Answer oldItem, @NonNull Answer newItem) {
            return oldItem.getAnswer().equals(newItem.getAnswer()) && oldItem.isCorrect() == newItem.isCorrect();
        }
    }
}