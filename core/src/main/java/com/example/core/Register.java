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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.example.core.Repository; // Assegure-se de ter esta classe acessível

import java.net.URL;

public class Register extends Fragment {

    private FragmentRegisterBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Repository repo = new Repository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        configurarJaPossuiConta();
        binding.btnCadastrar.setOnClickListener(v -> cadastrar());

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

        // clique -> navega para a tela de login
        ClickableSpan click = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Navigation.findNavController(requireView()).navigate(R.id.Login);
            }
        };
        ss.setSpan(click, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvJaConta.setText(ss);
        binding.tvJaConta.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvJaConta.setHighlightColor(Color.TRANSPARENT);
    }

    private void cadastrar() {
        limparTodosErros();
        ocultarMensagem();

        String nome = get(binding.edtNome);
        String email = get(binding.edtEmail);
        String fone = get(binding.edtTelefone);
        String cnpj = get(binding.edtCnpj);
        String senha = get(binding.edtSenha);
        String confirmarSenha = get(binding.edtConfirmar);

        if (!validarCampos(nome, email, fone, cnpj, senha, confirmarSenha)) {
            mostrarMensagem("Corrija os campos em vermelho.");
            return;
        }

        // 1) Cria no Auth
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(res -> {
                    // 2) Atualiza displayName no Auth
                    res.getUser().updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(nome).build()
                    );
                    // 3) Upsert no Firestore com createdAt/lastLoginAt + cnpj/phone
                    repo.upsertFromAuth(res.getUser(), null)
                        //    .onSuccessTask(aVoid -> repo.updateContact(res.getUser().getUid(), cnpj, fone))
                            .addOnSuccessListener(v -> {
                                // 4) Próxima tela
                                Uri deepLink = Uri.parse("app://WorkerIndependent/Plan");
                                Navigation.findNavController(requireView()).navigate(deepLink);
                            })
                            .addOnFailureListener(e -> error(binding.tilEmail, "Falha ao salvar dados. Tente novamente."));
                })
                .addOnFailureListener(e -> error(binding.tilEmail, mapAuthCreateError(e)));
    }

    private boolean validarCampos(String nome, String email, String fone, String cnpj, String senha, String confirmarSenha) {
        boolean ok = true;

        if (TextUtils.isEmpty(nome)) { error(binding.tilNome, "Informe seu nome"); ok = false; }
        if (TextUtils.isEmpty(email)) { error(binding.tilEmail, "Informe seu e-mail"); ok = false; }
        else if (!Validators.isValidEmail(email)) { error(binding.tilEmail, "E-mail inválido"); ok = false; }
        if (TextUtils.isEmpty(fone)) { error(binding.tilTelefone, "Informe seu telefone"); ok = false; }
        else if (!Validators.isValidPhone(fone)) { error(binding.tilTelefone, "Telefone inválido"); ok = false; }
        if (TextUtils.isEmpty(cnpj)) { error(binding.tilCnpj, "Informe seu CNPJ"); ok = false; }
        else if (!Validators.isValidCNPJ(cnpj)) { error(binding.tilCnpj, "CNPJ inválido"); ok = false; }
        if (TextUtils.isEmpty(senha)) { error(binding.tilSenha, "Informe sua senha"); ok = false; }
        else if (!Validators.isStrongPassword(senha)) { error(binding.tilSenha, "Mín. 6, com letra e número"); ok = false; }
        if (TextUtils.isEmpty(confirmarSenha)) { error(binding.tilConfirmar, "Confirme sua senha"); ok = false; }
        else if (!senha.equals(confirmarSenha)) { error(binding.tilConfirmar, "As senhas não coincidem"); ok = false; }

        return ok;
    }

    private String get(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void clearErrors() {
        for (TextInputLayout til : new TextInputLayout[]{
                binding.tilNome, binding.tilEmail, binding.tilTelefone,
                binding.tilCnpj, binding.tilSenha, binding.tilConfirmar}) {
            if (til != null) { til.setError(null); }
        }
    }

    private void error(TextInputLayout til, String msg) {
        if (til == null) return;
        til.setError(msg);
        til.setErrorIconDrawable(null);
        int red = ContextCompat.getColor(requireContext(), com.example.core.R.color.error_red);
        til.setBoxStrokeColor(red);
        try {
            til.setBoxStrokeWidth(2);
            til.setBoxStrokeWidthFocused(2);
        } catch (Exception ignored) {}
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

    private String mapAuthCreateError(Exception e) {
        String m = e.getMessage() == null ? "" : e.getMessage();
        if (m.contains("email address is already in use")) return "E-mail já cadastrado";
        if (m.contains("WEAK_PASSWORD")) return "Senha fraca (mín. 6)";
        if (m.contains("INVALID_EMAIL")) return "E-mail inválido";
        return "Não foi possível criar sua conta";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}