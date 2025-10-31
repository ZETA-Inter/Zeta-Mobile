package com.example.core.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.core.R;
import com.example.core.TipoUsuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Method;

public final class BrandingHelper {

    private BrandingHelper(){}

    public static TipoUsuario resolveTipo(Context ctx, TipoUsuario fallback) {
        // Prioriza argumento recebido; se nulo, tenta sessão
        if (fallback != null) return fallback;
        SharedPreferences sp = ctx.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String t = sp.getString("tipo_usuario", null);
        if (t == null) return null;
        try { return TipoUsuario.valueOf(t); } catch (Exception ignored) { return null; }
    }

    @StyleRes
    public static int themeOverlayFor(TipoUsuario tipo) {
        if (tipo == TipoUsuario.WORKER) return R.style.ThemeOverlay_App_Worker;
        return R.style.ThemeOverlay_App_Company;
    }

    /** Inflate com overlay para que TODOS os componentes já herdem as cores. */
    public static LayoutInflater themedInflater(Context base, LayoutInflater original, TipoUsuario tipo) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(base, themeOverlayFor(tipo));
        return original.cloneInContext(wrapper);
    }

    /** Aplica logo + tints em views já infladas (se precisar). */
    public static void applyBrandToViews(View root, TipoUsuario tipo, int logoImageViewId, int... buttonIds) {
        if (root == null || tipo == null) return;

        // Logo
        View logo = root.findViewById(logoImageViewId);
        if (logo instanceof android.widget.ImageView) {
            ((android.widget.ImageView) logo).setImageResource(
                    tipo == TipoUsuario.WORKER ? R.drawable.logo_worker : R.drawable.logo_company
            );
        }

        // Cores para tint manual (se necessário)
        int primary = root.getContext().getTheme().obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorPrimary}).getColor(0, 0);
        int onPrimary = root.getContext().getTheme().obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorOnPrimary}).getColor(0, 0);

        // Botões
        for (int id : buttonIds) {
            View v = root.findViewById(id);
            if (v instanceof MaterialButton) {
                MaterialButton b = (MaterialButton) v;
                b.setBackgroundTintList(ColorStateList.valueOf(primary));
                b.setTextColor(onPrimary);
            }
        }
    }

    /** Exemplo opcional: pintar borda dos TextInputLayout */
    public static void tintTextInputLayouts(ViewGroup parent, int strokeColor) {
        if (parent == null) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);
            if (v instanceof TextInputLayout) {
                ((TextInputLayout) v).setBoxStrokeColor(strokeColor);
            } else if (v instanceof ViewGroup) {
                tintTextInputLayouts((ViewGroup) v, strokeColor);
            }
        }
    }


    /** Cor “primária” por tipo, para usar no contador */
    public static int getCountdownColor(@NonNull Context ctx, @NonNull TipoUsuario tipo) {
        @ColorRes int res = (tipo == TipoUsuario.WORKER)
                ? R.color.worker_primary      // defina no colors.xml (ex.: azul/verde do Worker)
                : R.color.company_primary;    // defina no colors.xml (ex.: roxo/azul da Company)
        return ContextCompat.getColor(ctx, res);
    }

    /**
     * Tenta aplicar a cor do brand em um “contador” circular, com vários fallbacks:
     * - Se for ProgressBar: setProgressTint / setIndeterminateTint
     * - Se for uma view custom: tenta métodos comuns via reflection (setRingColor / setProgressColor / setIndicatorColor)
     * - Por fim, tenta backgroundTint.
     */
    public static void tintCountdown(@NonNull View root,
                                     @NonNull TipoUsuario tipo,
                                     @IdRes int progressViewId) {
        View v = root.findViewById(progressViewId);
        if (v == null) return;

        int color = getCountdownColor(root.getContext(), tipo);

        // ProgressBar padrão (circular)
        if (v instanceof ProgressBar) {
            ColorStateList tint = ColorStateList.valueOf(color);
            ProgressBar pb = (ProgressBar) v;
            try { pb.setProgressTintList(tint); } catch (Throwable ignored) {}
            try { pb.setSecondaryProgressTintList(tint); } catch (Throwable ignored) {}
            try { pb.setIndeterminateTintList(tint); } catch (Throwable ignored) {}
            return;
        }

        // View custom — tenta métodos comuns por reflection
        String[] methods = new String[]{"setRingColor", "setProgressColor", "setIndicatorColor"};
        for (String m : methods) {
            try {
                Method method = v.getClass().getMethod(m, int.class);
                method.invoke(v, color);
                return;
            } catch (Throwable ignored) {}
        }

        // Último fallback: backgroundTint
        try {
            ViewCompat.setBackgroundTintList(v, ColorStateList.valueOf(color));
        } catch (Throwable ignored) {}
    }
}
