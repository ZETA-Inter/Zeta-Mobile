package com.example.core;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.R;
import com.example.core.client.ApiClient;
import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.CompanyResponse;
import com.example.core.dto.response.WorkerResponse;

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

        api = ApiClient.get();

        RecyclerView lesson = findViewById(R.id.item_lesson);
        RecyclerView progress_goals = findViewById(R.id.item_progress_goals);
        RecyclerView recyclerView = findViewById(R.id.item_progress_lesson);

        String kind = getIntent().getStringExtra(ProfileStarter.EXTRA_KIND);
        int id = getIntent().getIntExtra(ProfileStarter.EXTRA_ID, -1);
        if (id <= 0 || kind == null) {
            Toast.makeText(this, "Parâmetros inválidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if ("COMPANY".equals(kind)) {
            loadCompany(id);
            // Se não quiser mostrar listas de worker para COMPANY, esconda as RecyclerViews aqui.
            // findViewById(R.id.item_lesson).setVisibility(View.GONE); etc.
        } else {
            loadWorker(id);
            listProgressGoals(id);
            listLessonsWorker(id);
            listProgressLesson(id);
        }
    }

    private void loadCompany(int companyId) {
        // Ideal: findCompanyById. Se não tiver, use findCompanyByEmail (precisa do e-mail da sessão)
        Call<CompanyResponse> call = api.findCompanyById(companyId); // adicione o endpoint se não existir
        call.enqueue(new Callback<CompanyResponse>() {
            @Override public void onResponse(@NonNull Call<CompanyResponse> call, @NonNull Response<CompanyResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    CompanyResponse c = resp.body();
                    ((TextView)findViewById(R.id.nome_worker)).setText(c.getName());
                    // Atualize outros campos: plano, imagem, etc.
                } else {
                    logHttpError(resp);
                }
            }
            @Override public void onFailure(@NonNull Call<CompanyResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "loadCompany", t);
            }
        });
    }

    private void loadWorker(int workerId) {
        api.findWorkerById(workerId).enqueue(new Callback<WorkerResponse>() {
            @Override public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    WorkerResponse w = resp.body();
                    ((TextView)findViewById(R.id.nome_worker)).setText(w.getName());
                    // Preencha imagem e demais campos do header, se houver
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
                    // TODO: montar adapter de lessons com response.body()
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
