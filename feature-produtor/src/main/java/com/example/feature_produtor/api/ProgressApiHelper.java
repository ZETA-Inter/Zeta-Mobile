package com.example.feature_produtor.api;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_produtor.dto.request.ProgressAddRequestDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressApiHelper {

    private static final String TAG = "ProgressApiHelper";

    public interface ProgressUpdateCallback {
        // Agora retorna o novo TOTAL calculado, não o ganho.
        void onProgressUpdated(int newPercentageTotal);
        void onError(String message);
    }

    // MUDANÇA DE ASSINATURA: Adicionamos currentTotalPercentage
    public static void updateProgramProgress(Context context, Integer programId, int percentageGain, int pointsGain, int currentTotalPercentage, int workerId, @Nullable ProgressUpdateCallback callback) {

        if (context == null || programId == null || workerId == -1 || callback == null) {
            Log.e(TAG, "Dados de salvamento incompletos ou inválidos.");
            if (callback != null) {
                callback.onError("Dados de salvamento incompletos ou inválidos.");
            }
            return;
        }

        final int newCalculatedTotal = Math.min(currentTotalPercentage + percentageGain, 100);

        ApiPostgres client = RetrofitClientPostgres
                .getInstance(context.getApplicationContext())
                .create(ApiPostgres.class);

        ProgressAddRequestDTO request = new ProgressAddRequestDTO(
                pointsGain,
                newCalculatedTotal, // O ganho em porcentagem
                workerId,
                programId
        );




        client.addProgramProgress(request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Progresso/Pontos adicionados com sucesso! Novo Total: " + newCalculatedTotal + "%");

                            // Retorna o total que o Fragment deve assumir como o novo estado
                            callback.onProgressUpdated(newCalculatedTotal);
                        } else {
                            Log.e(TAG, "Falha ao adicionar progresso. Code: " + response.code());
                            callback.onError("Falha HTTP: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e(TAG, "Erro de conexão ao adicionar progresso: " + t.getMessage());
                        callback.onError("Erro de conexão: " + t.getMessage());
                    }
                });
    }

}