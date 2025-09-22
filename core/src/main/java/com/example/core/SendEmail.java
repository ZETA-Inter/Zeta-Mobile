package com.example.core;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.databinding.FragmentSendEmailBinding;

public class SendEmail extends Fragment {


    private FragmentSendEmailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando o View Binding
        binding = FragmentSendEmailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configura o listener do botão
        binding.btnVoltarLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.Login);
        });

        return root;
    }

    // Importante: limpa a referência do binding para evitar vazamento de memória
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
