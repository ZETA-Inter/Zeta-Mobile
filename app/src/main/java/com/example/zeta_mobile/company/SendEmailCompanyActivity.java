package com.example.zeta_mobile.company;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zeta_mobile.R;
import com.google.android.material.button.MaterialButton;

public class SendEmailCompanyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email_company);

        MaterialButton btnVoltar = findViewById(R.id.btnVoltarLogin);
        btnVoltar.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginCompanyActivity.class));
            finish(); // encerra esta tela para não voltar aqui ao pressionar back
        });
    }
}
