package com.example.zeta_mobile.company;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.core.Repository;
import com.example.zeta_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginCompanyActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilSenha;
    private TextInputEditText edtEmail, edtSenha;
    private MaterialButton btnEntrar;
    private ImageButton btnGoogle;

    private FirebaseAuth mAuth;
    private final Repository repo = new Repository();

    private static final int RC_GOOGLE = 9001;
    private GoogleSignInClient gsc;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_company);

        mAuth = FirebaseAuth.getInstance();
        tilEmail = findViewById(R.id.tilEmail);
        tilSenha = findViewById(R.id.tilSenha);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(v -> loginEmailSenha());

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        btnGoogle = findViewById(R.id.btnGoogle); // se for ImageView, troque aqui pelo seu id
        if (btnGoogle != null) btnGoogle.setOnClickListener(v -> startGoogleFlow());
    }

    // ===== E-mail/Senha =====
    private void loginEmailSenha() {
        clearErrors();
        String email = get(edtEmail), senha = get(edtSenha);

        boolean ok = true;
        if (TextUtils.isEmpty(email)) { error(tilEmail, "Informe seu e-mail"); ok = false; }
        else if (!Validators.isValidEmail(email)) { error(tilEmail, "E-mail inválido"); ok = false; }
        if (TextUtils.isEmpty(senha)) { error(tilSenha, "Informe sua senha"); ok = false; }
        if (!ok) return;

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(res -> {
                    goHome();
                })
                .addOnFailureListener(e -> error(tilEmail, mapAuthLoginError(e)));

    }

    // ===== Google =====
    private void startGoogleFlow() {
        startActivityForResult(gsc.getSignInIntent(), RC_GOOGLE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acc = task.getResult(ApiException.class);
                AuthCredential cred = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(cred)
                        .addOnSuccessListener(r -> {
                            // cria/atualiza doc da company com createdAt/lastLoginAt
                            repo.upsertFromAuth(r.getUser(), null)
                                    .addOnCompleteListener(x -> goHome());
                        })
                        .addOnFailureListener(e -> error(tilEmail, "Falha no Google Sign-In (verifique SHA-1/SHA-256 e default_web_client_id)"));
            } catch (ApiException e) {
                // cancelado ou falhou
            }
        }
    }

    private void goHome() {
        startActivity(new Intent(this, HomePageCompanyActivity.class));
        finish();
    }

    // ===== utils UI =====
    private String get(TextInputEditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private void clearErrors() { if (tilEmail!=null){tilEmail.setError(null);} if (tilSenha!=null){tilSenha.setError(null);} }
    private void error(TextInputLayout til, String msg) {
        if (til == null) return;
        til.setError(msg);
        til.setErrorIconDrawable(null);
        int red = ContextCompat.getColor(this, R.color.error_red);
        til.setBoxStrokeColor(red);
        try { til.setBoxStrokeWidth(2); til.setBoxStrokeWidthFocused(2); } catch (Exception ignored) {}
    }
    private String mapAuthLoginError(Exception e) {
        String m = e.getMessage() == null ? "" : e.getMessage();
        if (m.contains("no user record")) return "Usuário não encontrado";
        if (m.contains("password is invalid")) return "Senha inválida";
        if (m.contains("INVALID_EMAIL")) return "E-mail inválido";
        return "Falha no login";
    }
}
