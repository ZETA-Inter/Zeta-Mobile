package com.example.zeta_mobile.company;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zeta_mobile.R;

public class PaymentSuccessfulCompanyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_successful_company);

        // espera ~2s e vai para a Home da empresa
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, HomePageCompanyActivity.class));
            finish();
        }, 2000);
    }
}
