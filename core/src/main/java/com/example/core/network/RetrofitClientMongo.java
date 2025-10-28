package com.example.core.network;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClientMongo {

    private static volatile Retrofit retrofit;
    private static final String BASE_URL_MONGO = "https://api-mongodb-fjnf.onrender.com/";
    private static final String AUTH_TOKEN = "Bearer TokenUserZeta1234";

    private RetrofitClientMongo() {}

    public static Retrofit getInstance(@NonNull Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClientMongo.class) {
                if (retrofit == null) {

                    // Interceptor para Adicionar o Token de Autorização
                    Interceptor authInterceptor = chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .addHeader("Authorization", AUTH_TOKEN)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .addInterceptor(authInterceptor)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL_MONGO)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static void invalidate() {
        synchronized (RetrofitClientMongo.class) {
            retrofit = null;
        }
    }
}