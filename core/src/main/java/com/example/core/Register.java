package com.example.core;

import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.adapter.AuthAdapter;
import com.example.core.databinding.FragmentRegisterBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;

// OBS: Assumindo que você tem a classe Validators e R.color.error_red acessíveis.

public class Register extends Fragment {

    private FragmentRegisterBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Repository repo = new Repository(); // Repository não está sendo usado, mas mantido.
    private TipoUsuario tipoAtual;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle bundle = getArguments();

        if (bundle != null) {
            tipoAtual = (TipoUsuario) bundle.getSerializable("TIPO_USUARIO");
        }
        else if (tipoAtual == null) {
            Toast.makeText(requireContext(), "Tipo de usuário não informado", Toast.LENGTH_SHORT).show();
            // Considere adicionar Navigation.findNavController(root).popBackStack(); aqui para voltar.
        }

        configurarJaPossuiConta();

        // 1. IMPLEMENTAÇÃO DA VALIDAÇÃO E FLUXO DE CADASTRO
        binding.btnCadastrar.setOnClickListener(v ->{
            limparTodosErros();
            ocultarMensagem();

            // 1. Coleta de dados
            String nome = binding.tilNome.getEditText().getText().toString().trim();
            String email = binding.tilEmail.getEditText().getText().toString().trim();
            String telefone = binding.tilTelefone.getEditText().getText().toString().trim();
            String senha = binding.tilSenha.getEditText().getText().toString().trim();
            String confirmarSenha = binding.tilConfirmar.getEditText().getText().toString().trim();

            // Tratamento do CPF/CNPJ (assumindo que tilCnpj está sendo usado para ambos)
            String documento = binding.tilCnpj.getEditText().getText().toString().trim();
            String campoDocumento = tipoAtual == TipoUsuario.WORKER ? "CPF" : "CNPJ";


            // 2. CHAMA A VALIDAÇÃO: Se falhar, o método retorna e o cadastro não é iniciado
            if (!validarCampos(nome, email, telefone, documento, senha, confirmarSenha, campoDocumento)) {
                mostrarMensagem("Preencha todos os campos corretamente.");
                return;
            }

            // 3. Prepara o mapa de dados (SÓ ENVIAMOS DADOS, NUNCA A SENHA)
            Map<String, Object> dadosUsuario = new HashMap<>();
            dadosUsuario.put("Nome", nome);
            dadosUsuario.put("Email", email);
            dadosUsuario.put("Telefone", telefone);
            // Usa a chave correta (CPF ou CNPJ) no mapa
            dadosUsuario.put(campoDocumento, documento);

            AuthAdapter adapter = new AuthAdapter();

            // 4. Chama o cadastro (sem try-catch, pois o AuthAdapter não lança exceções diretas)
            adapter.cadastrar(email, senha, tipoAtual, dadosUsuario, requireContext());

            // 5. Navega para a tela de escolha do plano
            bundle.putSerializable("TIPO_USUARIO", tipoAtual);
            bundle.putString("Nome", nome);
            bundle.putString("Email", email);
            Navigation.findNavController(v).navigate(R.id.Plan, bundle);

        });

        return root;
    }

    private void configurarJaPossuiConta() {
        String base = "Já possui uma conta? Entre com ela.";
        SpannableString ss = new SpannableString(base);
        int start = base.indexOf("Entre com ela.");
        int end = start + "Entre com ela.".length();

        // cor
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), com.example.core.R.color.error_red)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // clique -> navega para a tela de login
        ClickableSpan click = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("TIPO_USUARIO", tipoAtual);
                Navigation.findNavController(requireView()).navigate(R.id.Login);
            }
        };
        ss.setSpan(click, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvJaConta.setText(ss);
        binding.tvJaConta.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvJaConta.setHighlightColor(Color.TRANSPARENT);
    }


    // 2. Método validarCampos corrigido para receber o tipo de documento
    private boolean validarCampos(String nome, String email, String fone, String documento, String senha, String confirmarSenha, String tipoDocumento) {
        boolean ok = true;

        if (TextUtils.isEmpty(nome)) { error(binding.tilNome, "Informe seu nome"); ok = false; }
        if (TextUtils.isEmpty(email)) { error(binding.tilEmail, "Informe seu e-mail"); ok = false; }
        else if (!Validators.isValidEmail(email)) { error(binding.tilEmail, "E-mail inválido"); ok = false; }
        if (TextUtils.isEmpty(fone)) { error(binding.tilTelefone, "Informe seu telefone"); ok = false; }
        else if (!Validators.isValidPhone(fone)) { error(binding.tilTelefone, "Telefone inválido"); ok = false; }

        // Validação condicional do documento (CPF ou CNPJ)
        if (TextUtils.isEmpty(documento)) {
            error(binding.tilCnpj, "Informe seu " + tipoDocumento);
            ok = false;
        }
        else if (tipoDocumento.equals("CNPJ") && !Validators.isValidCNPJ(documento)) {
            error(binding.tilCnpj, "CNPJ inválido");
            ok = false;
        }
        // OBS: Adicione a validação Validators.isValidCPF para WORKER (CPF)
        else if (tipoDocumento.equals("CPF") && !Validators.isValidCPF(documento)) {
            error(binding.tilCnpj, "CPF inválido");
            ok = false;
        }

        if (TextUtils.isEmpty(senha)) { error(binding.tilSenha, "Informe sua senha"); ok = false; }
        else if (!Validators.isStrongPassword(senha)) { error(binding.tilSenha, "Mín. 6, com letra e número"); ok = false; }
        if (TextUtils.isEmpty(confirmarSenha)) { error(binding.tilConfirmar, "Confirme sua senha"); ok = false; }
        else if (!senha.equals(confirmarSenha)) { error(binding.tilConfirmar, "As senhas não coincidem"); ok = false; }

        return ok;
    }

    // Métodos de utilidade (mantidos sem alteração)
    private String get(TextInputEditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private void clearErrors() { /* ... */ }
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