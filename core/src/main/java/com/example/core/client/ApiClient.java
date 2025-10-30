package com.example.core.client;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static ApiPostgresClient api;
    private static final String BASE_URL = "https://api-postgresql-zeta-fide.onrender.com/";
    private static final String PREF_NAME = "user_session";
    private static final String TOKEN_KEY = "access_token"; // Chave que armazena o token

    // ✅ Alteração: O método 'get' agora recebe o Context
    public static ApiPostgresClient get(@NonNull Context context) {
        if (api == null) {

            // 1. Crie o Interceptor para adicionar o token
            Interceptor authInterceptor = new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    // Busca o token no SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    String token = prefs.getString(TOKEN_KEY, null);

                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder();

                    if (token != null) {
                        // Adiciona o cabeçalho de Autorização no formato Bearer
                        builder.header("Authorization", "Bearer " + token);
                    }

                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                }
            };

            // 2. Crie o OkHttpClient com o Interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor) // Adiciona a lógica de autenticação
                    .build();

            // 3. Crie o Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            api = retrofit.create(ApiPostgresClient.class);
        }
        return api;
    }

    private ApiClient() {}
}