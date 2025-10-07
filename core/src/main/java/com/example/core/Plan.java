package com.example.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.adapter.PlanAdapter;
import com.example.core.client.ApiPostgresClient;
import com.example.core.databinding.FragmentPlanBinding;
import com.example.core.dto.PlanResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import okhttp3.OkHttpClient;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Plan extends Fragment {

    private FragmentPlanBinding binding;

    private RecyclerView planRV;

    private Retrofit retrofit;

    private TipoUsuario tipoAtual;

    private PlanResponse selectedPlan;

    private int selected = 0;

    private Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentPlanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        bundle = getArguments();

        if (bundle != null) {
            tipoAtual = (TipoUsuario) bundle.getSerializable("TIPO_USUARIO");
        }

        // Chamada da API
        list_plans_api();

        // Configura o listener do botão
        binding.btnContinuar.setOnClickListener(this::continuar);

        return root;
    }

    private void list_plans_api() {
        //Definir a URL
        String url = "https://api-postgresql-zeta-fide.onrender.com";

        // Configuração das requisições
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS) // tempo para conectar
                .readTimeout(30, TimeUnit.SECONDS)    // tempo para esperar resposta
                .writeTimeout(20, TimeUnit.SECONDS)   // tempo para enviar dados
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Criar a interface para a API
        ApiPostgresClient postgresClient = retrofit.create(ApiPostgresClient.class);

        // Chamar o método da API
        Call<List<PlanResponse>> call = postgresClient.listPlans();

        call.enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> response) {
                if (response.isSuccessful()) {
                    List<PlanResponse> plans = response.body();

                    // Carrega o adapter do RecyclerView
                    PlanAdapter planAdapter = new PlanAdapter(plans, plan -> {
                        hideError();
                        selectedPlan = plan;
                        selected = plan.getPlanId();
                    });

                    planRV = binding.planRV;
                    planRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                    planRV.setAdapter(planAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable throwable) {
                throwable.printStackTrace();
                Toast.makeText(Plan.this.getContext(), "Erro ao carregar planos", Toast.LENGTH_SHORT).show();
                list_plans_api();
            }
        });
    }

    private void continuar(View v) {
        if (selected == 0) {
            // Erro: nada selecionado
            showError("Escolha um plano para continuar.");
            return;
        }

        hideError();

        bundle.putInt("plano_id", selected);

        if (selectedPlan.getValue() == 0) {
            Navigation.findNavController(v).navigate(R.id.PaymentSuccessful, bundle);
        } else {
            Navigation.findNavController(v).navigate(R.id.Payment, bundle);
        }
    }

    private void showError(String msg) {
        binding.tvPlansError.setText(msg);
        binding.tvPlansError.setVisibility(View.VISIBLE);
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