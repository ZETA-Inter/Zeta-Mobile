package com.example.zeta_mobile.company;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zeta_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.TextView;

public class ForgetPasswordCompanyActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText edtEmail;
    private MaterialButton btnEnviarEmail;
    private TextView tvFormMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_company);

        tilEmail = findViewById(R.id.tilEmail);
        edtEmail = findViewById(R.id.edtEmail);
        btnEnviarEmail = findViewById(R.id.btnEnviarEmail);
        tvFormMsg = findViewById(R.id.tvFormMsg);

        btnEnviarEmail.setOnClickListener(v -> validarOuIr());
    }

    private void validarOuIr() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";

        limparErro(tilEmail);
        ocultarMensagem();

        if (TextUtils.isEmpty(email)) {
            marcarErro(tilEmail, "Informe seu e-mail");
            mostrarMensagem("O campo deve ser preenchido.");
            return;
        }

        // OK -> próxima tela (envio de e-mail)
        startActivity(new Intent(this, SendEmailCompanyActivity.class));
        finish();
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

    private void limparErro(TextInputLayout til) {
        til.setError(null);
        til.setErrorEnabled(false);
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
