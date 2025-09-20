package com.example.zeta_mobile.company;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zeta_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PlanCompanyActivity extends AppCompatActivity {

    private MaterialCardView cardBasic, cardPlus;
    private MaterialButton btnContinuar;
    private TextView tvPlansError;

    // 0 = nenhum, 1 = basic (FREE), 2 = plus (PAGO)
    private int selected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_company);

        cardBasic = findViewById(R.id.cardBasic);
        cardPlus  = findViewById(R.id.cardPlus);
        btnContinuar = findViewById(R.id.btnContinuar);
        tvPlansError = findViewById(R.id.tvPlansError);

        cardBasic.setOnClickListener(v -> selectPlan(1));
        cardPlus.setOnClickListener(v -> selectPlan(2));

        btnContinuar.setOnClickListener(v -> continuar());
    }

    private void selectPlan(int which) {
        int primary = ContextCompat.getColor(this, R.color.primary_dark);
        int transparent = ContextCompat.getColor(this, android.R.color.transparent);

        hideError();

        // limpa bordas
        cardBasic.setStrokeColor(transparent);
        cardPlus.setStrokeColor(transparent);

        if (which == 1) {
            cardBasic.setStrokeColor(primary);  // destaca o selecionado
            selected = 1;
        } else {
            cardPlus.setStrokeColor(primary);
            selected = 2;
        }
    }

    private void continuar() {
        if (selected == 0) {
            // erro: nada selecionado
            showError("Escolha um plano para continuar.");
            int red = ContextCompat.getColor(this, R.color.error_red);
            cardBasic.setStrokeColor(red);
            cardPlus.setStrokeColor(red);
            return;
        }

        if (selected == 1) {
            // FREE -> vai direto para a home
            Intent it = new Intent(this, HomePageCompanyActivity.class);
            startActivity(it);
            finish();
        } else {
            // PAGO -> vai para a tela de pagamento
            Intent it = new Intent(this, PaymentCompanyActivity.class);
            it.putExtra("plan_selected", selected);
            startActivity(it);
            finish();
        }
    }

    private void showError(String msg) {
        tvPlansError.setText(msg);
        tvPlansError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvPlansError.setVisibility(View.GONE);
    }
}
