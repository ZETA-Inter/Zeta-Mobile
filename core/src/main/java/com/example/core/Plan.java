package com.example.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.adapter.PlanAdapter;
import com.example.core.client.ApiPostgresClient;
import com.example.core.databinding.FragmentPlanBinding;
import com.example.core.dto.response.PlanResponse;
import com.example.core.network.RetrofitClientPostgres;
import com.example.core.ui.BrandingHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Plan extends Fragment {

    private FragmentPlanBinding binding;
    private RecyclerView planRV;

    private TipoUsuario tipoAtual;
    private PlanResponse selectedPlan;
    private int selected = 0;
    private Bundle bundle;

    public Plan() {}

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
        tipoAtual = BrandingHelper.resolveTipo(requireContext(), fromBundle);
        if (tipoAtual == null) tipoAtual = TipoUsuario.COMPANY; // fallback seguro

        LayoutInflater themed = BrandingHelper.themedInflater(requireContext(), inflater, tipoAtual);
        binding = FragmentPlanBinding.inflate(themed, container, false);
        View root = binding.getRoot();

        // 2) Guarda/normaliza o bundle para navegação adiante
        bundle = (args != null) ? args : new Bundle();
        bundle.putSerializable("TIPO_USUARIO", tipoAtual);

        // 3) Aplica branding (logo + botão continuar)
        //    Garanta que seu layout possua um ImageView @id/ivLogo
        BrandingHelper.applyBrandToViews(
                root,
                tipoAtual,
                R.id.imgLogo,
                R.id.btnContinuar
        );

        // 4) Carrega os planos
        list_plans_api();

        // 5) Continuar
        binding.btnContinuar.setOnClickListener(this::continuar);

        return root;
    }

    private void list_plans_api() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiPostgresClient api = RetrofitClientPostgres.getApiService(requireContext());
        Call<List<PlanResponse>> call = api.listPlans();

        call.enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<PlanResponse>> call,
                                   @NonNull Response<List<PlanResponse>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    showError("Não foi possível carregar os planos. (" + response.code() + ")");
                    return;
                }

                List<PlanResponse> plans = response.body();

                PlanAdapter planAdapter = new PlanAdapter(plans, plan -> {
                    hideError();
                    selectedPlan = plan;
                    selected = plan.getPlanId();
                });

                planRV = binding.planRV;
                planRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                planRV.setAdapter(planAdapter);
            }

            @Override
            public void onFailure(@NonNull Call<List<PlanResponse>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showError("Falha ao carregar planos: " + (t.getMessage() != null ? t.getMessage() : "erro de rede"));
                // evite recursão infinita aqui; deixe o usuário tentar novamente manualmente se desejar
            }
        });
    }

    private void continuar(View v) {
        if (selected == 0 || selectedPlan == null) {
            showError("Escolha um plano para continuar.");
            return;
        }
        hideError();

        bundle.putInt("plan_id", selected);
        showDurationCard(v);
    }

    private void showDurationCard(View v) {
        // Infla o card no mesmo tema atual
        View cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.card_plan_duration, binding.rootLayout, false);

        MaterialCardView card = cardView.findViewById(R.id.cardDuration);

        // Valores do plano selecionado
        ((TextView) cardView.findViewById(R.id.tvMensalValue))
                .setText(String.format("R$%,.2f", selectedPlan.getValue()));
        ((TextView) cardView.findViewById(R.id.tvSemestralValue))
                .setText(String.format("R$%,.2f", (selectedPlan.getValue() * 5.5)));
        ((TextView) cardView.findViewById(R.id.tvAnualValue))
                .setText(String.format("R$%,.2f", (selectedPlan.getValue() * 10)));

        // Clique nas opções
        cardView.findViewById(R.id.cardMensal).setOnClickListener(view -> {
            bundle.putString("duration", "mensal");
            bundle.putDouble("amount", selectedPlan.getValue());
            goToPayment(view);
        });

        cardView.findViewById(R.id.cardSemestral).setOnClickListener(view -> {
            bundle.putString("duration", "semestral");
            bundle.putDouble("amount", (selectedPlan.getValue() * 5.5));
            goToPayment(view);
        });

        cardView.findViewById(R.id.cardAnual).setOnClickListener(view -> {
            bundle.putString("duration", "anual");
            bundle.putDouble("amount", (selectedPlan.getValue() * 10));
            goToPayment(view);
        });

        // Exibe modal
        binding.modalBackground.setVisibility(View.VISIBLE);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.CENTER;
        params.setMargins(20, 0, 20, 0);
        binding.rootLayout.addView(cardView, params);

        // Fecha modal clicando no fundo
        binding.modalBackground.setOnClickListener(view -> {
            binding.modalBackground.setVisibility(View.GONE);
            binding.rootLayout.removeView(cardView);
        });
    }

    private void goToPayment(View v) {
        if (selectedPlan != null && selectedPlan.getValue() == 0) {
            Navigation.findNavController(v).navigate(R.id.PaymentSuccessful, bundle);
        } else {
            Navigation.findNavController(v).navigate(R.id.Payment, bundle);
        }
    }

    private void showError(String msg) {
        binding.tvPlansError.setText(msg);
        binding.tvPlansError.setVisibility(View.VISIBLE);
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void hideError() {
        binding.tvPlansError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
