package com.example.zeta_mobile.company;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zeta_mobile.R;

public class PaymentFailureCompanyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_failure_company);

        // espera ~2.5s e volta para Planos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, PlanCompanyActivity.class));
            finish();
        }, 2500);
    }
}
