package com.example.feature_produtor;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.feature_produtor.R; // Ajuste para o seu R.java

public class ProcessingDialogFragment extends DialogFragment {

    private static final String ARG_QUERY = "query";
    private TextView messageTextView;

    public static ProcessingDialogFragment newInstance(String query) {
        ProcessingDialogFragment fragment = new ProcessingDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usa um tema de tela cheia ou o que mais se encaixa no seu design
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_YourApp_FullScreenDialog);
        // Você precisará criar este tema (veja a seção de Layout/Estilo).
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Você precisará de um layout XML simples para este Diálogo
        View view = inflater.inflate(R.layout.dialog_loading_simple, container, false);
        messageTextView = view.findViewById(R.id.loading_message);

        // Exibe a query que o usuário digitou
        if (getArguments() != null) {
            String query = getArguments().getString(ARG_QUERY, "Pesquisando...");
            messageTextView.setText("Processando pesquisa para: " + query + "...");
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Garante que ocupe a largura total
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // Torna o fundo transparente (se necessário)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            // Deixa a área acima do teclado mais visível/focada
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    // Você pode adicionar um método de cancelamento aqui que o LessonsWorker irá chamar
    public interface OnCancelListener {
        void onProcessingDialogCancel();
    }
}
