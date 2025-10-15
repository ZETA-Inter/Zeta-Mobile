package com.example.core;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.ProgramResponse;
import com.example.core.dto.response.WorkerResponse;
import com.example.feature_fornecedor.ListPage.ListAPI;
import com.example.feature_fornecedor.ListPage.Worker;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileWorker extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_profile_worker);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayout), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });


        RecyclerView lesson = findViewById(R.id.item_lesson);
        RecyclerView progress_goals = findViewById(R.id.item_progress_goals);
        RecyclerView recyclerView = findViewById(R.id.item_progress_lesson);

        listLessonsWorker();
        listProgressGoals();
        listProgressLesson();
    }

    private void listLessonsWorker() {
        String BASE_URL = "https://api-postgresql-zeta-fide.onrender.com/api/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiPostgresClient APIpostgres = retrofit.create(ApiPostgresClient.class);

        String workerId = null;
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int worker_id = prefs.getInt("user_id", -1);
        if (worker_id == -1){
            Toast.makeText(this, "Erro ao buscar dados do trabalhador", Toast.LENGTH_SHORT).show();
        }
        else {
            workerId = String.valueOf(worker_id);
        }

        Call<WorkerResponse> call = APIpostgres.findProgramById(workerId);
        call.enqueue(new Callback<WorkerResponse>() {
            @Override
            public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WorkerResponse workerResponse = response.body();

                } else {
                    Log.e("API_ERROR", "Resposta não foi bem-sucedida. Código: " + response.code() + ", Mensagem: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "Falha na chamada à API: ", t);
            }
        });
    }

    private void listProgressGoals() {

        String BASE_URL = "https://api-postgresql-zeta-fide.onrender.com/api/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiPostgresClient APIpostgres = retrofit.create(ApiPostgresClient.class);

        String workerId = null;
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int worker_id = prefs.getInt("user_id", -1);
        if (worker_id == -1){
            Toast.makeText(this, "Erro ao buscar dados do trabalhador", Toast.LENGTH_SHORT).show();
        }
        else {
            workerId = String.valueOf(worker_id);
        }

        Call<WorkerResponse> call = APIpostgres.findLessonsProgressById(workerId);
        call.enqueue(new Callback<WorkerResponse>() {
            @Override
            public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WorkerResponse workerResponse = response.body();

                } else {
                    Log.e("API_ERROR", "Resposta não foi bem-sucedida. Código: " + response.code() + ", Mensagem: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "Falha na chamada à API: ", t);
            }
        });

    }

    private void listProgressLesson() {
        String BASE_URL = "https://api-postgresql-zeta-fide.onrender.com/api/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiPostgresClient APIpostgres = retrofit.create(ApiPostgresClient.class);

        String workerId = null;
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int worker_id = prefs.getInt("user_id", -1);
        if (worker_id == -1){
            Toast.makeText(this, "Erro ao buscar dados do trabalhador", Toast.LENGTH_SHORT).show();
        }
        else {
            workerId = String.valueOf(worker_id);
        }

        Call<WorkerResponse> call = APIpostgres.findProgressGoalsById(workerId);
        call.enqueue(new Callback<WorkerResponse>() {
            @Override
            public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WorkerResponse workerResponse = response.body();

                } else {
                    Log.e("API_ERROR", "Resposta não foi bem-sucedida. Código: " + response.code() + ", Mensagem: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "Falha na chamada à API: ", t);
            }
        });

    }
}