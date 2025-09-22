package com.example.zeta_mobile.company;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.core.Repository;
import com.example.zeta_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterCompanyActivity extends AppCompatActivity {

    private TextInputLayout tilNome, tilEmail, tilTelefone, tilCnpj, tilSenha, tilConfirmar, tilMsg;
    private TextInputEditText edtNome, edtEmail, edtTelefone, edtCnpj, edtSenha, edtConfirmar;
    private MaterialButton btnCadastrar;
    private FirebaseAuth mAuth;
    private final Repository repo = new Repository();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_company);

        mAuth = FirebaseAuth.getInstance();

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

        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCadastrar.setOnClickListener(v -> cadastrar());
    }

    private void cadastrar() {
        clearErrors();

        String nome = get(edtNome);
        String email = get(edtEmail);
        String fone = get(edtTelefone);
        String cnpj = get(edtCnpj);
        String s1 = get(edtSenha);
        String s2 = get(edtConfirmar);

        boolean ok = true;

        if (TextUtils.isEmpty(nome)) { error(tilNome, "Informe seu nome"); ok = false; }
        if (TextUtils.isEmpty(email)) { error(tilEmail, "Informe seu e-mail"); ok = false; }
        else if (!Validators.isValidEmail(email)) { error(tilEmail, "E-mail inválido"); ok = false; }

        if (TextUtils.isEmpty(fone)) { error(tilTelefone, "Informe seu telefone"); ok = false; }
        else if (!Validators.isValidPhone(fone)) { error(tilTelefone, "Telefone inválido"); ok = false; }

        if (TextUtils.isEmpty(cnpj)) { error(tilCnpj, "Informe seu CNPJ"); ok = false; }
        else if (!Validators.isValidCNPJ(cnpj)) { error(tilCnpj, "CNPJ inválido"); ok = false; }

        if (TextUtils.isEmpty(s1)) { error(tilSenha, "Informe sua senha"); ok = false; }
        else if (!Validators.isStrongPassword(s1)) { error(tilSenha, "Mín. 6, com letra e número"); ok = false; }

        if (TextUtils.isEmpty(s2)) { error(tilConfirmar, "Confirme sua senha"); ok = false; }
        else if (!s1.equals(s2)) { error(tilConfirmar, "As senhas não coincidem"); ok = false; }

        if (!ok) return;

        // 1) Cria no Auth
        mAuth.createUserWithEmailAndPassword(email, s1)
                .addOnSuccessListener(res -> {
                    // 2) Atualiza displayName no Auth
                    res.getUser().updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(nome).build()
                    );

                    // 3) Upsert no Firestore com createdAt/lastLoginAt + cnpj/phone
                    repo.upsertFromAuth(res.getUser(), null)
                            .onSuccessTask(aVoid -> repo.updateContact(res.getUser().getUid(), cnpj, fone))
                            .addOnSuccessListener(v -> {
                                // 4) Próxima tela do seu fluxo
                                startActivity(new Intent(this, PlanCompanyActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> error(tilEmail, "Falha ao salvar dados. Tente novamente."));
                })
                .addOnFailureListener(e -> error(tilEmail, mapAuthCreateError(e)));
    }

    // ===== utilidades =====
    private String get(TextInputEditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private void clearErrors() {
        for (TextInputLayout til : new TextInputLayout[]{tilNome, tilEmail, tilTelefone, tilCnpj, tilSenha, tilConfirmar}) {
            if (til != null) { til.setError(null); til.setBoxStrokeColor(ContextCompat.getColor(this, R.color.plan_card_bg)); }
        }
    }
    private void error(TextInputLayout til, String msg) {
        if (til == null) return;
        til.setError(msg);
        til.setErrorIconDrawable(null);
        int red = ContextCompat.getColor(this, R.color.error_red);
        til.setBoxStrokeColor(red);
        try { til.setBoxStrokeWidth(2); til.setBoxStrokeWidthFocused(2); } catch (Exception ignored) {}
    }
    private String mapAuthCreateError(Exception e) {
        String m = e.getMessage() == null ? "" : e.getMessage();
        if (m.contains("email address is already in use")) return "E-mail já cadastrado";
        if (m.contains("WEAK_PASSWORD")) return "Senha fraca (mín. 6)";
        if (m.contains("INVALID_EMAIL")) return "E-mail inválido";
        return "Não foi possível criar sua conta";
    }
}
