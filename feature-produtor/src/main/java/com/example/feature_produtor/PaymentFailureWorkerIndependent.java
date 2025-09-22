package com.example.feature_produtor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.feature_produtor.databinding.FragmentPaymentFailureWorkerIndependentBinding;

public class PaymentFailureWorkerIndependent extends Fragment {

    // A classe de binding gerada para o seu layout
    private FragmentPaymentFailureWorkerIndependentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando o View Binding
        binding = FragmentPaymentFailureWorkerIndependentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        View view = root;

        // Espera ~2.5s e volta para a tela de planos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Navigation.findNavController(view).navigate(R.id.PlanWorkerIndependent);

        }, 2500);

        return root;
    }

    // Importante: limpa a referência do binding para evitar vazamento de memória
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
