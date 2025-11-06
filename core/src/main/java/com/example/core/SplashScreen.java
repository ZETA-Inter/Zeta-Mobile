package com.example.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.core.databinding.FragmentSplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends Fragment {

    private static final long SPLASH_DELAY = 1200L; // 1.2s
    private FragmentSplashScreenBinding binding;

    // Guardamos handler e runnable para poder cancelar no onDestroyView()
    private final Handler handler = new Handler();
    private final Runnable navigateRunnable = new Runnable() {
        @Override public void run() {
            if (!isAdded() || binding == null) return;

            // Resolva o NavController pela VIEW (mais seguro aqui)
            View root = binding.getRoot();
            NavController nav = Navigation.findNavController(root);

            // Evita navegação múltipla se já saiu do Splash
            if (nav.getCurrentDestination() == null ||
                    nav.getCurrentDestination().getId() != R.id.SplashScreen) {
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            try {
                if (user != null) {
                    String userType = getUserType();
                    if ("WORKER".equalsIgnoreCase(userType)) {
                        nav.navigate(Uri.parse("app://Worker/Home"));
                    } else if ("COMPANY".equalsIgnoreCase(userType)) {
                        nav.navigate(Uri.parse("app://Company/Home"));
                    } else {
                        nav.navigate(R.id.FirstPage);
                    }
                } else {
                    // Volta limpando o Splash da pilha
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.SplashScreen, true)
                            .build();
                    nav.navigate(R.id.FirstPage, null, options);
                }
            } catch (Exception e) {
                // Fallback seguro
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.SplashScreen, true)
                        .build();
                nav.navigate(R.id.FirstPage, null, options);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Status bar combinando com o fundo
        requireActivity().getWindow().setStatusBarColor(
                ContextCompat.getColor(requireContext(), R.color.bg_light));

        // Animação do logo
        Animation fade = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
        binding.imgLogoSplash.startAnimation(fade);

        // Dispara a navegação DEPOIS da view existir
        view.postDelayed(navigateRunnable, SPLASH_DELAY);
    }

    private String getUserType() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return prefs.getString("tipo_usuario", "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancela callbacks pendentes para não rodar sem view
        handler.removeCallbacksAndMessages(null);
        View v = binding != null ? binding.getRoot() : null;
        if (v != null) v.removeCallbacks(navigateRunnable);
        binding = null;
    }
}
