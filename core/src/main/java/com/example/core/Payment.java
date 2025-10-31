package com.example.core;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.core.databinding.FragmentPaymentBinding;
import com.example.core.ui.BrandingHelper;

public class Payment extends Fragment {

    private static final long TOTAL_MS = 5 * 60 * 1000L; // 5 min
    private CountDownTimer timer;
    private boolean finishedOrNavigated = false;
    private FragmentPaymentBinding binding;

    private Bundle bundle;
    private TipoUsuario tipoAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1) Resolve TIPO_USUARIO (Bundle → sessão) e aplica Theme Overlay
        Bundle args = getArguments();
        TipoUsuario fromBundle = (args != null)
                ? (TipoUsuario) args.getSerializable("TIPO_USUARIO")
                : null;

        // resolveTipo já faz fallback via sessão; a seguir, mais um fallback seguro
        tipoAtual = BrandingHelper.resolveTipo(requireContext(), fromBundle);
        if (tipoAtual == null) tipoAtual = TipoUsuario.COMPANY;

        LayoutInflater themed = BrandingHelper.themedInflater(requireContext(), inflater, tipoAtual);
        binding = FragmentPaymentBinding.inflate(themed, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2) Bundle normalizado (preserva args) e garante TIPO_USUARIO
        bundle = (getArguments() != null) ? new Bundle(getArguments()) : new Bundle();
        bundle.putSerializable("TIPO_USUARIO", tipoAtual);

        // 3) Aplica branding (logo + botão) e pinta o círculo do contador
        BrandingHelper.applyBrandToViews(
                view,
                tipoAtual,
                R.id.imgLogo,
                R.id.btnCopy
        );
        BrandingHelper.tintCountdown(view, tipoAtual, R.id.countdownProgressView);

        // 4) Prepara o código PIX vindo do layout ou do bundle (se fornecido)
        String pixFromLayout = (binding.tvPixCode != null) ? String.valueOf(binding.tvPixCode.getText()) : "";
        String pixFromArgs = bundle.getString("pix_code", "");
        final String pixCode = !TextUtils.isEmpty(pixFromArgs) ? pixFromArgs : pixFromLayout;
        if (!TextUtils.isEmpty(pixFromArgs) && binding.tvPixCode != null) {
            binding.tvPixCode.setText(pixFromArgs);
        }

        // 5) Clique do botão “Copiar”
        if (binding.btnCopy != null) {
            binding.btnCopy.setOnClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null && !TextUtils.isEmpty(pixCode)) {
                    cm.setPrimaryClip(ClipData.newPlainText("PIX", pixCode));
                    Toast.makeText(requireContext(), "Código copiado", Toast.LENGTH_SHORT).show();
                }
                onPaymentSuccess(v);
            });
        }

        // 6) Timer
        startCountdown();
    }

    private void startCountdown() {
        cancelTimerIfAny();
        timer = new CountDownTimer(TOTAL_MS, 1000L) {
            @Override
            public void onTick(long msLeft) {
                float frac = (float) msLeft / TOTAL_MS;
                if (binding != null && binding.countdownProgressView != null) {
                    binding.countdownProgressView.updateTimer(frac, format(msLeft));
                }
            }

            @Override
            public void onFinish() {
                if (!finishedOrNavigated && getView() != null && isAdded()) {
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
        if (finishedOrNavigated || !isAdded()) return;
        finishedOrNavigated = true;
        cancelTimerIfAny();

        NavController nav = Navigation.findNavController(v);
        // Garante que o bundle vá junto (contendo TIPO_USUARIO e demais dados)
        nav.navigate(R.id.PaymentSuccessful, bundle);
    }

    private void goToFailureThenBack(View v) {
        if (!isAdded()) return;
        finishedOrNavigated = true;
        cancelTimerIfAny();

        // Mantém consistência com a tela Plan: a chave é "plan_id"
        bundle.remove("plan_id");

        Navigation.findNavController(v).navigate(R.id.PaymentFailure, bundle);
    }

    private void cancelTimerIfAny() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Evita vazamento ao sair da tela
        cancelTimerIfAny();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelTimerIfAny();
        binding = null;
    }
}
