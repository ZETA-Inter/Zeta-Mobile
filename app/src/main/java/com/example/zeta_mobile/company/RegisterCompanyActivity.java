package com.example.zeta_mobile.company;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zeta_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.TextView;

public class RegisterCompanyActivity extends AppCompatActivity {

    private TextInputLayout tilNome, tilEmail, tilTelefone, tilCnpj, tilSenha, tilConfirmar;
    private TextInputEditText edtNome, edtEmail, edtTelefone, edtCnpj, edtSenha, edtConfirmar;
    private TextView tvFormMsg, tvJaConta;
    private MaterialButton btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_company);

        // refs
        tilNome = findViewById(R.id.tilNome);
        tilEmail = findViewById(R.id.tilEmail);
        tilTelefone = findViewById(R.id.tilTelefone);
        tilCnpj = findViewById(R.id.tilCnpj);
        tilSenha = findViewById(R.id.tilSenha);
        tilConfirmar = findViewById(R.id.tilConfirmar);

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtCnpj = findViewById(R.id.edtCnpj);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmar = findViewById(R.id.edtConfirmar);

        tvFormMsg = findViewById(R.id.tvFormMsg);
        tvJaConta = findViewById(R.id.tvJaConta);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        // link “Entre com ela.” clicável e vermelho
        configurarJaPossuiConta();

        btnCadastrar.setOnClickListener(v -> validarOuIr());
    }

    private void configurarJaPossuiConta() {
        String base = "Já possui uma conta? Entre com ela.";
        SpannableString ss = new SpannableString(base);
        int start = base.indexOf("Entre com ela.");
        int end = start + "Entre com ela.".length();

        // cor vermelha
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.error_red)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // clique -> LoginCompanyActivity
        ClickableSpan click = new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) {
                startActivity(new Intent(RegisterCompanyActivity.this, LoginCompanyActivity.class));
                finish();
            }
        };
        ss.setSpan(click, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvJaConta.setText(ss);
        tvJaConta.setMovementMethod(LinkMovementMethod.getInstance());
        tvJaConta.setHighlightColor(Color.TRANSPARENT);
    }

    private void validarOuIr() {
        boolean ok = true;

        limparTodosErros();
        ocultarMensagem();

        if (isEmpty(edtNome))    { marcarErro(tilNome, "Informe seu nome"); ok = false; }
        if (isEmpty(edtEmail))   { marcarErro(tilEmail, "Informe seu e-mail"); ok = false; }
        if (isEmpty(edtTelefone)){ marcarErro(tilTelefone, "Informe seu telefone"); ok = false; }
        if (isEmpty(edtCnpj))    { marcarErro(tilCnpj, "Informe seu CNPJ"); ok = false; }
        if (isEmpty(edtSenha))   { marcarErro(tilSenha, "Informe sua senha"); ok = false; }
        if (isEmpty(edtConfirmar)){ marcarErro(tilConfirmar, "Confirme sua senha"); ok = false; }

        if (!ok) {
            mostrarMensagem("Preencha todos os campos.");
            return;
        }

        String s1 = edtSenha.getText().toString();
        String s2 = edtConfirmar.getText().toString();
        if (!s1.equals(s2)) {
            marcarErro(tilConfirmar, "As senhas não coincidem");
           mostrarMensagem("Corrija os campos em vermelho.");
             return;
        }

        // Sucesso -> vai para PlanCompanyActivity
        startActivity(new Intent(this, PlanCompanyActivity.class));
        finish();
    }

    private boolean isEmpty(TextInputEditText edt) {
        CharSequence t = edt.getText();
        return t == null || TextUtils.isEmpty(t.toString().trim());
    }

    private void marcarErro(TextInputLayout til, String msg) {
        int red = ContextCompat.getColor(this, R.color.error_red);
        til.setError(msg);
        til.setErrorIconDrawable(null);
        til.setBoxStrokeColor(red);
        try {
            til.setBoxStrokeWidth(2);
            til.setBoxStrokeWidthFocused(2);
        } catch (Exception ignored) {}
    }

    private void limparTodosErros() {
        limparErro(tilNome);
        limparErro(tilEmail);
        limparErro(tilTelefone);
        limparErro(tilCnpj);
        limparErro(tilSenha);
        limparErro(tilConfirmar);
    }

    private void limparErro(TextInputLayout til) {
        til.setError(null);
        til.setErrorEnabled(false);
    }

    private void mostrarMensagem(String s) {
        tvFormMsg.setText(s);
        tvFormMsg.setVisibility(View.VISIBLE);
    }

    private void ocultarMensagem() {
        tvFormMsg.setVisibility(View.GONE);
    }
}
