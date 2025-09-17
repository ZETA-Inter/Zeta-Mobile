package com.example.zeta_mobile.company;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zeta_mobile.R;

public class PaymentCompanyActivity extends AppCompatActivity {

    private static final long TOTAL_MS = 5 * 60 * 1000L; // 5 min
    private CountdownProgressView circle;
    private CountDownTimer timer;
    private boolean finishedOrNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_company);

        circle = findViewById(R.id.circleTimer);
        TextView tvPix = findViewById(R.id.tvPixCode);
        ImageButton btnCopy = findViewById(R.id.btnCopy);

        // exemplo: receba o código via Intent se quiser
        String pixCode = tvPix.getText().toString();

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("PIX", pixCode));
            Toast.makeText(this, "Código copiado", Toast.LENGTH_SHORT).show();
        });

        startCountdown();

        // Quando confirmar, chame: onPaymentSuccess();
    }

    private void startCountdown() {
        // estado inicial
        circle.setProgressFraction(1f);
        circle.setCenterText(format(TOTAL_MS));

        timer = new CountDownTimer(TOTAL_MS, 1000L) {
            @Override
            public void onTick(long msLeft) {
                float frac = (float) msLeft / (float) TOTAL_MS;
                circle.setProgressFraction(frac);
                circle.setCenterText(format(msLeft));
            }
            @Override
            public void onFinish() {
                if (!finishedOrNavigated) {
                    goToFailureThenBack();
                }
            }
        }.start();
    }

    private String format(long ms) {
        long s = ms / 1000L;
        long m = s / 60L;
        long ss = s % 60L;
        return String.format("%02d:%02d", m, ss);
    }

    /** Chame isto quando o backend confirmar o pagamento. */
    public void onPaymentSuccess() {
        if (finishedOrNavigated) return;
        finishedOrNavigated = true;
        if (timer != null) timer.cancel();
        startActivity(new Intent(this, PaymentSuccessfulCompanyActivity.class));
        finish();
    }

    private void goToFailureThenBack() {
        finishedOrNavigated = true;
        if (timer != null) timer.cancel();
        Intent it = new Intent(this, PaymentFailureCompanyActivity.class);
        startActivity(it);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
