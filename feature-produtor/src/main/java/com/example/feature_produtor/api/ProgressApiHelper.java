

package com.example.feature_produtor.api;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_produtor.dto.request.ProgressUpdatePayload;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressApiHelper {

    private static final String TAG = "ProgressApiHelper";

    // O método updateProgramProgress agora é estático e centralizado aqui
    public static void updateProgramProgress(Context context, int programId, int percentage, int workerId) {
        if (context == null || workerId == -1) {
            Log.e(TAG, "Worker ID ou Context inválido. Não foi possível salvar o progresso.");
            return;
        }

        ApiPostgres client = RetrofitClientPostgres
                .getInstance(context.getApplicationContext())
                .create(ApiPostgres.class);

        ProgressUpdatePayload request = new ProgressUpdatePayload(programId, percentage);

        client.updateProgramProgress(workerId, request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Progresso atualizado para " + percentage + "% no programa " + programId + " (via Helper)");
                        } else {
                            Log.e(TAG, "Falha ao atualizar progresso. Code: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e(TAG, "Erro de conexão ao atualizar progresso: " + t.getMessage());
                    }
                });
    }
}