package com.example.core;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.CompanyResponse;
import com.example.core.dto.response.WorkerResponse;
import com.example.core.network.RetrofitClientPostgres;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends AppCompatActivity {

    private ApiPostgresClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_profile);

        // Usa Retrofit com Context para evitar NPE no interceptor
        api = RetrofitClientPostgres.getApiService(this);

        RecyclerView lesson = findViewById(R.id.item_lesson);
        RecyclerView progress_goals = findViewById(R.id.item_progress_goals);
        RecyclerView recyclerView = findViewById(R.id.item_progress_lesson);

        String kind = getIntent().getStringExtra(ProfileStarter.EXTRA_KIND);
        int id = getIntent().getIntExtra(ProfileStarter.EXTRA_ID, -1);

        if (kind == null || id <= 0) {
            Toast.makeText(this, "Parâmetros inválidos (tipo ou id).", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if ("COMPANY".equals(kind)) {
            loadCompanyById(id);
            // Se desejar esconder listas no perfil de COMPANY, faça aqui.
            // findViewById(R.id.item_lesson).setVisibility(View.GONE);
            // findViewById(R.id.item_progress_lesson).setVisibility(View.GONE);
            // findViewById(R.id.item_progress_goals).setVisibility(View.GONE);
        } else {
            loadWorker(id);
            listProgressGoals(id);
            listLessonsWorker(id);
            listProgressLesson(id);
        }
    }

    // ===== COMPANY =====
    private void loadCompanyById(int companyId) {
        Call<CompanyResponse> call = api.findCompanyById(companyId);
        call.enqueue(new Callback<CompanyResponse>() {
            @Override public void onResponse(@NonNull Call<CompanyResponse> call, @NonNull Response<CompanyResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    CompanyResponse c = resp.body();
                    TextView tv = findViewById(R.id.nome_worker);
                    if (tv != null) tv.setText(c.getName());
                    // TODO: carregar imagem (Glide/Picasso) se houver c.getImageUrl()
                } else {
                    logHttpError(resp);
                }
            }
            @Override public void onFailure(@NonNull Call<CompanyResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "loadCompanyById", t);
            }
        });
    }

    // ===== WORKER =====
    private void loadWorker(int workerId) {
        api.findWorkerById(workerId).enqueue(new Callback<WorkerResponse>() {
            @Override public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    WorkerResponse w = resp.body();
                    TextView tv = findViewById(R.id.nome_worker);
                    if (tv != null) tv.setText(w.getName());
                    // TODO: carregar imagem se existir
                } else {
                    logHttpError(resp);
                }
            }
            @Override public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "loadWorker", t);
            }
        });
    }

    private void listLessonsWorker(int workerId) {
        api.findProgramById(String.valueOf(workerId)).enqueue(new Callback<WorkerResponse>() {
            @Override public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: montar adapter de lessons
                } else {
                    logHttpError(response);
                }
            }
            @Override public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "listLessonsWorker", t);
            }
        });
    }

    private void listProgressGoals(int workerId) {
        api.findProgressGoalsById(String.valueOf(workerId)).enqueue(new Callback<WorkerResponse>() {
            @Override public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: montar adapter de goals
                } else {
                    logHttpError(response);
                }
            }
            @Override public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "listProgressGoals", t);
            }
        });
    }

    private void listProgressLesson(int workerId) {
        api.findLessonsProgressById(String.valueOf(workerId)).enqueue(new Callback<WorkerResponse>() {
            @Override public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: montar adapter de progress de lesson
                } else {
                    logHttpError(response);
                }
            }
            @Override public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "listProgressLesson", t);
            }
        });
    }

    private void logHttpError(Response<?> resp) {
        Log.e("API_ERROR", "HTTP " + resp.code() + " - " + resp.message());
    }
}
