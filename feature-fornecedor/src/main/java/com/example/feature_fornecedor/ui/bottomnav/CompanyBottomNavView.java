package com.example.feature_fornecedor.ui.bottomnav;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.view.ViewCompat;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.feature_fornecedor.R;
import com.google.android.material.card.MaterialCardView;

public class CompanyBottomNavView extends MaterialCardView {

    public interface OnItemSelected {
        void onItemSelected(Item item);
    }

    public enum Item { AWARDS, HOME, TEAM }

    private MaterialCardView btnAwards, btnTeam;
    private MaterialCardView btnHome;
    private OnItemSelected listener;

    public CompanyBottomNavView(Context c) { this(c, null); }
    public CompanyBottomNavView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }

    private void init() {
        inflate(getContext(), R.layout.view_bottom_nav_company, this);

        btnAwards = findViewById(R.id.btnAwards);
        btnTeam   = findViewById(R.id.btnTeam);
        btnHome   = findViewById(R.id.btnHome);

        // estado inicial: Home "selecionado" visualmente (círculo azul já no layout)
        select(btnHome, true);

        btnAwards.setOnClickListener(v -> { animateTap(v); select(btnAwards, true); select(btnTeam, false); onSelect(Item.AWARDS); });
        btnHome.setOnClickListener(v -> { animateTap(v); select(btnAwards, false); select(btnTeam, false); onSelect(Item.HOME); });
        btnTeam.setOnClickListener(v -> { animateTap(v); select(btnTeam, true); select(btnAwards, false); onSelect(Item.TEAM); });
    }

    private void onSelect(Item item) { if (listener != null) listener.onItemSelected(item); }

    private void animateTap(View v) {
        v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(90)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(110).start())
                .start();
    }

    /** define o “chip” branco com ícone azul quando selecionado */
    private void select(MaterialCardView card, boolean selected) {
        card.setCardBackgroundColor(selected ? getColorPrimary() : 0x00000000);

        View icon = (card.getChildCount() > 0 ? card.getChildAt(0) : null);
        if (icon instanceof ImageView) {
            ((ImageView) icon).setColorFilter(
                    selected ? getColorOnPrimary() : getColorOnSurface()
            );
        }
        card.setSelected(selected);
    }


    private int getColorPrimary()   { return resolveAttr(android.R.attr.colorAccent, "appColorPrimary"); }
    private int getColorOnPrimary() { return resolveAttr(android.R.attr.textColorPrimary, "appColorOnPrimary"); }
    private int getColorOnSurface() { return getResources().getColor(android.R.color.black); }

    private int resolveAttr(int fallback, String attrName) {
        int id = getResources().getIdentifier(attrName, "attr", getContext().getPackageName());
        if (id == 0) return getResources().getColor(android.R.color.black);
        final android.util.TypedValue tv = new android.util.TypedValue();
        getContext().getTheme().resolveAttribute(id, tv, true);
        return tv.data;
    }

    public void setOnItemSelected(OnItemSelected l) { this.listener = l; }
}
