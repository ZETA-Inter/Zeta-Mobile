package com.example.feature_fornecedor.ui.bottomnav;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;

import com.example.feature_fornecedor.R;

public class CompanyBottomNavView extends FrameLayout {

    public enum Item { AWARDS, HOME, TEAM }

    private ImageButton iconAwards, iconHome, iconTeam;
    private View selectorBg;      // círculo azul móvel
    private ImageView whiteNotch; // contorno branco

    @Nullable private NavController navController;
    private int destAwards, destHome, destTeam;

    private final AccelerateDecelerateInterpolator ease = new AccelerateDecelerateInterpolator();
    private static final long MOVE_MS = 240L;

    // Quanto “sobe” o selecionado (círculo + ícone). Ajuste aqui.
    private static final float FLOAT_Y_DP = 6f;

    public CompanyBottomNavView(Context c) { this(c, null); }
    public CompanyBottomNavView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        setClipToPadding(false);
        setClipChildren(false);

        inflate(getContext(), R.layout.view_bottom_nav_company, this);

        View root = getChildCount() > 0 ? getChildAt(0) : null;
        if (root instanceof ViewGroup) {
            ((ViewGroup) root).setClipToPadding(false);
            ((ViewGroup) root).setClipChildren(false);
        }

        iconAwards = findViewById(R.id.navTrophy);
        iconHome   = findViewById(R.id.navHome);
        iconTeam   = findViewById(R.id.navPeople);
        selectorBg = findViewById(R.id.selectorBg);
        whiteNotch = findViewById(R.id.whiteNotch);

        iconAwards.setOnClickListener(v -> animateTo(Item.AWARDS, true));
        iconHome.setOnClickListener(v   -> animateTo(Item.HOME,   true));
        iconTeam.setOnClickListener(v   -> animateTo(Item.TEAM,   true));

        // Ajusta insets (barra de navegação do telefone)
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            final int bottom = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            ).bottom;
            ViewGroup.LayoutParams lp = getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                if (mlp.bottomMargin != bottom) {
                    mlp.bottomMargin = bottom;
                    setLayoutParams(mlp);
                }
            }
            return insets;
        });
    }

    public void bindNavController(NavController nav, int awardsDestId, int homeDestId, int teamDestId) {
        this.navController = nav;
        this.destAwards = awardsDestId;
        this.destHome   = homeDestId;
        this.destTeam   = teamDestId;
    }

    public void setCurrentItem(Item item, boolean animate) {
        if (animate) {
            animateTo(item, false);
        } else {
            // centraliza o círculo no ícone + aplica offset vertical (subida)
            float[] d = computeDeltaToIconCenter(item, -dp(FLOAT_Y_DP)); // dy extra negativo (sobe)
            selectorBg.setTranslationX(selectorBg.getTranslationX() + d[0]);
            selectorBg.setTranslationY(selectorBg.getTranslationY() + d[1]);
            if (whiteNotch != null) whiteNotch.setTranslationX(whiteNotch.getTranslationX() + d[0]);

            // ícones: só o selecionado sobe, os demais voltam
            float oy = -dp(FLOAT_Y_DP);
            iconAwards.setTranslationY(item == Item.AWARDS ? oy : 0f);
            iconHome.setTranslationY(item == Item.HOME ? oy : 0f);
            iconTeam.setTranslationY(item == Item.TEAM ? oy : 0f);
        }
    }

    private void animateTo(Item item, boolean navigateAfter) {
        post(() -> {
            float oy = -dp(FLOAT_Y_DP);
            float[] d = computeDeltaToIconCenter(item, oy); // move o círculo para o centro do ícone + offset

            // Círculo azul
            selectorBg.animate()
                    .translationX(selectorBg.getTranslationX() + d[0])
                    .translationY(selectorBg.getTranslationY() + d[1])
                    .setInterpolator(ease)
                    .setDuration(MOVE_MS)
                    .withEndAction(() -> {
                        if (whiteNotch != null) whiteNotch.setTranslationX(whiteNotch.getTranslationX() + d[0]);
                        if (navigateAfter) navigate(item);
                    })
                    .start();

            // Contorno branco acompanha apenas em X
            if (whiteNotch != null) {
                whiteNotch.animate()
                        .translationX(whiteNotch.getTranslationX() + d[0])
                        .setInterpolator(ease)
                        .setDuration(MOVE_MS)
                        .start();
            }

            // Ícones: selecionado sobe, demais descem para 0
            ImageButton sel = (item == Item.AWARDS) ? iconAwards : (item == Item.TEAM) ? iconTeam : iconHome;
            ImageButton a = iconAwards, h = iconHome, t = iconTeam;

            a.animate().translationY(item == Item.AWARDS ? oy : 0f).setDuration(MOVE_MS).setInterpolator(ease).start();
            h.animate().translationY(item == Item.HOME   ? oy : 0f).setDuration(MOVE_MS).setInterpolator(ease).start();
            t.animate().translationY(item == Item.TEAM   ? oy : 0f).setDuration(MOVE_MS).setInterpolator(ease).start();

            // micro realce de escala no selecionado (opcional)
            float sSel = 1.12f;
            float sOff = 1f;
            a.animate().scaleX(item == Item.AWARDS ? sSel : sOff).scaleY(item == Item.AWARDS ? sSel : sOff).setDuration(120).start();
            h.animate().scaleX(item == Item.HOME   ? sSel : sOff).scaleY(item == Item.HOME   ? sSel : sOff).setDuration(120).start();
            t.animate().scaleX(item == Item.TEAM   ? sSel : sOff).scaleY(item == Item.TEAM   ? sSel : sOff).setDuration(120).start();
        });
    }

    private void navigate(Item item) {
        if (navController == null) return;
        int dest = (item == Item.AWARDS) ? destAwards : (item == Item.TEAM) ? destTeam : destHome;
        if (dest == 0) return;
        try {
            if (navController.getCurrentDestination() == null
                    || navController.getCurrentDestination().getId() != dest) {
                navController.navigate(dest);
            }
        } catch (Exception ignore) {}
    }

    /**
     * Calcula quanto o círculo precisa mover (dx, dy) para ficar
     * centralizado no ícone alvo, somando um offset vertical extra (oy).
     */
    private float[] computeDeltaToIconCenter(Item item, float extraYOffsetPx) {
        View icon = (item == Item.AWARDS) ? iconAwards : (item == Item.TEAM) ? iconTeam : iconHome;
        float[] iconCenter = centerInParent(icon);
        float[] selCenter  = centerInParent(selectorBg);

        // destino do centro do círculo = centro do ícone + offset Y extra (subida)
        float targetCx = iconCenter[0];
        float targetCy = iconCenter[1] + extraYOffsetPx;

        float dx = targetCx - selCenter[0];
        float dy = targetCy - selCenter[1];
        return new float[]{dx, dy};
    }

    private float[] centerInParent(View v) {
        int[] vLoc = new int[2];
        int[] pLoc = new int[2];
        v.getLocationOnScreen(vLoc);
        this.getLocationOnScreen(pLoc);
        float cx = (vLoc[0] - pLoc[0]) + v.getWidth()  / 2f;
        float cy = (vLoc[1] - pLoc[1]) + v.getHeight() / 2f;
        return new float[]{cx, cy};
    }

    private float dp(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
