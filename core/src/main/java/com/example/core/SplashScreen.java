package com.example.core;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.core.databinding.FragmentSplashScreenBinding;

public class SplashScreen extends Fragment {

    private static final long SPLASH_DELAY = 1200L; // 1.2s

    private FragmentSplashScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentSplashScreenBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        if (getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.bg_light));
        }

        Animation fade = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
        binding.imgLogoSplash.startAnimation(fade);


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(requireContext(), FirstPage.class);
            startActivity(intent);
            requireActivity().finish(); // End the hosting activity
        }, SPLASH_DELAY);

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
