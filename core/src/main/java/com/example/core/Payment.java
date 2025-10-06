package com.example.core;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.feature_produtor.CountdownProgressView;

import com.example.feature_produtor.databinding.FragmentPaymentWorkerIndependentBinding;

public class Payment extends Fragment {

    private static final long TOTAL_MS = 5 * 60 * 1000L; // 5 min
    private CountDownTimer timer;
    private boolean finishedOrNavigated = false;
    private FragmentPaymentWorkerIndependentBinding binding;
    private CountdownProgressView countdownProgressView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentWorkerIndependentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // carrega o fragment do timer
        if (savedInstanceState == null) {
           // countdownProgressView = new CountdownProgressView();
            getChildFragmentManager()
                    .beginTransaction()
                  //  .replace(R.id.timerContainer, countdownProgressView)
                    .commit();
        }

        String pixCode = binding.tvPixCode.getText().toString();
        binding.btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("PIX", pixCode));
            Toast.makeText(requireContext(), "Código copiado", Toast.LENGTH_SHORT).show();
        });

        startCountdown();
    }

    private void startCountdown() {
        timer = new CountDownTimer(TOTAL_MS, 1000L) {
            @Override
            public void onTick(long msLeft) {
                float frac = (float) msLeft / (float) TOTAL_MS;
                if (countdownProgressView != null) {
                  //  countdownProgressView.updateTimer(frac, format(msLeft));
                }
            }

            @Override
            public void onFinish() {
                if (!finishedOrNavigated && getView() != null) {
                    goToFailureThenBack(getView());
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

    public void onPaymentSuccess(View v) {
        if (finishedOrNavigated) return;
        finishedOrNavigated = true;
        if (timer != null) timer.cancel();

        Navigation.findNavController(v)
                .navigate(R.id.PaymentSuccessfulWorkerIndependent);
    }

    private void goToFailureThenBack(View v) {
        finishedOrNavigated = true;
        if (timer != null) timer.cancel();

        Navigation.findNavController(v)
                .navigate(R.id.PaymentFailureWorkerIndependent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
        binding = null;
        countdownProgressView = null;
    }
}
