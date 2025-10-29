package com.example.zeta_mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            Log.e("MainActivity", "NavHostFragment não encontrado no layout!");
            return;
        }

        NavController navController = navHostFragment.getNavController();

        boolean isUserLoggedIn = checkIfUserIsLoggedIn();

        if (isUserLoggedIn) {
            String userType = getUserType();
            Log.d("Main", "Tipo de Usuário: " + userType);

            Uri deepLink;
            if ("WORKER".equalsIgnoreCase(userType)) {
                deepLink = Uri.parse("app://Worker/Home");
            } else if ("COMPANY".equalsIgnoreCase(userType)) {
                deepLink = Uri.parse("app://Company/Home");
            } else {
                // Tipo não reconhecido → volta pro login
                Log.w("MainActivity", "Tipo de usuário desconhecido. Redirecionando para login...");
                return;
            }

            try {
                navController.navigate(deepLink);
            } catch (Exception e) {
                Log.e("MainActivity", "Falha ao navegar: " + e.getMessage(), e);
            }

        } else {
            Log.d("MainActivity", "Usuário não logado. Indo para LoginFragment padrão...");
        }
    }

    private boolean checkIfUserIsLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);

        boolean hasUserId = prefs.contains("user_id") || prefs.contains("company_id");
        String userType = prefs.getString("tipo_usuario", "");

        return hasUserId && !userType.isEmpty();
    }

    private String getUserType() {
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return prefs.getString("tipo_usuario", "");
    }

}