package com.example.core;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import com.example.core.databinding.FragmentRegisterBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Register extends Fragment {


    private FragmentRegisterBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando View Binding
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        configurarJaPossuiConta();

        // Configura o listener do botão
        binding.btnCadastrar.setOnClickListener(v -> validarOuIr(v));

        return root;
    }

    private void configurarJaPossuiConta() {
        String base = "Já possui uma conta? Entre com ela.";
        SpannableString ss = new SpannableString(base);
        int start = base.indexOf("Entre com ela.");
        int end = start + "Entre com ela.".length();

        // cor
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.error_red)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // clique -> LoginCompanyActivity
        ClickableSpan click = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent i = new Intent(requireContext(), Login.class);
                startActivity(i);
                requireActivity().finish();
            }
        };
        ss.setSpan(click, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvJaConta.setText(ss);
        binding.tvJaConta.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvJaConta.setHighlightColor(Color.TRANSPARENT);
    }

    private void validarOuIr(View v) {
        boolean ok = true;

        limparTodosErros();
        ocultarMensagem();

        if (isEmpty(binding.edtNome)) {
            marcarErro(binding.tilNome, "Informe seu nome");
            ok = false;
        }
        if (isEmpty(binding.edtEmail)) {
            marcarErro(binding.tilEmail, "Informe seu e-mail");
            ok = false;
        }
        if (isEmpty(binding.edtTelefone)) {
            marcarErro(binding.tilTelefone, "Informe seu telefone");
            ok = false;
        }
        if (isEmpty(binding.edtCnpj)) {
            marcarErro(binding.tilCnpj, "Informe seu CNPJ");
            ok = false;
        }
        if (isEmpty(binding.edtSenha)) {
            marcarErro(binding.tilSenha, "Informe sua senha");
            ok = false;
        }
        if (isEmpty(binding.edtConfirmar)) {
            marcarErro(binding.tilConfirmar, "Confirme sua senha");
            ok = false;
        }

        if (!ok) {
            mostrarMensagem("Preencha todos os campos.");
            return;
        }

        String s1 = binding.edtSenha.getText() != null ? binding.edtSenha.getText().toString() : "";
        String s2 = binding.edtConfirmar.getText() != null ? binding.edtConfirmar.getText().toString() : "";
        if (!s1.equals(s2)) {
            marcarErro(binding.tilConfirmar, "As senhas não coincidem");
            mostrarMensagem("Corrija os campos em vermelho.");
            return;
        }

        // Sucesso -> vai para PlanCompanyActivity
        //deeplink porque a tela é de outro módulo
        Uri deepLink = Uri.parse("app://WorkerIndependent/Plan");
        Navigation.findNavController(v).navigate(deepLink);
        //lógica de navigation component
    }

    private boolean isEmpty(TextInputEditText edt) {
        CharSequence t = edt.getText();
        return t == null || TextUtils.isEmpty(t.toString().trim());
    }

    private void marcarErro(TextInputLayout til, String msg) {
        int red = ContextCompat.getColor(requireContext(), R.color.error_red);
        til.setError(msg);
        til.setErrorIconDrawable(null);
        til.setBoxStrokeColor(red);
        try {
            til.setBoxStrokeWidth(2);
            til.setBoxStrokeWidthFocused(2);
        } catch (Exception ignored) {
        }
    }

    private void limparTodosErros() {
        limparErro(binding.tilNome);
        limparErro(binding.tilEmail);
        limparErro(binding.tilTelefone);
        limparErro(binding.tilCnpj);
        limparErro(binding.tilSenha);
        limparErro(binding.tilConfirmar);
    }

    private void limparErro(TextInputLayout til) {
        til.setError(null);
        til.setErrorEnabled(false);
    }

    private void mostrarMensagem(String s) {
        binding.tvFormMsg.setText(s);
        binding.tvFormMsg.setVisibility(View.VISIBLE);
    }

    private void ocultarMensagem() {
        binding.tvFormMsg.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Anula a referência do binding para evitar vazamento de memória
        binding = null;
    }
}