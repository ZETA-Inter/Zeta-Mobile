package com.example.core.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.core.TipoUsuario;
import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.request.CompanyRequest;
import com.example.core.dto.request.WorkerRequest;
import com.example.core.dto.response.CompanyResponse;
import com.example.core.dto.response.WorkerResponse;
import com.example.core.network.RetrofitClientPostgres;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterService {

    public static void salvarNoBackend(Context context, TipoUsuario tipo, Object request) {
        ApiPostgresClient api = RetrofitClientPostgres.getApiService(context);

        if (tipo == TipoUsuario.WORKER) {
            Call<WorkerResponse> call = api.createWorker((WorkerRequest) request);
            call.enqueue(new Callback<WorkerResponse>() {
                @Override
                public void onResponse(Call<WorkerResponse> call, Response<WorkerResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        WorkerResponse worker = response.body();
                        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", worker.getId());
                        editor.putString("name", worker.getName());
                        editor.putString("email", worker.getEmail());
                        editor.putString("tipo_usuario", tipo.name());
                        editor.apply();
                        Log.d("RegisterService", "Worker salvo: " + worker.getName());
                    }
                }

                @Override
                public void onFailure(Call<WorkerResponse> call, Throwable t) {
                    Log.e("RegisterService", "Erro ao cadastrar WORKER: " + t.getMessage());
                }
            });
        } else {
            Call<CompanyResponse> call = api.createCompany((CompanyRequest) request);
            call.enqueue(new Callback<CompanyResponse>() {
                @Override
                public void onResponse(Call<CompanyResponse> call, Response<CompanyResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CompanyResponse company = response.body();
                        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", company.getId());
                        editor.putString("name", company.getName());
                        editor.putString("email", company.getEmail());
                        editor.putString("tipo_usuario", tipo.name());
                        editor.apply();
                        Log.d("RegisterService", "Company salvo: " + company.getName());
                    }
                }

                @Override
                public void onFailure(Call<CompanyResponse> call, Throwable t) {
                    Log.e("RegisterService", "Erro ao cadastrar COMPANY: " + t.getMessage());
                }
            });
        }
    }

}
