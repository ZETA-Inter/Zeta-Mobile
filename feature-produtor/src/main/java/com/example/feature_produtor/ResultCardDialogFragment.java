package com.example.feature_produtor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ResultCardDialogFragment extends DialogFragment {

    private static final String ARG_CURSO = "curso_nome";
    private static final String ARG_ACERTOS = "acertos_count";
    private static final String ARG_ERROS = "erros_count";


    public static ResultCardDialogFragment newInstance(String curso, int acertos, int erros) {
        ResultCardDialogFragment fragment = new ResultCardDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CURSO, curso);
        args.putInt(ARG_ACERTOS, acertos);
        args.putInt(ARG_ERROS, erros);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // infla o layout do diálogo (R.layout.dialog_result_card)
        View view = inflater.inflate(R.layout.dialog_result_card, container, false);

        TextView textCurso = view.findViewById(R.id.text_curso_nome);
        TextView textAcertos = view.findViewById(R.id.text_acertos);
        TextView textErros = view.findViewById(R.id.text_erros);
        View buttonClose = view.findViewById(R.id.button_close_dialog);

        Bundle args = getArguments();
        if (args != null) {
            textCurso.setText(args.getString(ARG_CURSO, "Curso Desconhecido"));
            textAcertos.setText(String.valueOf(args.getInt(ARG_ACERTOS, 0)));
            textErros.setText(String.valueOf(args.getInt(ARG_ERROS, 0)));
        }

        // Configura o botão de fechar (X)
        buttonClose.setOnClickListener(v -> {
            // Fecha o diálogo. A navegação será tratada no onDismiss.
            dismiss();
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Remove a barra de título padrão para usar o layout customizado
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Permite fechar clicando fora ou com o botão Voltar
        dialog.setCanceledOnTouchOutside(true);
        setCancelable(true);

        return dialog;
    }
}
