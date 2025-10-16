package com.example.feature_fornecedor.ranking;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit get() {
        if (retrofit == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .build();

            retrofit = new Retrofit.Builder()
                    // baseUrl é exigido pelo Retrofit mesmo usando @Url; não influencia a chamada
                    .baseUrl("https://api-postgresql-zeta-fide.onrender.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
