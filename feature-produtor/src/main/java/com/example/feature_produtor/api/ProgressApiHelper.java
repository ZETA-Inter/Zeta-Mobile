package com.example.feature_produtor.api;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_produtor.dto.request.GradeRequestDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressApiHelper {

    private static final String TAG = "ProgressApiHelper";

    // 1. Interface de Retorno (Callback)
    public interface ProgressUpdateCallback {
        void onProgressUpdated(int newPercentage);
        void onError(String message);
    }

    // 2. Método Modificado para receber o Callback
    public static void updateProgramProgress(Context context, int programId, int percentage, int workerId, @Nullable ProgressUpdateCallback callback) {

        if (context == null || workerId == -1 || callback == null) {
            Log.e(TAG, "Worker ID, Context ou Callback inválido. Não foi possível salvar o progresso.");
            if (callback != null) {
                callback.onError("Dados de salvamento incompletos ou inválidos.");
            }
            return;
        }

        ApiPostgres client = RetrofitClientPostgres
                .getInstance(context.getApplicationContext())
                .create(ApiPostgres.class);

        // O PERCENTUAL DE PROGRESSO É O 'GRADE' (Nota)
        // O DTO esperado pelo servidor é o GradeRequestDTO
        GradeRequestDTO request = new GradeRequestDTO(workerId, programId, percentage);

        // Agora usamos a chamada correta com o DTO de Grade
        client.updateProgramProgress(request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Progresso/Grade atualizado com sucesso! Progresso: " + percentage + "%");
                            callback.onProgressUpdated(percentage);
                        } else {
                            // Este erro 404 DEVE desaparecer com a rota correta!
                            Log.e(TAG, "Falha ao atualizar progresso. Code: " + response.code() + ". Verifique a URL Base.");
                            callback.onError("Falha HTTP: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e(TAG, "Erro de conexão ao atualizar progresso: " + t.getMessage());
                        callback.onError("Erro de conexão: " + t.getMessage());
                    }
                });
    }

}