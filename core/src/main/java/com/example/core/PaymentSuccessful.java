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

import com.example.core.client.ApiPostgresClient;
import com.example.core.databinding.FragmentPaymentSuccessfulBinding;
import com.example.core.dto.CompanyResponse;
import com.example.core.dto.WorkerResponse;
import com.example.core.dto.request.CompanyRequest;
import com.example.core.dto.request.WorkerRequest;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
        if (tipoAtual == TipoUsuario.WORKER) {
            Call<WorkerResponse> call = postgresClient.createWorker(new WorkerRequest());
            call.enqueue(new Callback<WorkerResponse>() {
                @Override
                public void onResponse(Call<WorkerResponse> call, Response<WorkerResponse> response) {
                    if (response.isSuccessful()) {
                        WorkerResponse worker = response.body();

                        try {
                        // Setar dados do usuário no Shared Preferences, para pegar globalmente em outros fragments
                        SharedPreferences prefs = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", worker.getId());
                        editor.putString("name", worker.getName());
                        editor.putString("email", worker.getEmail());
                        editor.putString("tipo_usuario", tipoAtual.name());
                        editor.apply();

                        } catch (Exception ex) {
                            Log.e("SESSION", "Erro ao salvar sessão: " + ex.getMessage());
                            Toast.makeText(getContext(), "Erro ao salvar sessão local.", Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(getContext(), "Sessão salva com sucesso!", Toast.LENGTH_SHORT).show();
                        Log.d("SESSION", "Usuário salvo: " + worker.getName() + " (ID: " + worker.getId() + ")");
                    }
                }

                @Override
                public void onFailure(Call<WorkerResponse> call, Throwable throwable) {
                    Toast.makeText(getContext(), "Erro ao cadastrar o usuário", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Call<CompanyResponse> call = postgresClient.createCompany(new CompanyRequest());
            call.enqueue(new Callback<CompanyResponse>() {
                @Override
                public void onResponse(Call<CompanyResponse> call, Response<CompanyResponse> response) {
                    if (response.isSuccessful()) {
                        CompanyResponse company = response.body();

                        try {
                            // Setar dados do usuário no Shared Preferences, para pegar globalmente em outros fragments
                            SharedPreferences prefs = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("user_id", company.getId());
                            editor.putString("name", company.getName());
                            editor.putString("email", company.getEmail());
                            editor.putString("tipo_usuario", tipoAtual.name());
                            editor.apply();

                        } catch (Exception ex) {
                            Log.e("SESSION", "Erro ao salvar sessão: " + ex.getMessage());
                            Toast.makeText(getContext(), "Erro ao salvar sessão local.", Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(getContext(), "Sessão salva com sucesso!", Toast.LENGTH_SHORT).show();
                        Log.d("SESSION", "Usuário salvo: " + company.getName() + " (ID: " + company.getId() + ")");
                    }
                }

                @Override
                public void onFailure(Call<CompanyResponse> call, Throwable throwable) {
                    Toast.makeText(getContext(), "Erro ao cadastrar o usuário", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Importante: limpa a referência do binding para evitar memory leaks
        binding = null;
    }
}
