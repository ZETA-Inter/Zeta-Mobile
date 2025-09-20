package com.example.zeta_mobile;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zeta_mobile.R;
import com.example.zeta_mobile.company.LoginCompanyActivity;
import com.example.zeta_mobile.worker.LoginWorkerActivity;
import com.google.android.material.button.MaterialButton;

import com.example.zeta_mobile.R;
import com.example.zeta_mobile.company.LoginCompanyActivity;
import com.example.zeta_mobile.worker.LoginWorkerActivity;
import com.google.android.material.button.MaterialButton;

public class FirstPageActivity extends AppCompatActivity {

    private MaterialButton btnFornecedor, btnProdutor;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomeRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        btnFornecedor = findViewById(R.id.btnFornecedor);
        btnProdutor   = findViewById(R.id.btnProdutor);

        btnFornecedor.setOnClickListener(v ->
                startActivity(new Intent(this, LoginCompanyActivity.class)));

        btnProdutor.setOnClickListener(v ->
                startActivity(new Intent(this, LoginWorkerActivity.class)));

    }
}
