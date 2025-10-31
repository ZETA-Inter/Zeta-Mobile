package com.example.core;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.databinding.FragmentPaymentSuccessfulBinding;
import com.example.core.dto.request.CompanyRequest;
import com.example.core.dto.request.PlanInfoRequest;
import com.example.core.dto.request.WorkerRequest;
import com.example.core.service.RegisterService;
import com.example.core.ui.BrandingHelper;

import java.util.Map;

public class PaymentSuccessful extends Fragment {

    private FragmentPaymentSuccessfulBinding binding;
    private TipoUsuario tipoAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1) Resolve tipo (Bundle → sessão) e aplica Theme Overlay
        Bundle args = getArguments();
        TipoUsuario fromBundle = (args != null)
                ? (TipoUsuario) args.getSerializable("TIPO_USUARIO")
                : null;
        tipoAtual = BrandingHelper.resolveTipo(requireContext(), fromBundle);
        if (tipoAtual == null) tipoAtual = TipoUsuario.COMPANY; // fallback seguro

        LayoutInflater themed = BrandingHelper.themedInflater(requireContext(), inflater, tipoAtual);
        binding = FragmentPaymentSuccessfulBinding.inflate(themed, container, false);
        View root = binding.getRoot();

        // 2) Aplica logo (opcional—ignora se id não existir no layout)
        BrandingHelper.applyBrandToViews(root, tipoAtual, R.id.imgLogo /*, ids extras se quiser */);

        // 3) Coleta argumentos e valida
        if (args == null) {
            Toast.makeText(requireContext(), "Dados do pagamento ausentes.", Toast.LENGTH_SHORT).show();
            navigateHomeWithDelay(root, 2000);
            return root;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> dadosUsuario = (Map<String, Object>) args.getSerializable("dadosUsuario");
        String nome      = args.getString("Nome", "");
        String email     = args.getString("Email", "");
        int    planId    = args.getInt("plan_id", 0);
        String duration  = args.getString("duration", null);
        double amount    = args.getDouble("amount", 0.0);

        if (dadosUsuario == null || nome.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Informações do usuário incompletas.", Toast.LENGTH_SHORT).show();
            navigateHomeWithDelay(root, 2000);
            return root;
        }

        // 4) Cria usuário (Firebase + backend)
        createUser(dadosUsuario, nome, email, planId, duration, amount, root);

        // 5) Navega para Home após ~3s (independente do backend; ajuste se quiser condicionar)
        navigateHomeWithDelay(root, 3000);

        return root;
    }

    private void createUser(Map<String, Object> dadosUsuario,
                            String nome,
                            String email,
                            int planId,
                            @Nullable String duration,
                            double amount,
                            @NonNull View navView) {

        Object senhaObj = dadosUsuario.get("Senha");
        String senha = (senhaObj instanceof String) ? (String) senhaObj : null;

        if (senha == null || senha.trim().isEmpty()) {
            Log.e("PaymentSuccessful", "Senha ausente ao criar usuário.");
            Toast.makeText(requireContext(), "Não foi possível finalizar o cadastro (senha ausente).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove a senha antes de propagar dados ao Firestore/API
        dadosUsuario.remove("Senha");

        new com.example.core.adapter.AuthAdapter().cadastrar(
                email,
                senha,
                tipoAtual,
                dadosUsuario,
                requireContext(),
                new com.example.core.adapter.AuthAdapter.Listener() {
                    @Override public void onSuccess(String uid) {
                        // Monta o planInfo; duration pode ser null (p.ex. plano grátis)
                        PlanInfoRequest planInfo = new PlanInfoRequest(
                                planId > 0 ? planId : null,
                                duration,
                                amount
                        );

                        // Request específico por tipo
                        Object request;
                        if (tipoAtual == TipoUsuario.WORKER) {
                            // companyId nulo no cadastro do worker (como combinado)
                            request = new WorkerRequest(nome, email, planInfo, null);
                        } else {
                            request = new CompanyRequest(nome, email, planInfo);
                        }

                        try {
                            RegisterService.salvarNoBackend(requireContext(), tipoAtual, request);
                            Log.d("PaymentSuccessful", "Usuário persistido no backend: " + request);
                        } catch (Exception e) {
                            Log.e("PaymentSuccessful", "Falha ao salvar no backend: " + e.getMessage(), e);
                            Toast.makeText(requireContext(), "Falha ao salvar no backend.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onError(String message) {
                        Log.e("PaymentSuccessful", message != null ? message : "Falha no cadastro (Auth)");
                        Toast.makeText(requireContext(), "Falha no cadastro: " + (message != null ? message : ""), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void navigateHomeWithDelay(@NonNull View v, long delayMs) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String deeplink = (tipoAtual == TipoUsuario.COMPANY)
                    ? "app://Company/Home"
                    : "app://Worker/Home";
            Navigation.findNavController(v).navigate(Uri.parse(deeplink));
        }, delayMs);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
