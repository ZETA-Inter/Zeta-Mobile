package com.example.zeta_mobile;


import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Conecta ao layout que contém o NavHostFragment

        // Encontra o NavController a partir do NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        // Lógica de verificação do usuário (ex: SharedPreferences ou Firebase)
        boolean isUserLoggedIn = checkIfUserIsLoggedIn();

        if (isUserLoggedIn) {
            // Se o usuário estiver logado, navegue para o perfil correto
            String userType = getUserType();
            Uri deepLink;
            if ("produtor".equals(userType)) {
                deepLink = Uri.parse("app://Worker/Home");
            } else {
                deepLink = Uri.parse("app://Company/Home");
            }
            navController.navigate(deepLink);
        } else {
            // Se não estiver logado, o Navigation Component irá para o startDestination padrão do seu nav_main.xml.
            // Que, no seu caso, aponta para o LoginFragment no módulo :core.
        }
    }

    // Método de exemplo para verificar o login
    private boolean checkIfUserIsLoggedIn() {
        // Implemente sua lógica de verificação de sessão aqui.
        // Por exemplo, checar um token em SharedPreferences ou Firebase Auth.
        return false;
    }

    // Método de exemplo para obter o tipo de usuário
    private String getUserType() {
        // Implemente sua lógica para saber se o usuário é "produtor" ou "fornecedor"
        return "produtor";
    }
}