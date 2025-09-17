package com.example.zeta_mobile.company;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zeta_mobile.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public class LoginCompanyActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilSenha;
    private TextInputEditText edtEmail, edtSenha;
    private MaterialButton btnEntrar;
    private TextView tvFormMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_company);

        tilEmail = findViewById(R.id.tilEmail);
        tilSenha = findViewById(R.id.tilSenha);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvFormMsg = findViewById(R.id.tvFormMsg);

        btnEntrar.setOnClickListener(v -> validarOuProsseguir());
    }

    private void validarOuProsseguir() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String senha = edtSenha.getText() != null ? edtSenha.getText().toString().trim() : "";

        TextView tvForgot = findViewById(R.id.tvForgotPasswordCompany);
        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgetPasswordCompanyActivity.class)));


        boolean ok = true;

        // limpa estados anteriores
        limparErro(tilEmail);
        limparErro(tilSenha);
        ocultarMensagem();

        if (TextUtils.isEmpty(email)) {
            marcarErro(tilEmail, "Informe seu e-mail");
            ok = false;
        }
        if (TextUtils.isEmpty(senha)) {
            marcarErro(tilSenha, "Informe sua senha");
            ok = false;
        }

        if (!ok) {
            mostrarMensagem("Os campos devem ser preenchidos.");
            return;
        }

        // OK -> próxima tela
        Intent i = new Intent(this, HomePageCompanyActivity.class);
        startActivity(i);
        finish();
    }

    private void marcarErro(TextInputLayout til, String msg) {
        int red = ContextCompat.getColor(this, R.color.error_red);

        // Mostra mensagem de erro no próprio campo (abaixo do input)
        til.setError(msg);
        til.setErrorIconDrawable(null);

        // Força destaque vermelho no contorno do TextInputLayout (mesmo no modo filled)
        til.setBoxStrokeColor(red);
        // Ajusta a largura do traço enquanto em erro (opcional)
        try {
            til.setBoxStrokeWidth(2);
            til.setBoxStrokeWidthFocused(2);
        } catch (Exception ignored) {}
    }

    private void limparErro(TextInputLayout til) {
        til.setError(null);
        til.setErrorEnabled(false);
        // volta pro estado normal (puxa cor do tema/estilo)
    }

    private void mostrarMensagem(String s) {
        if (tvFormMsg != null) {
            tvFormMsg.setText(s);
            tvFormMsg.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarMensagem() {
        if (tvFormMsg != null) tvFormMsg.setVisibility(View.GONE);
    }
}
