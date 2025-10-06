package com.example.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.feature_produtor.databinding.FragmentPlanWorkerIndependentBinding;

public class Plan extends Fragment {

    private FragmentPlanWorkerIndependentBinding binding;

    // 0 = nenhum, 1 = basic (FREE), 2 = plus (PAGO)
    private int selected = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentPlanWorkerIndependentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configura os listeners dos cards
        binding.cardBasic.setOnClickListener(v -> selectPlan(1));
        binding.cardPlus.setOnClickListener(v -> selectPlan(2));

        // Configura o listener do botão
        binding.btnContinuar.setOnClickListener(v -> continuar(v));

        return root;
    }

    private void selectPlan(int which) {
        // Use a classe R do projeto para acessar as cores
        int primary = ContextCompat.getColor(requireContext(), R.color.primary_dark);
        int transparent = ContextCompat.getColor(requireContext(), android.R.color.transparent);

        hideError();

        // Limpa as bordas de seleção
        binding.cardBasic.setStrokeColor(transparent);
        binding.cardPlus.setStrokeColor(transparent);

        if (which == 1) {
            binding.cardBasic.setStrokeColor(primary);  // Destaca o plano selecionado
            selected = 1;
        } else {
            binding.cardPlus.setStrokeColor(primary);
            selected = 2;
        }
    }

    private void continuar(View v) {
        if (selected == 0) {
            // Erro: nada selecionado
            showError("Escolha um plano para continuar.");
            int red = ContextCompat.getColor(requireContext(), R.color.error_red);
            binding.cardBasic.setStrokeColor(red);
            binding.cardPlus.setStrokeColor(red);
            return;
        }

        if (selected == 1) {
            // Plano FREE -> vai para a home
            Navigation.findNavController(v).navigate(R.id.HomePageWorker);
        } else {
            // Plano PAGO -> vai para a tela de pagamento
            Navigation.findNavController(v).navigate(R.id.PaymentWorkerIndependent);
        }
    }

    private void showError(String msg) {
        binding.tvPlansError.setText(msg);
        binding.tvPlansError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvPlansError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}