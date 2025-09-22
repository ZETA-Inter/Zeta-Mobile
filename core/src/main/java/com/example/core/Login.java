package com.example.core;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.databinding.FragmentLoginBinding;
import com.google.android.material.textfield.TextInputLayout;

public class Login extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando View Binding
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Define os listeners usando o objeto binding
        binding.btnEntrar.setOnClickListener(v -> validarOuProsseguir(v));
        binding.tvForgotPasswordCompany.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.ForgetPassword);
        });


        binding.tvCadastro.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.Register);

        });

        return root;
    }

    private void validarOuProsseguir(View v) {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";
        String senha = binding.edtSenha.getText() != null ? binding.edtSenha.getText().toString().trim() : "";

        boolean ok = true;

        // limpa estados anteriores
        limparErro(binding.tilEmail);
        limparErro(binding.tilSenha);
        ocultarMensagem();

        if (TextUtils.isEmpty(email)) {
            marcarErro(binding.tilEmail, "Informe seu e-mail");
            ok = false;
        }
        if (TextUtils.isEmpty(senha)) {
            marcarErro(binding.tilSenha, "Informe sua senha");
            ok = false;
        }

        if (!ok) {
            mostrarMensagem("Os campos devem ser preenchidos.");
            return;
        }
        //deeplink porque a tela é de outro módulo
        Uri deepLink = Uri.parse("app://Company/Home");
        Navigation.findNavController(v).navigate(deepLink);
        //lógica de navigation component
    }

    private void marcarErro(TextInputLayout til, String msg) {
        int red = ContextCompat.getColor(requireContext(), R.color.error_red);

        til.setError(msg);
        til.setErrorIconDrawable(null);

        til.setBoxStrokeColor(red);
        try {
            til.setBoxStrokeWidth(2);
            til.setBoxStrokeWidthFocused(2);
        } catch (Exception ignored) {}
    }

    private void limparErro(TextInputLayout til) {
        til.setError(null);
        til.setErrorEnabled(false);
    }

    private void mostrarMensagem(String s) {
        if (binding.tvFormMsg != null) {
            binding.tvFormMsg.setText(s);
            binding.tvFormMsg.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarMensagem() {
        if (binding.tvFormMsg != null) binding.tvFormMsg.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Anula a referência do binding para evitar vazamento de memória
        binding = null;
    }
}