package com.example.core;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.core.SendEmail;
import com.example.core.databinding.FragmentForgetPasswordBinding;
import com.google.android.material.textfield.TextInputLayout;

public class ForgetPassword extends Fragment {

    private FragmentForgetPasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando View Binding
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configura o listener do botão
        binding.btnEnviarEmail.setOnClickListener(v -> validarOuIr());

        return root;
    }

    private void validarOuIr() {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

        limparErro(binding.tilEmail);
        ocultarMensagem();

        if (TextUtils.isEmpty(email)) {
            marcarErro(binding.tilEmail, "Informe seu e-mail");
            mostrarMensagem("O campo deve ser preenchido.");
            return;
        }

        // OK -> próxima tela (envio de e-mail)
        Intent i = new Intent(requireContext(), SendEmail.class);
        startActivity(i);
        requireActivity().finish();
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
        binding = null;
    }
}