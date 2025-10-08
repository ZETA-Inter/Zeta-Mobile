package com.example.core;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.client.ApiPostgresClient;
import com.example.core.databinding.FragmentPaymentSuccessfulBinding;
import com.example.core.dto.request.CompanyRequest;
import com.example.core.dto.request.WorkerRequest;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentSuccessful extends Fragment {

    private FragmentPaymentSuccessfulBinding binding;

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

//        String nome = bundle.getString("Nome");
//        String email = bundle.getString("Email");
//        Integer planId = bundle.getInt("plan_id");
//        String duration = bundle.getString("duration");
//        Double amount = bundle.getDouble("amount");
//        createUser();

        // espera ~3s e vai para a Home da empresa
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String deeplink = tipoAtual == TipoUsuario.COMPANY ? "app://Company/Home" : "app://Worker/Home";
            Uri deepLinkUri = Uri.parse(deeplink);
            Navigation.findNavController(view).navigate(deepLinkUri);
        }, 3000);

        return root;
    }

    public void createUser() {
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
        Call<?> call;
        if (tipoAtual == TipoUsuario.WORKER) {
            call = postgresClient.createWorker(new WorkerRequest());
        } else {
            call = postgresClient.createCompany(new CompanyRequest());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Importante: limpa a referência do binding para evitar memory leaks
        binding = null;
    }
}
