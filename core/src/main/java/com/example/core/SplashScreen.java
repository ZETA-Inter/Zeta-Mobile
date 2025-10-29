package com.example.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkRequest;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.core.databinding.FragmentSplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SplashScreen extends Fragment {

    private static final long SPLASH_DELAY = 1200L; // 1.2s
    private FragmentSplashScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSplashScreenBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Status bar da mesma cor do fundo
        if (getActivity() != null) {
            getActivity().getWindow()
                    .setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.bg_light));
        }

        // Animação do logo
        Animation fade = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
        binding.imgLogoSplash.startAnimation(fade);

        // Checa FirebaseUser antes do delay
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Delay e redirecionamento
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Use o 'root' view para encontrar o NavController
            NavController navController = NavHostFragment.findNavController(this);

            if (user != null) {

                String userType = getUserType();
                Log.d("Main", "Tipo de Usuário: " + userType);

                Uri deepLink = null;
                if (userType.equals("WORKER")) {
                    deepLink = Uri.parse("app://Company/Home");
                } else if (userType.equals("COMPANY")) {
                    deepLink = Uri.parse("app://Company/Home");
                }

                navController.navigate(deepLink);
            } else {
                // Navega para a página inicial (Primeira Página)
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.SplashScreen, true)
                        .build();

                navController.navigate(R.id.FirstPage, null, options);
            }

        }, SPLASH_DELAY);

        return root;
    }

    private String getUserType() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return prefs.getString("tipo_usuario", "");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
