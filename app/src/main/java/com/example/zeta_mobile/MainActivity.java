package com.example.zeta_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.core.notifications.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        NavController navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        NotificationHelper.createNotificationChannel(this);
        pedirPermissaoNotificacao();

//        FirebaseAuth.getInstance().signOut();
//        SharedPreferences sp = getSharedPreferences("user_session", MODE_PRIVATE);
//        sp.edit().clear().apply();

        boolean isUserLoggedIn = checkIfUserIsLoggedIn();

        if (isUserLoggedIn) {
            String userType = getUserType();
            String userName = getUserName();
            Log.d("MainActivity", "Tipo de Usuário: " + userType);

            NotificationHelper.sendNotification(
                    this,
                    "Bem-vindo de volta!",
                    "Bom te ver novamente, " + userName + "!",
                    new Intent(this, MainActivity.class)
            );
            Log.d("MainActivity", "Notificação enviada com sucesso!");

            try {
                navController.navigate(com.example.core.R.id.SplashScreen);
            } catch (Exception e) {
                Log.e("MainActivity", "Falha ao navegar: " + e.getMessage(), e);
            }

        } else {
            Log.d("MainActivity", "Usuário não logado. Indo para LoginFragment padrão...");
            try {
                // Supondo que o ID do seu LoginFragment no nav_graph é 'loginFragment'
                // Você DEVE verificar o nome correto no seu nav_graph.xml.
                assert navController != null;
                navController.navigate(com.example.core.R.id.Login);
            } catch (Exception e) {
                Log.e("MainActivity", "Falha ao navegar para LoginFragment: " + e.getMessage(), e);
            }
        }
    }

    private void pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
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

    private String getUserName() {
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return prefs.getString("name", "");
    }

}