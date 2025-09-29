//package com.example.zeta_mobile.company;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//
//import com.example.core.SendEmail;
//import com.example.core.Validators;
//import com.example.zeta_mobile.R;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class ForgetPasswordCompanyActivity extends AppCompatActivity {
//
//    private TextInputLayout tilEmail;
//    private TextInputEditText edtEmail;
//    private MaterialButton btnEnviar;
//
//    @Override protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_forget_password_company);
//
//        tilEmail = findViewById(R.id.tilEmail);
//        edtEmail = findViewById(R.id.edtEmail);
//        btnEnviar = findViewById(R.id.btnEnviarEmail);
//
//        btnEnviar.setOnClickListener(v -> enviar());
//    }
//
//    private void enviar() {
//        tilEmail.setError(null);
//        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
//        if (TextUtils.isEmpty(email)) { setErr("Informe seu e-mail"); return; }
//        if (!Validators.isValidEmail(email)) { setErr("E-mail inválido"); return; }
//
//        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
//                .addOnSuccessListener(v -> {
//                    startActivity(new Intent(this, SendEmail.class));
//                    finish();
//                })
//                .addOnFailureListener(e -> setErr("Não foi possível enviar. Verifique o e-mail."));
//    }
//
//    private void setErr(String msg) {
//        tilEmail.setError(msg);
//        tilEmail.setErrorIconDrawable(null);
//        int red = ContextCompat.getColor(this, com.example.feature_produtor.R.color.error_red);
//        tilEmail.setBoxStrokeColor(red);
//        try { tilEmail.setBoxStrokeWidth(2); tilEmail.setBoxStrokeWidthFocused(2); } catch (Exception ignored) {}
//    }
//}
