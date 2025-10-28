package com.example.core.client;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static ApiPostgresClient api;

    public static ApiPostgresClient get() {
        if (api == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api-postgresql-zeta-fide.onrender.com/") // sem /api/ no final
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(ApiPostgresClient.class);
        }
        return api;
    }

    private ApiClient() {}
}
