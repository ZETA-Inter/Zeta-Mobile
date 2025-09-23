package com.example.core;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.core.databinding.FragmentForgetPasswordBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends Fragment {

    private FragmentForgetPasswordBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnEnviarEmail.setOnClickListener(v -> validarEEnviarEmail(v));

        return root;
    }

    private void validarEEnviarEmail(View v) {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

        // Limpa mensagens de erro
        limparErro(binding.tilEmail);
        ocultarMensagem();

        // Valida se o campo de e-mail está vazio
        if (TextUtils.isEmpty(email)) {
            marcarErro(binding.tilEmail, "Informe seu e-mail");
            mostrarMensagem("O campo deve ser preenchido.");
            return;
        }

        // Valida o formato do e-mail (agora com a lógica aqui)
        if (!isValidEmail(email)) {
            marcarErro(binding.tilEmail, "E-mail inválido");
            mostrarMensagem("E-mail inválido.");
            return;
        }

        // Envia o e-mail de recuperação de senha
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    // Sucesso: navega para a tela de confirmação
                    Navigation.findNavController(v).navigate(R.id.SendEmail);
                })
                .addOnFailureListener(e -> {
                    // Falha: exibe uma mensagem de erro
                    marcarErro(binding.tilEmail, "Não foi possível enviar o e-mail. Verifique a conexão ou se o e-mail existe.");
                    mostrarMensagem("Não foi possível enviar o e-mail.");
                });
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void marcarErro(TextInputLayout til, String msg) {
        int red = ContextCompat.getColor(requireContext(), com.example.core.R.color.error_red);
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
        int defaultColor = ContextCompat.getColor(requireContext(), com.example.core.R.color.primary_dark);
        til.setBoxStrokeColor(defaultColor);
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