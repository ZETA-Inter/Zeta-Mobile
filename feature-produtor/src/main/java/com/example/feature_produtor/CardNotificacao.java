package com.example.feature_produtor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class CardNotificacao extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o seu XML de CardView (que chamamos de dialog_notification.xml)
        return inflater.inflate(R.layout.fragment_card_notificacao, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, com.google.android.material.R.style.Theme_AppCompat_Dialog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // OPCIONAL: Fechar automaticamente após 3 segundos
        view.postDelayed(this::dismiss, 3000); // Fecha após 3 segundos
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {

            // 1. Definir o Tamanho (Largura e Altura)
            int width ;
            int height ;

            int fixedWidthDp = 380;
            int fixedHeight = 100;
            width = (int) (fixedWidthDp * getResources().getDisplayMetrics().density);
            height = (int) (fixedHeight * getResources().getDisplayMetrics().density);

            getDialog().getWindow().setLayout(width, height);

            // 2. Definir a Posição (TOPO e Margem)
            getDialog().getWindow().setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);

            // Adicionar margem superior (16dp)
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            int marginDp = 16;
            int marginTopPx = (int) (marginDp * getResources().getDisplayMetrics().density);

            params.y = marginTopPx; // Define o deslocamento Y a partir do topo

            getDialog().getWindow().setAttributes(params);
        }
    }


}
