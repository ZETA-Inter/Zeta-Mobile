package com.example.feature_fornecedor;

import android.content.Context;
import android.util.Log;

import com.example.feature_fornecedor.dto.FinishedGoalsPercentage;
import com.example.feature_fornecedor.dto.ProgramCountResponse;
import com.example.feature_fornecedor.dto.ProgramGoalResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FeatureCompanyAPI {

    // === ENDPOINT REAL ===
    @GET("api/companies/count-workers-by-program/{companyId}")
    Call<List<ProgramCountResponse>> countWorkersByProgram(@Path("companyId") int companyId);

    @GET("api/goals/count-goals-by-program/{companyId}")
    Call<List<ProgramGoalResponse>> countGoalsByProgram(@Path("companyId") int companyId);

    // 1)
    @GET("api/companies/average-progress-percentage/{companyId}")
    Call<Double> averageProgressPercentage(@Path("companyId") int id);

    // 2)
    @GET("api/companies/average-points/{companyId}")
    Call<Double> averagePoints(@Path("companyId") int id);

    // 3)
    @GET("api/goals/finished-goals-percentage/{companyId}")
   Call<FinishedGoalsPercentage> finishedGoalsPercentage(@Path("companyId") int id);



    // =========================================================
    // CLIENTE SINGLETON
    // =========================================================
    static FeatureCompanyAPI getInstance(Context context) {
        return Holder.get(context);
    }

    class Holder {
        private static FeatureCompanyAPI INSTANCE;

        static synchronized FeatureCompanyAPI get(Context context) {
            if (INSTANCE == null) {
                try {
                    // Usa os valores fixos definidos no core
                    String baseUrl = context.getString(com.example.core.R.string.core_api_base_url);
                    String token = context.getString(com.example.core.R.string.core_api_token);

                    Log.d("FeatureCompanyAPI", "BaseURL=" + baseUrl + " | Token=" + token);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new Interceptor() {
                                @Override
                                public Response intercept(Chain chain) throws java.io.IOException {
                                    Request original = chain.request();
                                    Request.Builder builder = original.newBuilder()
                                            .header("Accept", "application/json");

                                    if (token != null && !token.isEmpty()) {
                                        builder.header("Authorization", "Bearer " + token);
                                    }

                                    return chain.proceed(builder.build());
                                }
                            })
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();

                    INSTANCE = retrofit.create(FeatureCompanyAPI.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao inicializar FeatureCompanyAPI", e);
                }
            }
            return INSTANCE;
        }
    }
}
