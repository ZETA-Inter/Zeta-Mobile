package com.example.feature_fornecedor.ranking;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.feature_fornecedor.R;
import com.example.feature_fornecedor.ranking.RankingEntry;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.VH> {

    private final List<RankingEntry> items = new ArrayList<>();
    private int startPosition = 4; // fallback quando não vier 'position' nos itens

    public void submit(List<RankingEntry> data, int startPos) {
        startPosition = startPos;
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking_row, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        RankingEntry e = items.get(position);

        int ordinal = (e.position != null && e.position > 0)
                ? e.position
                : (startPosition + position);

        h.tvPos.setText(ordinal + "º");
        h.tvName.setText(e.name != null ? e.name : "—");

        int pts = e.points != null ? e.points : 0;
        h.tvScore.setText(String.valueOf(pts));
        h.tvSub.setText(pts + " pts");

        Glide.with(h.avatar.getContext())
                .load(e.photo)
                .placeholder(R.drawable.perfil)
                .error(R.drawable.perfil)
                .into(h.avatar);

        int alt = ContextCompat.getColor(h.itemView.getContext(), R.color.bg_light);
        h.itemView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : alt);
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvPos, tvName, tvSub, tvScore;
        ShapeableImageView avatar;
        VH(@NonNull View v) {
            super(v);
            tvPos   = v.findViewById(R.id.tvPos);
            tvName  = v.findViewById(R.id.tvName);
            tvSub   = v.findViewById(R.id.tvSub);
            tvScore = v.findViewById(R.id.tvScore);
            avatar  = v.findViewById(R.id.imgAvatar);
        }
    }
}
