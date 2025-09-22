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

import com.example.feature_produtor.databinding.FragmentPaymentSuccessfulWorkerIndependentBinding;

import com.example.feature_produtor.databinding.FragmentPaymentSuccessfulWorkerIndependentBinding;

public class PaymentSuccessfulWorkerIndependent extends Fragment {

    private FragmentPaymentSuccessfulWorkerIndependentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando o View Binding
        binding = FragmentPaymentSuccessfulWorkerIndependentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        View view = root;

        // espera ~2s e vai para a Home da empresa
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Navigation.findNavController(view).navigate(R.id.HomePageWorker);
        }, 2000);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Importante: limpa a referência do binding para evitar memory leaks
        binding = null;
    }
}
