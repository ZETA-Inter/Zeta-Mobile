package com.example.core;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.core.ui.BrandingHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Register extends Fragment {

    private FragmentRegisterBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Repository repo = new Repository(); // mantido
    private TipoUsuario tipoAtual;

    // guardamos o último watcher para remover ao alternar
    private android.text.TextWatcher docMaskWatcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1) Resolve o tipo (Bundle → sessão)
        Bundle args = getArguments();
        TipoUsuario fromBundle = (args != null)
                ? (TipoUsuario) args.getSerializable("TIPO_USUARIO")
                : null;
        tipoAtual = BrandingHelper.resolveTipo(requireContext(), fromBundle);
        if (tipoAtual == null) tipoAtual = TipoUsuario.COMPANY; // fallback

        // 2) Inflar com Theme Overlay do tipo
        LayoutInflater themed = BrandingHelper.themedInflater(requireContext(), inflater, tipoAtual);
        binding = FragmentRegisterBinding.inflate(themed, container, false);
        View root = binding.getRoot();

        // 3) Branding visual (logo/botão)
        BrandingHelper.applyBrandToViews(
                root,
                tipoAtual,
                R.id.imgLogo,
                R.id.btnCadastrar
        );

        // 4) Agora SIM: aplicar máscara após binding existir
        configureDocumentoField(tipoAtual);

        // 5) CTA "Já possui conta?"
        configurarJaPossuiConta();

        // 6) Clique do Cadastrar
        binding.btnCadastrar.setOnClickListener(v -> {
            limparTodosErros();
            ocultarMensagem();

            String nome = get((TextInputEditText) binding.tilNome.getEditText());
            String email = get((TextInputEditText) binding.tilEmail.getEditText());
            String telefone = get((TextInputEditText) binding.tilTelefone.getEditText());
            String senha = get((TextInputEditText) binding.tilSenha.getEditText());
            String confirmarSenha = get((TextInputEditText) binding.tilConfirmar.getEditText());
            String documento = get((TextInputEditText) binding.tilCnpj.getEditText()); // vem mascarado

            String campoDocumento = (tipoAtual == TipoUsuario.WORKER) ? "CPF" : "CNPJ";

            if (!validarCampos(nome, email, telefone, documento, senha, confirmarSenha, campoDocumento)) {
                mostrarMensagem("Preencha todos os campos corretamente.");
                return;
            }

            // normaliza documento sem máscara para backend
            String documentoRaw = documento.replaceAll("\\D+", "");

            Map<String, Object> dadosUsuario = new HashMap<>();
            dadosUsuario.put("Nome", nome);
            dadosUsuario.put("Email", email);
            dadosUsuario.put("Telefone", telefone);
            dadosUsuario.put("Senha", senha);
            dadosUsuario.put(campoDocumento, documentoRaw);

            navegarParaPlanos(dadosUsuario, tipoAtual, nome, email, v);
        });

        return root;
    }

    private void navegarParaPlanos(Map<String, Object> dadosUsuario,
                                   TipoUsuario tipo,
                                   String nome,
                                   String email,
                                   View clickView) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("dadosUsuario", (Serializable) dadosUsuario);
        bundle.putSerializable("TIPO_USUARIO", tipo);
        bundle.putString("Nome", nome);
        bundle.putString("Email", email);
        Navigation.findNavController(clickView).navigate(R.id.Plan, bundle);
    }

    private void configurarJaPossuiConta() {
        String base = "Já possui uma conta? Entre com ela.";
        SpannableString ss = new SpannableString(base);
        int start = base.indexOf("Entre com ela.");
        int end = start + "Entre com ela.".length();

        ss.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(requireContext(), com.example.core.R.color.error_red)
                ),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        ClickableSpan click = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("TIPO_USUARIO", tipoAtual);
                Navigation.findNavController(widget).navigate(R.id.Login, bundle);
            }
        };
        ss.setSpan(click, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvJaConta.setText(ss);
        binding.tvJaConta.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvJaConta.setHighlightColor(Color.TRANSPARENT);
    }

    // ===== Validação =====
    private boolean validarCampos(String nome,
                                  String email,
                                  String fone,
                                  String documento, // mascarado
                                  String senha,
                                  String confirmarSenha,
                                  String tipoDocumento) {
        boolean ok = true;

        if (TextUtils.isEmpty(nome)) { error(binding.tilNome, "Informe seu nome"); ok = false; }

        if (TextUtils.isEmpty(email)) { error(binding.tilEmail, "Informe seu e-mail"); ok = false; }
        else if (!Validators.isValidEmail(email)) { error(binding.tilEmail, "E-mail inválido"); ok = false; }

        if (TextUtils.isEmpty(fone)) { error(binding.tilTelefone, "Informe seu telefone"); ok = false; }
        else if (!Validators.isValidPhone(fone)) { error(binding.tilTelefone, "Telefone inválido"); ok = false; }

        if (TextUtils.isEmpty(documento)) {
            error(binding.tilCnpj, "Informe seu " + tipoDocumento);
            ok = false;
        } else if (tipoDocumento.equals("CNPJ") && !Validators.isValidCNPJ(documento)) {
            error(binding.tilCnpj, "CNPJ inválido");
            ok = false;
        } else if (tipoDocumento.equals("CPF") && !Validators.isValidCPF(documento)) {
            error(binding.tilCnpj, "CPF inválido");
            ok = false;
        }

        if (TextUtils.isEmpty(senha)) { error(binding.tilSenha, "Informe sua senha"); ok = false; }
        else if (!Validators.isStrongPassword(senha)) { error(binding.tilSenha, "Mín. 6, com letra e número"); ok = false; }

        if (TextUtils.isEmpty(confirmarSenha)) { error(binding.tilConfirmar, "Confirme sua senha"); ok = false; }
        else if (!senha.equals(confirmarSenha)) { error(binding.tilConfirmar, "As senhas não coincidem"); ok = false; }

        return ok;
    }

    // ===== Aplicação/alternância da máscara (agora funcional) =====

    private void configureDocumentoField(@Nullable TipoUsuario tipo) {
        if (binding == null) return;

        // 1) Label externo acima do campo
        if (binding.lblCnpj != null) {
            binding.lblCnpj.setText(tipo == TipoUsuario.WORKER ? "CPF" : "CNPJ");
        }

        // 2) Desliga label flutuante do TextInputLayout
        binding.tilCnpj.setHintEnabled(false);
        binding.tilCnpj.setHelperTextEnabled(false); // sem texto embaixo

        // 3) Placeholder e máscara no EditText
        TextInputEditText et = binding.edtCnpj;
        if (et == null) return;

        // Remove máscara anterior
        if (docMaskWatcher != null) {
            et.removeTextChangedListener(docMaskWatcher);
            docMaskWatcher = null;
        }
        et.setText("");

        if (tipo == TipoUsuario.WORKER) {
            et.setHint("Digite seu CPF");
            //MaskUtils.applyMaxDigits(et, 14);
            docMaskWatcher = new MaskUtils.SimpleMaskTextWatcher(et, "###.###.###-##", 11);
            et.addTextChangedListener(docMaskWatcher);
        } else {
            et.setHint("Digite seu CNPJ");
            //MaskUtils.applyMaxDigits(et, 18);
            docMaskWatcher = new MaskUtils.SimpleMaskTextWatcher(et, "##.###.###/####-##", 14);
            et.addTextChangedListener(docMaskWatcher);
        }
    }



    // ===== Helpers UI =====
    private String get(TextInputEditText e) {
        return (e == null || e.getText() == null) ? "" : e.getText().toString().trim();
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
        if (til == null) return;
        til.setError(null);
        til.setErrorEnabled(false);
    }

    private void mostrarMensagem(String s) {
        if (binding != null && binding.tvFormMsg != null) {
            binding.tvFormMsg.setText(s);
            binding.tvFormMsg.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarMensagem() {
        if (binding != null && binding.tvFormMsg != null) {
            binding.tvFormMsg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // evita vazamento de watcher
        if (binding != null && binding.tilCnpj != null) {
            TextInputEditText et = (TextInputEditText) binding.tilCnpj.getEditText();
            if (et != null && docMaskWatcher != null) et.removeTextChangedListener(docMaskWatcher);
            docMaskWatcher = null;
        }
        binding = null;
    }
}
