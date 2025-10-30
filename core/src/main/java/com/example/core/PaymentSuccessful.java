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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.adapter.AuthAdapter;
import com.example.core.client.ApiPostgresClient;
import com.example.core.databinding.FragmentPaymentSuccessfulBinding;
import com.example.core.dto.request.PlanInfoRequest;
import com.example.core.dto.response.CompanyResponse;
import com.example.core.dto.response.WorkerResponse;
import com.example.core.dto.request.CompanyRequest;
import com.example.core.dto.request.WorkerRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentSuccessful extends Fragment {

    private FragmentPaymentSuccessfulBinding binding;

    private Register register;

    private TipoUsuario tipoAtual;

    private Retrofit retrofit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando o View Binding
        binding = FragmentPaymentSuccessfulBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        View view = root;

        Bundle bundle = getArguments();

        if (bundle != null) {
            tipoAtual = (TipoUsuario) bundle.getSerializable("TIPO_USUARIO");
        }

        Map<String, Object> dadosUsuario = (Map<String, Object>) bundle.getSerializable("dadosUsuario");
        String nome = bundle.getString("Nome");
        String email = bundle.getString("Email");
        Integer planId = bundle.getInt("plan_id");
        String duration = bundle.getString("duration");
        Double amount = bundle.getDouble("amount");
        assert dadosUsuario != null;
        createUser(dadosUsuario, nome, email, planId, duration, amount);

        // espera ~3s e vai para a Home da empresa
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String deeplink = tipoAtual == TipoUsuario.COMPANY ? "app://Company/Home" : "app://Worker/Home";
            Uri deepLinkUri = Uri.parse(deeplink);
            Navigation.findNavController(view).navigate(deepLinkUri);
        }, 3000);

        return root;
    }

    public void createUser(Map<String, Object> dadosUsuario, String nome, String email, Integer planId, String duration, Double amount) {

        String senha = dadosUsuario.get("Senha").toString();

        if (senha == null) {
            Log.e("PaymentSucessful", "Senha vazia.");
            return;
        }

        dadosUsuario.remove("Senha");

        AuthAdapter adapter = new AuthAdapter();
        adapter.cadastrar(email, senha, tipoAtual, dadosUsuario, requireContext(),
                new AuthAdapter.Listener() {
                    @Override public void onSuccess(String uid) {
                        PlanInfoRequest planInfo = new PlanInfoRequest(planId, duration, amount);

                        Object request;
                        if (tipoAtual == TipoUsuario.WORKER) {
                            request = new WorkerRequest(nome, email, planInfo, null);
                        } else {
                            request = new CompanyRequest(nome, email, planInfo);
                        }

                        register.salvarNoBackend(request);
                    }
                    @Override public void onError(String message) {
                        Log.e("Payment Sucessful", message != null ? message : "Falha no cadastro");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Importante: limpa a referência do binding para evitar memory leaks
        binding = null;
    }
}
