package com.example.zeta_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.widget.ImageView;

import com.example.zeta_mobile.company.HomePageCompanyActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1200L; // 1.2s

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // status bar da mesma cor do fundo (pra não “cortar”)
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.bg_light));

        ImageView logo = findViewById(R.id.imgLogoSplash);
        Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fade);

        // depois do fade, vai para a próxima tela
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, com.example.zeta_mobile.FirstPageActivity.class));
            finish();
        }, SPLASH_DELAY);

        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        startActivity(new Intent(this, fu != null ? HomePageCompanyActivity.class : FirstPageActivity.class));
        finish();

    }
}
